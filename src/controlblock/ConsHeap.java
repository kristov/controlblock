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
        prepareRoot();
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
        if (a == 0) {
            return 0;
        }
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
        if (a == 0) {
            return 0;
        }
        if (atom(a)) {
            return 0;
        }
        int head = car(a);
        if (head == 0) {
            this.heap[a * 2] = list2(sym(key), v);
            return v;
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
                return v;
            }
            head = this.heap[(head * 2) + 1];
        }
        this.heap[(head * 2) + 1] = list2(sym(key), v);
        return v;
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
        return heap[i * 2];
    }

    public int cdr(int i) {
        return heap[(i * 2) + 1];
    }

    public boolean empty(int i) {
        return heap[i * 2] == 0;
    }

    public int sym(String symbol) {
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
        if (!atom(n)) {
            return (String)objects[0];
        }
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

    private int plus_e(int frame) {
        int vars = pairGet(frame, "variables");
        String a = pairStringGet(vars, "a");
        String b = pairStringGet(vars, "b");
        Float r = Float.valueOf(a) + Float.valueOf(b);
        return sym(r.toString());
    }

    private int pset_e(int frame) {
        int vars = pairGet(frame, "variables");
        int list = pairGet(vars, "list");
        String key = pairStringGet(vars, "key");
        int value = pairGet(vars, "value");
        return pairSet(list, key, value);
    }

    private int pget_e(int frame) {
        int vars = pairGet(frame, "variables");
        int list = pairGet(vars, "list");
        String key = pairStringGet(vars, "key");
        return pairGet(list, key);
    }

    private int leta_e(int frame) {
        int vars = pairGet(frame, "variables");
        String name = pairStringGet(vars, "name");
        int value = pairGet(vars, "value");
        return pairSet(vars, name, value);
    }

    public int dispatch(int symbol, int frame) {
        String sym = atomString(symbol);
        if (sym.equals("root_e")) {
            return this.root;
        }
        if (sym.equals("plus_e")) {
            return plus_e(frame);
        }
        if (sym.equals("pset_e")) {
            return pset_e(frame);
        }
        if (sym.equals("pget_e")) {
            return pget_e(frame);
        }
        if (sym.equals("leta_e")) {
            return leta_e(frame);
        }
        return 0;
    }

    private void addBuiltin(int env, String symbol, int args, String builtin) {
        push(env, list2(sym(symbol), list3(sym("lambda"), args, sym(builtin))));
    }

    public int buildEnv() {
        int env = newCons();
        addBuiltin(env, "ROOT", newCons(), "root_e");
        addBuiltin(env, "leta", list2(sym("name"), sym("value")), "leta_e");
        addBuiltin(env, "+", list2(sym("a"), sym("b")), "plus_e");
        addBuiltin(env, "pset", list3(sym("list"), sym("key"), sym("value")), "pset_e");
        addBuiltin(env, "pget", list2(sym("list"), sym("key")), "pget_e");
        return env;
    }

    private void prepareRoot() {
        int frame = list1(sym("frame"));
        int builtins = list2(sym("builtins"), buildEnv());
        int symbols = list2(sym("symbols"), newCons());
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
        int parentfr = list2(sym("parentfr"), parent);
        int stack = list2(sym("stack"), newCons());
        int vars = list2(sym("variables"), newCons());
        int psyms = pairGet(parent, "symbols");
        if (psyms == 0) {
            psyms = newCons();
        }
        int syms = list2(sym("symbols"), psyms);
        int values = list2(sym("values"), newCons());
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
            int builtins = pairGet(this.root, "builtins");
            e = resolveSymbol(builtins, e);
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
                push(stack, list1(sym("pop-frame")));
                values = pairGet(frame, "values");
                push(values, dispatch(body, frame));
            }
            else {
                push(stack, list1(sym("pop-frame")));
                push(stack, body);
            }
            return true;
        }
        if (symbolEq(car, "define")) {
            int name = cdr(car);
            int value = cdr(name);
            pairSet(syms, atomString(name), value);
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
            int last_stack = pairGet(last_frame, "stack");
            push(last_stack, last_val);
            pairSet(this.root, "frame", last_frame);
            return true;
        }
        else if (symbolEq(car, "quote")) {
            push(values, copy(cdr(car)));
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
            push(stack, list2(sym("then"), cdr(test)));
            push(stack, test);
            return true;
        }
        else if (symbolEq(car, "while")) {
            int test = cdr(e);
            int body = cdr(test);
            push(stack, e); // push original while again
            push(stack, list2(sym("then"), cdr(body)));
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
        else if (symbolEq(car, ".dupv")) {
            push(values, copy(car(values)));
        }
        else if (symbolEq(car, ".pushs")) {
            push(stack, pop(values));
        }
        else if (symbolEq(car, ".cdr")) {
            push(values, cdr(pop(values)));
        }
        else if (symbolEq(car, ".sym")) {
            push(values, copy(cdr(car)));
        }
        else if (symbolEq(car, ".list2")) {
            int v2 = pop(values);
            int v1 = pop(values);
            int list = newCons();
            push(list, v2);
            push(list, v1);
        }
        // Poorly thought out macro system
        int macro = pairGet(pairGet(this.root, "macros"), atomString(car));
        if (macro > 0) {
            push(stack, macro);
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