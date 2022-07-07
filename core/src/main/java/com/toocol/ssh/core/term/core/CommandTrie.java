package com.toocol.ssh.core.term.core;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/7 16:57
 */
public final class CommandTrie {
    private static final int CHARS = 26;

    protected static final class Node {
        public boolean end;
        public String cmd;

        public final Node[] tns = new Node[CHARS];

        public int nextSize() {
            int cnt = 0;
            for (Node tn : tns) {
                if (tn != null) {
                    cnt++;
                }
            }
            return cnt;
        }

        public Node next() {
            for (Node tn : tns) {
                if (tn != null) {
                    return tn;
                }
            }
            return null;
        }
    }

    private final Node root = new Node();

    /**
     * Insert a cmd to CommandTrie
     */
    public void insert(String cmd) {
        Node p = root;
        for (char ch : cmd.toCharArray()) {
            int u = ch - 'a';
            if (p.tns[u] == null) {
                p.tns[u] = new Node();
            }
            p = p.tns[u];
        }
        p.end = true;
        p.cmd = cmd;
    }

    /**
     * Checking whether CommandTrie have the cmd equals input cmd.
     */
    public boolean contains(String cmd) {
        Node p = root;
        for (char ch : cmd.toCharArray()) {
            int u = ch - 'a';
            if (p.tns[u] == null) return false;
            p = p.tns[u];
        }
        return p.end;
    }

    /**
     * Checking whether CommandTrie have the cmd stars with the prefix or not.
     */
    public boolean startsWith(String prefix) {
        Node p = root;
        for (char ch : prefix.toCharArray()) {
            int u = ch - 'a';
            if (p.tns[u] == null) return false;
            p = p.tns[u];
        }
        return true;
    }

    /**
     * Auto-complex cmd.
     */
    public String complex(String cmd) {
        Node p = root;
        for (char ch : cmd.toCharArray()) {
            int u = ch - 'a';
            if (p.tns[u] == null) {
                return cmd;
            }
            p = p.tns[u];
        }
        while (p != null && p.nextSize() > 1) {
            p = p.next();
        }
        return (p != null && p.end) ? p.cmd : cmd;
    }
}
