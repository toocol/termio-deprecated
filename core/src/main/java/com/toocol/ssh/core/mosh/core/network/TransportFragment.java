package com.toocol.ssh.core.mosh.core.network;

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

        @Override
        public String toString() {
            return "Fragment{" +
                    "id=" + id +
                    ", fragmentNum=" + fragmentNum +
                    ", finalize=" + finalize +
                    ", initialized=" + initialized +
                    ", contents='" + contents + '\'' +
                    '}';
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

            String payload = getCompressor().compressStr(inst.toString());
            short fragmentNum = 0;

            Queue<Fragment> ret = new ArrayDeque<>();
            while (StringUtils.isNotEmpty(payload)) {
                String thisFragment;
                boolean finalize = false;

                if (payload.length() > mtu) {
                    byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
                    thisFragment = new String(bytes, 0, mtu);
                    payload = new String(bytes, mtu, bytes.length - mtu);
                } else {
                    thisFragment = payload;
                    payload = null;
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
