package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.crypto.ByteOrder;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:24
 * @version: 0.0.1
 */
public final class TransportFragment {
    private static final int FRAG_HEADER_LEN = 10; /* sizeof(long) + sizeof(short) */

    private static final int FRAGMENT_BUFFER_LEN = NetworkConstants.DEFAULT_SEND_MTU * 2;

    public static class Fragment {
        private static final byte[] BUFFER = new byte[FRAGMENT_BUFFER_LEN];

        private final long id;
        private final short fragmentNum;
        private final boolean finalize;

        private final boolean initialized;

        private final byte[] contents;

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

            assert (fragmentNum & 0x8000) > 0;
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

}
