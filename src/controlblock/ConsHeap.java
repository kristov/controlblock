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
    public boolean atom(int i) {
        return heap[i * 2] < 0;
    }

    public void cons(int a, int b) {
        heap[(a * 2) + 1] = b;
    }

    public int list1(int a) {
        int p = newCons();
        heap[p * 2] = a;
        return p;
    }

    public int list2(int a, int b) {
        int p = newCons();
        heap[p * 2] = a;
        heap[(a * 2) + 1] = b;
        return p;
    }

    public int list3(int a, int b, int c) {
        int p = newCons();
        heap[p * 2] = a;
        heap[(a * 2) + 1] = b;
        heap[(b * 2) + 1] = c;
        return p;
    }

    public boolean eq(int a, int b) {
        if (atom(a) && atom(b)) {
            if (atomString(a).equals(atomString(b))) {
                return true;
            }
        }
        return false;
    }

    public boolean isTrue(int n) {
        if (atom(n)) {
            // decide if "0" and "NULL" are true or not :-/
            return true;
        }
        return true;
    }

    // a is a list of k/v pairs
    public int pairGet(int a, int b) {
        if (heap[a * 2] <= 0) {
            return 0;
        }
        a = heap[a * 2];
        do {
            int k = heap[a * 2];
            if (eq(k, b)) {
                return heap[(k * 2) + 1];
            }
            a = heap[(a * 2) + 1];
        } while (a > 0);
        return 0;
    }

    public void append(int a, int b) {
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

    /* (setf place (cons item place)) */
    public void push(int list, int item) {
        if (atom(list)) {
            return;
        }
        if (heap[list * 2] != 0) {
            // list is not the empty list so swap item in as the first element
            heap[(item * 2) + 1] = heap[list * 2];
        }
        // point the car of b to a
        heap[list * 2] = item;
        return;
    }

    /* (prog1 (car place) (setf place (cdr place))) */
    public int pop(int i) {
        int car = car(i);
        int cdr = cdr(car);
        heap[i * 2] = cdr;
        heap[(car * 2) + 1] = 0;
        // deref car
        return car;
    }

    public int car(int i) {
        if (heap[i * 2] < 0) {
            return 0;
        }
        return heap[i * 2];
    }

    public int cdr(int i) {
        return heap[(i * 2) + 1];
    }

    public boolean empty(int i) {
        return heap[i * 2] == 0;
    }

    /* Create a new symbol (string) */
    public int newSymbol(String symbol) {
        int strIdx = addString(symbol);
        int i = newCons();
        heap[(i * 2)] = 0 - strIdx;
        return i;
    }

    public int copy(int i) {
        int n = newCons();
        heap[n * 2] = heap[i * 2];
        return n;
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

    public boolean symbolEq(int n, String symbol) {
        if (!atom(n)) {
            return false;
        }
        return atomString(n).equals(symbol);
    }

    public String atomString(int n) {
        return strings[0 - heap[n * 2]];
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
    
    public String pairStringGet(int a, String b) {
        if (heap[a * 2] <= 0) {
            return "";
        }
        a = heap[a * 2];
        do {
            int k = heap[a * 2];
            String key = atomString(k);
            if (key.equals(b)) {
                return atomString(cdr(k));
            }
            a = heap[(a * 2) + 1];
        } while (a > 0);
        return "";
    }

    private int plus_e(int env) {
        String a = pairStringGet(env, "a");
        String b = pairStringGet(env, "b");
        Float r = Float.valueOf(a) + Float.valueOf(b);
        return newSymbol(r.toString());
    }

    public int dispatch(int symbol, int env) {
        String sym = atomString(symbol);
        if (sym.equals("plus_e")) {
            return plus_e(env);
        }
        return 0;
    }

    public int buildEnv() {
        int env = newCons();
        append(env, list2(newSymbol("car"), list3(newSymbol("lambda"), list1(newSymbol("a")), newSymbol("car_e"))));
        append(env, list2(newSymbol("cdr"), list3(newSymbol("lambda"), list1(newSymbol("a")), newSymbol("cdr_e"))));
        append(env, list2(newSymbol("cons"), list3(newSymbol("lambda"), list2(newSymbol("a"), newSymbol("b")), newSymbol("cons_e"))));
        append(env, list2(newSymbol("+"), list3(newSymbol("lambda"), list2(newSymbol("a"), newSymbol("b")), newSymbol("plus_e"))));
        append(env, list2(newSymbol("eq"), list3(newSymbol("lambda"), list2(newSymbol("a"), newSymbol("b")), newSymbol("eq_e"))));
        return env;
    }

    public void dumpCons(int indent, int i) {
        int n = i;
        while (n > 0) {
            if (atom(n)) {
                System.out.print(strings[0 - heap[n * 2]]);
                if (heap[(n * 2) + 1] > 0) {
                    System.out.print(" ");
                }
            }
            else if (heap[n * 2] == 0 && heap[(n * 2) + 1] == 0) {
                //System.out.print("()");
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

    public void dumpHeapWithStrings() {
        for (int z = 0; z < heap_size; z++) {
            if (refcount[z] > 0) {
                if (heap[z * 2] < 0) {
                    System.out.print(z + "[" + strings[0 - (heap[z * 2])] + ":" + heap[(z * 2) + 1] + "] ");
                }
                else {
                    System.out.print(z + "[" + heap[z * 2] + ":" + heap[(z * 2) + 1] + "] ");
                }
            }
        }
        System.out.println();
    }
}
