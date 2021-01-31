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
    private int root;
    private final int heap_size;
    private final int[] heap;
    private final byte[] refcount;
    private Object[] objects;
    private final byte[] objrefcount;

    public ConsHeap(int nrCons) {
        heap = new int[nrCons * 2];
        refcount = new byte[nrCons];
        refcount[0] = 99;
        heap_size = nrCons;
        objects = new Object[nrCons];
        objects[0] = "NULL";
        objrefcount = new byte[nrCons];
        objrefcount[0] = 99;
        prepareHeap();
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

    public int list5(int a, int b, int c, int d, int e) {
        int p = newCons();
        heap[p * 2] = a;
        heap[(a * 2) + 1] = b;
        heap[(b * 2) + 1] = c;
        heap[(c * 2) + 1] = d;
        heap[(d * 2) + 1] = e;
        refcount[a]++;
        refcount[b]++;
        refcount[c]++;
        refcount[d]++;
        refcount[e]++;
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
            if (atomString(n).equals("0")) {
                return false;
            }
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

    public int reverse(int i) {
        int rev = newCons();
        i = car(i);
        int prev = 0;
        while (i > 0) {
            int n = copy(i);
            heap[(n * 2) + 1] = prev;
            prev = n;
            i = heap[(i * 2) + 1];
        }
        heap[rev * 2] = prev;
        return rev;
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

    public int newSymbol(String symbol) {
        int strIdx = addObject(symbol);
        int i = newCons();
        heap[(i * 2)] = strIdx;
        return i;
    }

    public int copy(int i) {
        int n = newCons();
        int dst = heap[i * 2];
        heap[n * 2] = dst;
        if (dst < 0) {
            refObject(dst);
        }
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

    private int addObject(Object toAdd) {
        int z = 1;
        for (z = 1; z < heap_size; z++) {
            if (objrefcount[z] == 0) {
                objrefcount[z] = 1;
                objects[z] = toAdd;
                return 0 - z;
            }
        }
        return 0;
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

    private void prepareHeap() {
        int frame = list1(newSymbol("frame"));
        int builtins = list2(newSymbol("builtins"), buildEnv());
        int symbols = list2(newSymbol("symbols"), newCons());
        this.root = list3(frame, builtins, symbols);
    }

    public void prepareFirstFrame(int start) {
        int frame = newFrame(0);
        int stack = pairGet(frame, "stack");
        push(stack, start);
    }

    public void evalExpression(int start) {
        prepareFirstFrame(start);
        eval();
        while (eval()) {}
    }

    public int result() {
        int frame = pairGet(this.root, "frame");
        int values = pairGet(frame, "values");
        return pop(values);
    }

    public int newFrame(int parent) {
        int parentfr = list2(newSymbol("parentfr"), parent);
        int stack = list2(newSymbol("stack"), newCons());
        int vars = list2(newSymbol("variables"), newCons());
        int syms = list2(newSymbol("symbols"), newCons());
        int values = list2(newSymbol("values"), newCons());
        int frame = list5(parentfr, stack, vars, syms, values);
        pairSet(this.root, "frame", frame);
        return frame;
    }

    private int resolveSymbol(int table, int symbol) {
        if (!atom(symbol)) {
            return symbol;
        }
        int r = pairGet(table, atomString(symbol));
        return (r > 0) ? r : symbol;
    }

    public boolean eval() {
        int frame = pairGet(this.root, "frame");
        int stack = pairGet(frame, "stack");
        if (empty(stack)) {
            return false;
        }
        int vars = pairGet(frame, "variables");
        int syms = pairGet(frame, "symbols");
        int values = pairGet(frame, "values");
        int e = pop(stack);
        if (atom(e)) {
            int main = pairGet(this.root, "builtins");
            e = resolveSymbol(main, e);
            e = resolveSymbol(syms, e);
            e = resolveSymbol(vars, e);
        }
        if (atom(e)) {
            push(values, copy(e));
            return true;
        }
        int car = car(e);
        if (symbolEq(car, "lambda")) {
            frame = newFrame(frame);
            stack = pairGet(frame, "stack");
            vars = pairGet(frame, "variables");
            int arg = car(cdr(car));
            while (arg != 0) {
                int val = pop(values);
                pairSet(vars, atomString(arg), val);
                arg = cdr(arg);
            };
            int body = cdr(cdr(car));
            if (atom(body)) {
                push(stack, list1(newSymbol("pop-frame")));
                values = pairGet(frame, "values");
                push(values, dispatch(body, vars));
            }
            else {
                push(stack, list1(newSymbol("pop-frame")));
                push(stack, body);
            }
            return true;
        }
        if (symbolEq(car, "progn")) {
            int exp = car(reverse(cdr(car)));
            while (exp != 0) {
                push(stack, copy(exp));
                exp = cdr(exp);
            }
            return true;
        }
        else if (symbolEq(car, "pop-frame")) {
            int last_val = pop(values);
            int last_frame = pairGet(frame, "parentfr");
            if (last_frame == 0) {
                System.out.println("last_frame was zero");
                return true;
            }
            int last_values = pairGet(last_frame, "values");
            push(last_values, last_val);
            pairSet(this.root, "frame", last_frame);
            return true;
        }
        else if (symbolEq(car, "quote")) {
            e = cdr(e);
            push(values, copy(e));
            return true;
        }
        else if (symbolEq(car, "vassign")) {
            pairSet(vars, atomString(cdr(car)), cdr(cdr(car)));
            push(values, cdr(cdr(car)));
            return true;
        }
        else if (symbolEq(car, "sassign")) {
            pairSet(syms, atomString(cdr(car)), cdr(cdr(car)));
            push(values, cdr(cdr(car)));
            return true;
        }
        else if (symbolEq(car, "cond")) {
            int cond = pop(e);
            int first = pop(e);
            if (first == 0) {
                return true;
            }
            int test = car(first);
            push(e, cond);
            push(stack, e);
            push(stack, list2(newSymbol("then"), cdr(test)));
            push(stack, test);
            return true;
        }
        else if (symbolEq(car, "while")) {
            int test = cdr(e);
            int body = cdr(test);
            push(stack, e); // push original while again
            push(stack, list2(newSymbol("then"), cdr(body)));
            push(stack, test);
            return true;
        }
        else if (symbolEq(car, "then")) {
            int test = pop(values);
            if (isTrue(test)) {
                pop(stack); // remove cond
                push(stack, cdr(car));
            }
            return true;
        }
        e = car(e);
        while (e != 0) {
            push(stack, copy(e)); // possibly needs to be a deep copy
            e = cdr(e);
        }
        return true;
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

    public void dumpFrame() {
        int frame = pairGet(this.root, "frame");
        int stack = pairGet(frame, "stack");
        int values = pairGet(frame, "values");
        System.out.println("> FRAME: " + frame);
        dump(">  stack", stack);
        dump("> values", values);
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

    public int refCount(int i) {
        return this.refcount[i];
    }

    private void refObject(int id) {
        id = 0 - id;
        objrefcount[id]++;
    }

    private void derefObject(int id) {
        id = 0 - id;
        objrefcount[id]--;
        if (objrefcount[id] <= 1) {
            objrefcount[id] = 0;
            objects[id] = null;
        }
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
                derefObject(this.heap[i * 2]);
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