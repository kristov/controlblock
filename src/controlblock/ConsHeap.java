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
    private String[] strings;

    public ConsHeap(int nrCons) {
        heap = new int[nrCons * 2];
        heap_size = nrCons;
        strings = new String[] {"NULL"};
    }

    /* Returns the atom (i) if an atom, or the empty list (0) */
    public int atom(int i) {
        return heap[i * 2] < 0 ? i : 0;
    }

    public int car(int cons) {
        int car = heap[cons * 2];
        if (car <= 0) {
            return 0;
        }
        return car;
    }

    public int cdr(int cons) {
        int car = heap[cons * 2];
        return heap[(car * 2) + 1];
    }

    /* cons('a (cons 'b (cons 'c '()))) is evaluated inner most first. The empty
     * list is given as id=0. We find a new free cell and set it's car to 'c. We
     * then set its cdr to 0. Then we get called again and create a new cell.
     * We set its car to 'b and its cdr to the result of the first call. We then
     * get called again and we create a third cell and set its car to 'a and its
     * cdr to the result of the second call. So we end up with our memory
     * looking like this:
     *
     *     0      1      2      3
     *     [ 0, 0]['c, 0]['b, 1]['a, 2]
     *
     * The result of the entire eval is 3 (cons cell id=3). The car of 3 is 2,
     * the car of 2 is 1 and the car of 1 is 0.
     *
     */
    public int cons(int a, int b) {
        int free = newCons();
        heap[free * 2] = heap[a * 2];
        heap[(free * 2) + 1] = b;
        return free;
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
        for (int i = 2; i < heap_size; i += 2) {
            if (heap[i * 2] == 0) {
                return i;
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
                System.out.println(strings[0 - heap[i * 2]]);
            }
            n = heap[(n * 2) + 1];
        }
    }

    /* recursively dumps something */
    public void dump(int i) {
        dumpCons(0, i);
    }
}
