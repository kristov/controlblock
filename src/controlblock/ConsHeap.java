package controlblock;

/* The heap is an array of ints nrCons * 2 in size. A cell is identified by an
 * id int. The location of the cell in the array is id * 2. Int 1 of the cell is
 * the "car". If the car is less than 0 it's the id of a string. If it's greater
 * than 0 it's an id of another cell - it's a list. Int 2 of the cell is the
 * "cdr". It is an id of the next cell. If zero there are no more cells in this
 * cons list.
 */
public class ConsHeap {
    private final int heap_size;
    private final int[] heap;
    private final byte[] refcount;
    private String[] strings;

    public ConsHeap(int nrCons) {
        heap = new int[nrCons * 2];
        refcount = new byte[nrCons];
        heap_size = nrCons;
        strings = new String[] {"NULL"};
    }

    /* Returns the atom (i) if an atom, or the empty list (0) */
    public int atom(int i) {
        return heap[i * 2] < 0 ? i : 0;
    }

    public int car(int i) {
        return heap[i * 2];
    }

    public int cdr(int i) {
        return heap[(i * 2) + 1];
    }

    public int cons(int a, int b) {
        if (heap[b * 2] < 0) {
            // b is an atom
            return 0;
        }
        if (heap[b * 2] != 0) {
            // b is not the empty list so swap a in as the first element
            heap[(a * 2) + 1] = heap[b * 2];
        }
        // point the car of b to a
        heap[b * 2] = a;
        return b;
    }

    /* A list is a cell whos car is another cell */
    public int list() {
        int i = newCons();
        int j = newCons();
        heap[i * 2] = j;
        return i;
    }

    public void appendList(int a, int b) {
        if (heap[a * 2] == 0) {
            heap[a * 2] = b;
            return;
        }
        a = heap[a * 2];
        while (heap[(a * 2) + 1] > 0) {
            a = heap[(a * 2) + 1];
        }
        heap[(a * 2) + 1] = b;
    }

    /* Create a new symbol (string) */
    public int newSymbol(String symbol) {
        int strIdx = addString(symbol);
        int i = newCons();
        heap[(i * 2)] = 0 - strIdx;
        return i;
    }

    /* Find an empty cons cell */
    public int newCons() {
        for (int z = 1; z < heap_size; z++) {
            if (refcount[z] == 0) {
                refcount[z] = 1;
                return z;
            }
        }
        return 0;
    }

    /* Add a string to the global store of them */
    private int addString(String toAdd) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(toAdd)) {
                return i;
            }
        }
        int at = strings.length;
        String[] newStrings = new String[at + 1];
        for (int i = 0; i < strings.length; i++) {
            newStrings[i] = strings[i];
        }
        newStrings[strings.length] = toAdd;
        strings = newStrings;
        return at;
    }

    public void dumpCons(int indent, int i) {
        int n = i;
        while (n > 0) {
            if (atom(n) > 0) {
                System.out.print(strings[0 - heap[n * 2]]);
                if (heap[(n * 2) + 1] > 0) {
                    System.out.print(" ");
                }
            }
            else if (heap[n * 2] == 0 && heap[(n * 2) + 1] == 0) {
                System.out.print("()");
            }
            else {
                System.out.print("(");
                dumpCons(indent + 2, heap[n * 2]);
                System.out.print(")");
                if (heap[(n * 2) + 1] > 0) {
                    System.out.print(" ");
                }
            }
            n = heap[(n * 2) + 1];
        }
    }

    /* recursively dumps something */
    public void dump(int i) {
        dumpCons(0, i);
        System.out.println();
    }

    public void dumpHeap() {
        for (int z = 0; z < heap_size; z++) {
            if (refcount[z] > 0) {
                System.out.print(z + "[" + heap[z * 2] + ":" + heap[(z * 2) + 1] + "] ");
            }
        }
        System.out.println();
    }
}
