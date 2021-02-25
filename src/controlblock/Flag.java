package controlblock;

public class Flag {
    private final int[] flag;
    private final int nrFlagBytes;

    public Flag(int nrCons) {
        nrFlagBytes = nrCons / 32;
        flag = new int[nrFlagBytes];
    }

    public void set(int i) {
        int idx = i / 32;
        int off = 1 << (i % 32);
        flag[idx] |= off;
    }

    public boolean get(int i) {
        int idx = i / 32;
        int off = 1 << (i % 32);
        return (flag[idx] & off) > 0;
    }

    public void clear() {
        for (int idx = 0; idx < nrFlagBytes; idx++) {
            flag[idx] = 0;
        }
    }

    public void dump() {
        for (int idx = 0; idx < nrFlagBytes; idx++) {
            for (int j = 0; j < 32; j++) {
                int t = 1 << j;
                if ((flag[idx] & t) > 0) {
                    System.out.print("1 ");
                }
                else {
                    System.out.print("0 ");
                }
            }
        }
        System.out.println();
    }
}