package controlblock;

import java.util.ArrayList;
import java.util.List;

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
    private Object[] objects;

    public ConsHeap(int nrCons) {
        heap = new int[nrCons * 2];
        refcount = new byte[nrCons];
        refcount[0] = 99;
        heap_size = nrCons;
        objects = new Object[1];
        objects[0] = "NULL";
    }

    /* Returns the atom (i) if an atom */
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
        refcount[a]++;
        refcount[b]++;
        return p;
    }

    public int list3(int a, int b, int c) {
        int p = newCons();
        heap[p * 2] = a;
        heap[(a * 2) + 1] = b;
        heap[(b * 2) + 1] = c;
        refcount[a]++;
        refcount[b]++;
        refcount[c]++;
        return p;
    }

    public int list4(int a, int b, int c, int d) {
        int p = newCons();
        heap[p * 2] = a;
        heap[(a * 2) + 1] = b;
        heap[(b * 2) + 1] = c;
        heap[(c * 2) + 1] = d;
        refcount[a]++;
        refcount[b]++;
        refcount[c]++;
        refcount[d]++;
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

    public int appendCons(int cons, int item) {
        while (this.heap[(cons * 2) + 1] > 0) {
            cons = this.heap[(cons * 2) + 1];
        }
        this.heap[(cons * 2) + 1] = item;
        return cons;
    }

    public int appendList(int list, int item) {
        if (atom(list)) {
            return 0;
        }
        if (empty(list)) {
            this.heap[list * 2] = item;
            return item;
        }
        int head = this.heap[list * 2];
        return appendCons(head, item);
    }

    // a is a list of k/v pairs
    public int pairGet(int a, String key) {
        if (atom(a)) {
            return 0;
        }
        int head = car(a);
        while (head > 0) {
            int pair = car(head);
            if (atomString(pair).equals(key)) {
                return this.heap[(pair * 2) + 1];
            }
            head = this.heap[(head * 2) + 1];
        }
        return 0;
    }

    public int pairSet(int a, String key, int v) {
        if (atom(a)) {
            return 0;
        }
        int head = car(a);
        if (head == 0) {
            this.heap[a * 2] = list2(newSymbol(key), v);
            return a;
        }
        while (this.heap[(head * 2) + 1] > 0) {
            int pair = car(head);
            if (atomString(pair).equals(key)) {
                int cdr_pair = cdr(pair);
                this.refcount[v]++;
                this.heap[(pair * 2) + 1] = v;
                if (cdr_pair != 0) {
                    deref(cdr_pair);
                }
                return a;
            }
            head = this.heap[(head * 2) + 1];
        }
        this.heap[(head * 2) + 1] = list2(newSymbol(key), v);
        return a;
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

    public void push(int list, int item) {
        if (atom(list)) {
            return;
        }
        if (this.heap[list * 2] != 0) {
            // list is not the empty list so swap item in as the first element
            this.heap[(item * 2) + 1] = this.heap[list * 2];
        }
        // point the car of b to a
        this.heap[list * 2] = item;
        this.refcount[item]++;
        return;
    }

    public int pop(int i) {
        int car = car(i);
        int cdr = cdr(car);
        this.heap[i * 2] = cdr;
        this.heap[(car * 2) + 1] = 0;
        this.refcount[car]--;
        return car;
    }

    public int car(int i) {
//        if (heap[i * 2] < 0) {
//            return 0;
//        } WTF??
        return heap[i * 2];
    }

    public int cdr(int i) {
        return heap[(i * 2) + 1];
    }

    int setq(int i, int val) {
        if (i == 0) {
            System.out.println("Setting cell ZERO to a value!");
        }
        if (this.heap[i * 2] != 0) {
            if ((this.heap[i * 2] < 0) && this.refcount[i] <= 2) {
                objects[0 - heap[i * 2]] = null;
            }
            else {
                deref(i);
            }
        }
        this.heap[i * 2] = car(val);
        return val;
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
        Object thing = objects[0 - heap[n * 2]];
        if (thing instanceof String) {
            return (String)thing;
        }
        return thing.toString();
    }

    /* Add a string to the global store of them */
    private int addString(String toAdd) {
        int at = objects.length;
        Object[] newObjects = new Object[at + 1];
        for (int i = 0; i < objects.length; i++) {
            newObjects[i] = objects[i];
        }
        newObjects[objects.length] = toAdd;
        objects = newObjects;
        return at;
    }

    public String pairStringGet(int a, String b) {
        return atomString(pairGet(a, b));
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

    public void dumpConsSys(int indent, int i) {
        int n = i;
        while (n > 0) {
            if (atom(n)) {
                System.out.print(n + "[\"" + atomString(n) + "\":" + heap[(n * 2) + 1] + ":" + refcount[n] + "]");
                if (heap[(n * 2) + 1] > 0) {
                    System.out.print(" ");
                }
            }
            else if (heap[n * 2] == 0 && heap[(n * 2) + 1] == 0) {
                System.out.print(n + ":" + refcount[n] + "()");
            }
            else {
                System.out.print(n + ":" + refcount[n] + "(");
                dumpConsSys(indent + 2, heap[n * 2]);
                System.out.print(")");
                if (heap[(n * 2) + 1] > 0) {
                    System.out.print(" ");
                }
            }
            n = heap[(n * 2) + 1];
        }
    }

    public void dumpSys(int i) {
        dumpConsSys(0, i);
        System.out.println();
    }

    public void dumpCons(int indent, int i) {
        int n = i;
        while (n > 0) {
            if (atom(n)) {
                System.out.print(atomString(n));
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

    public void dump(String name, int i) {
        System.out.print(name + ": ");
        dumpCons(0, i);
        System.out.println();
    }

    public void info(String name, int i) {
        System.out.println("Id: " + i);
        if (heap[i * 2] == 0) {
            System.out.println("TYPE: Empty list");
            System.out.println("REFCOUNT: " + refcount[i]);
        }
        else if (heap[i * 2] < 0) {
            System.out.println("TYPE: String");
            System.out.println("REFCOUNT: " + refcount[i]);
        }
        else {
            System.out.println("TYPE: List");
            System.out.println("REFCOUNT: " + refcount[i]);            
        }
        System.out.print("DUMP: ");
        dump(name, i);
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
                    System.out.print(z + "[" + atomString(z) + ":" + heap[(z * 2) + 1] + ":" + refcount[z] + "] ");
                }
                else {
                    System.out.print(z + "[" + heap[z * 2] + ":" + heap[(z * 2) + 1] + ":" + refcount[z] + "] ");
                }
            }
        }
        System.out.println();
    }

    int refCount(int i) {
        return this.refcount[i];
    }

    void deref(int i) {
        while (i > 0) {
            this.refcount[i]--;
            if (this.refcount[i] > 1) {
                return;
            }
            int j = this.heap[(i * 2) + 1];
            this.refcount[i] = 0;
            if (this.heap[i * 2] < 0) {
                objects[0 - heap[i * 2]] = null;
            }
            if (this.heap[i * 2] > 0) {
                deref(this.heap[i * 2]);
            }
            this.heap[i * 2] = 0;
            this.heap[(i * 2) + 1] = 0;
            i = j;
        }
    }
}
