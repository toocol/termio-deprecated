package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.crypto.ByteOrder;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:24
 * @version: 0.0.1
 */
public final class TransportFragment {
    private static final int FRAG_HEADER_LEN = 10; /* sizeof(long) + sizeof(short) */

    public static class Fragment {
        private static final byte[] BUFFER = new byte[NetworkConstants.DEFAULT_SEND_MTU];

        private final long id;
        private final boolean finalize;
        private final boolean initialized;
        private final byte[] contents;

        private short fragmentNum;

        public Fragment() {
            this.id = -1;
            this.fragmentNum = -1;
            this.finalize = false;
            this.initialized = false;
            this.contents = new byte[0];
        }

        public Fragment(long id, short fragmentNum, boolean finalize, byte[] contents) {
            this.id = id;
            this.fragmentNum = fragmentNum;
            this.finalize = finalize;
            this.contents = contents;

            this.initialized = true;
        }

        public Fragment(byte[] bytes) {
            assert bytes.length > FRAG_HEADER_LEN;

            this.contents = new byte[bytes.length - FRAG_HEADER_LEN];
            System.arraycopy(bytes, FRAG_HEADER_LEN, this.contents, 0, bytes.length - FRAG_HEADER_LEN);

            byte[] idBytes = new byte[8];
            byte[] fragmentNumBytes = new byte[2];
            System.arraycopy(bytes, 0, idBytes, 0, 8);
            System.arraycopy(bytes, 8, fragmentNumBytes, 0, 2);

            this.id = ByteOrder.be64toh(idBytes);
            this.fragmentNum = ByteOrder.be16toh(fragmentNumBytes);
            this.finalize = ((fragmentNum & 0x8000) >>> 15) != 0;
            this.fragmentNum &= 0x7FFF;
            this.initialized = true;
        }

        private byte[] networkOrderBytes(short hostOrder) {
            return ByteOrder.htoBe16(hostOrder);
        }

        private byte[] networkOrderBytes(long hostOrder) {
            return ByteOrder.htoBe64(hostOrder);
        }

        public byte[] toBytes() {
            assert initialized;

            int proceed = 0;

            byte[] nob = networkOrderBytes(id);
            System.arraycopy(nob, 0, BUFFER, proceed, nob.length);
            proceed += nob.length;

            short combinedFragmentNum = (short) (((finalize ? 1 : 0) << 15) | fragmentNum);
            nob = networkOrderBytes(combinedFragmentNum);
            System.arraycopy(nob, 0, BUFFER, proceed, nob.length);
            proceed += nob.length;

            assert proceed == FRAG_HEADER_LEN;

            System.arraycopy(contents, 0, BUFFER, proceed, contents.length);
            proceed += contents.length;

            byte[] ret = new byte[proceed];
            System.arraycopy(BUFFER, 0, ret, 0, proceed);
            return ret;
        }
    }

    public static class Fragmenter implements ICompressorAcquirer {
        private long nextInstructionId;
        private InstructionPB.Instruction lastInstruction;
        private int lastMTU;

        public Fragmenter() {
            this.nextInstructionId = 0;
            this.lastMTU = -1;
        }

        public Queue<Fragment> makeFragments(@Nonnull InstructionPB.Instruction inst, int mtu) {
            mtu -= FRAG_HEADER_LEN;
            if (!inst.equals(lastInstruction) || lastMTU != mtu) {
                nextInstructionId++;
            }

            assert lastInstruction == null
                    || inst.getOldNum() != lastInstruction.getOldNum()
                    || inst.getNewNum() != lastInstruction.getNewNum() || inst.getDiff().equals(lastInstruction.getDiff());

            lastInstruction = inst;
            lastMTU = mtu;

            byte[] payload = getCompressor().compress(inst.toByteArray());
            int remain = payload.length;
            int deal = 0;
            short fragmentNum = 0;

            Queue<Fragment> ret = new ArrayDeque<>();
            while (remain > 0) {
                byte[] thisFragment;
                boolean finalize = false;

                if (remain > mtu) {
                    thisFragment = new byte[mtu];
                    System.arraycopy(payload, deal, thisFragment, 0, mtu);
                    deal += mtu;
                    remain -= mtu;
                } else {
                    thisFragment = new byte[remain];
                    System.arraycopy(payload, deal, thisFragment, 0, remain);
                    remain = 0;
                    finalize = true;
                }

                ret.offer(new Fragment(nextInstructionId, fragmentNum++, finalize, thisFragment));
            }

            return ret;
        }

        public long lastAckSent() {
            return lastInstruction.getAckNum();
        }
    }

    public static class FragmentAssembly implements ICompressorAcquirer{
        private final Set<Fragment> fragments = new TreeSet<>(Comparator.comparingInt(f -> f.fragmentNum));
        private long currentId;
        private int fragmentsArrived;
        private int fragmentsTotal;
        private int contentsLength;

        public FragmentAssembly() {
            this.currentId = -1;
            this.fragmentsArrived = 0;
            this.fragmentsTotal = -1;
            this.contentsLength = 0;
        }

        public boolean addFragment(Fragment fragment) {
            if (currentId != fragment.id) {
                fragments.clear();
                fragments.add(fragment);
                fragmentsArrived = 1;
                fragmentsTotal = -1;
                contentsLength = fragment.contents.length;
                currentId = fragment.id;
            } else {
                /* see if we already have this fragment */
                boolean have = fragments.size() > fragment.fragmentNum
                        && fragment.equals(getAt(fragment.fragmentNum));
                if (!have) {
                    fragments.add(fragment);
                    fragmentsArrived++;
                    contentsLength += fragment.contents.length;
                }
            }

            if (fragment.finalize) {
                fragmentsTotal = fragment.fragmentNum + 1;
                assert fragments.size() <= fragmentsTotal;
            }

            assert fragmentsTotal == -1 || fragmentsArrived <= fragmentsTotal;

            return fragmentsArrived == fragmentsTotal;
        }

        public InstructionPB.Instruction getAssembly() {
            assert fragmentsArrived == fragmentsTotal;

            byte[] contents = new byte[contentsLength];
            AtomicInteger proceed = new AtomicInteger(0);
            fragments.forEach(fragment -> {
                System.arraycopy(fragment.contents, 0, contents, proceed.get(), fragment.contents.length);
                proceed.set(proceed.get() + fragment.contents.length);
            });

            byte[] decompress = getCompressor().decompress(contents);
            try {
                InstructionPB.Instruction instruction = InstructionPB.Instruction.parseFrom(decompress);
                fragments.clear();
                fragmentsArrived = 0;
                fragmentsTotal = -1;
                contentsLength = 0;
                return instruction;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private Fragment getAt(int num) {
            for (Fragment fragment : fragments) {
                if (fragment.fragmentNum == num) {
                    return fragment;
                }
            }
            return null;
        }
    }
}
