package com.toocol.ssh.core.mosh.core.network;

import com.toocol.ssh.core.mosh.core.crypto.ByteOrder;
import com.toocol.ssh.core.mosh.core.proto.InstructionPB;
import com.toocol.ssh.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:24
 * @version: 0.0.1
 */
public final class TransportFragment {
    private static final int FRAG_HEADER_LEN = 10; /* sizeof(long) + sizeof(short) */

    public static class Fragment {
        private final long id;
        private final short fragmentNum;
        private final boolean finalize;

        private final boolean initialized;

        private final String contents;

        public Fragment() {
            this.id = -1;
            this.fragmentNum = -1;
            this.finalize = false;
            this.initialized = false;
            this.contents = StrUtil.EMPTY;
        }

        public Fragment(long id, short fragmentNum, boolean finalize, String contents) {
            this.id = id;
            this.fragmentNum = fragmentNum;
            this.finalize = finalize;
            this.contents = contents;

            this.initialized = true;
        }

        private String networkOrderString(short hostOrder) {
            byte[] netInt = ByteOrder.htoBe16(hostOrder);
            return new String(netInt, StandardCharsets.UTF_8);
        }

        private String networkOrderString(long hostOrder) {
            byte[] netInt = ByteOrder.htoBe64(hostOrder);
            return new String(netInt, StandardCharsets.UTF_8);
        }

        @Override
        public String toString() {
            assert initialized;

            StringBuilder ret = new StringBuilder();

            ret.append(networkOrderString(id));

            assert (fragmentNum & 0x8000) > 0;
            short combinedFragmentNum = (short) (((finalize ? 1 : 0) << 15) | fragmentNum);
            ret.append(networkOrderString(combinedFragmentNum));

            assert ret.length() == FRAG_HEADER_LEN / 2; // byte -> char

            ret.append(contents);

            return ret.toString();
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
                String thisFragment;
                boolean finalize = false;

                if (remain > mtu) {
                    thisFragment = new String(payload, deal, mtu, StandardCharsets.UTF_8);
                    deal += mtu;
                    remain -= mtu;
                } else {
                    thisFragment = new String(payload, deal, remain, StandardCharsets.UTF_8);
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
