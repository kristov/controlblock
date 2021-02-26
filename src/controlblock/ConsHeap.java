package controlblock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* The heap is an array of ints nrCons * 2 in size. A cell is identified by an
 * id int. The location of the cell in the array is id * 2. Int 1 of the cell is
 * the "car". If the car is less than 0 it's the id of a string. If it's greater
 * than 0 it's an id of another cell - it's a list. Int 2 of the cell is the
 * "cdr". It is an id of the next cell. If zero there are no more cells in this
 * cons list.
 */
public class ConsHeap {
    private int root;
    private int result;
    private final int heap_size;
    private final int[] heap;
    private final byte[] refcount;
    private Object[] objects;
    private final byte[] objrefcount;
    private final Flag flag;

    public ConsHeap(int nrCons) {
        heap = new int[nrCons * 2];
        refcount = new byte[nrCons];
        refcount[0] = 2;
        heap_size = nrCons;
        objects = new Object[nrCons];
        objects[0] = "NIL";
        objrefcount = new byte[nrCons];
        objrefcount[0] = 2;
        flag = new Flag(nrCons);
        prepareRoot();
    }

    private void prepareRoot() {
        int frame = list1(sym("frame"));
        int builtins = list2(sym("builtins"), buildEnv());
        int symbols = list2(sym("symbols"), newCons());
        this.root = list3(frame, builtins, symbols);
        newFrame(0);
    }

    public boolean atom(int i) {
        return heap[i * 2] < 0;
    }

    public int car(int i) {
        return heap[i * 2];
    }

    public int cdr(int i) {
        return heap[(i * 2) + 1];
    }

    public int cons(int car, int cdr) {
        int c = newCons();
        heap[c * 2] = car;
        heap[(c * 2) + 1] = cdr;
        if (car > 0) {
            refcount[car]++;
        }
        if (cdr > 0) {
            refcount[cdr]++;
        }
        return c;
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
        if (length(n) == 0) {
            return false;
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

    public boolean empty(int i) {
        return heap[i * 2] == 0;
    }

    public int length(int i) {
        i = car(i);
        int len = 0;
        while (i > 0) {
            len++;
            i = cdr(i);
        }
        return len;
    }

    public int sym(String symbol) {
        int strIdx = addObject(symbol);
        int i = newCons();
        heap[(i * 2)] = strIdx;
        return i;
    }

    public int obj(Object obj) {
        int idx = addObject(obj);
        int i = newCons();
        heap[(i * 2)] = idx;
        return i;
    }

    public int copy(int i) {
        int n = newCons();
        int dst = car(i);
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
        Object thing = atomObject(n);
        if (thing instanceof String) {
            return (String)thing;
        }
        return thing.toString();
    }

    public Object atomObject(int n) {
        if (!atom(n)) {
            return (String)objects[0];
        }
        return objects[0 - heap[n * 2]];
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

    private int HALT(String reason) {
        return list2(sym("HALT"), sym(reason));
    }

    private int quote(int i) {
        return list2(sym("quote"), i);
    }

    private int nil_e(int frame) {
        return 0;
    }

    private int cons_e(int frame) {
        int values = pairGet(frame, "values");
        int car = pop(values);
        int cdr = pop(values);
        int c = cons(car(car), copy(cdr));
        return c;
    }

    private int list_e(int frame) {
        int values = pairGet(frame, "values");
        int car = pop(values);
        int list = newCons();
        push(list, car);
        return quote(list);
    }

    private int plus_e(int frame) {
        int values = pairGet(frame, "values");
        String a = atomString(pop(values));
        String b = atomString(pop(values));
        Float r = Float.valueOf(a) + Float.valueOf(b);
        return sym(r.toString());
    }

    private int minus_e(int frame) {
        int values = pairGet(frame, "values");
        String a = atomString(pop(values));
        String b = atomString(pop(values));
        Float r = Float.valueOf(a) - Float.valueOf(b);
        return sym(r.toString());
    }

    private int greaterthan_e(int frame) {
        int values = pairGet(frame, "values");
        String a = atomString(pop(values));
        String b = atomString(pop(values));
        Float r = Float.valueOf(a) + Float.valueOf(b);
        if (Float.valueOf(a) > Float.valueOf(b)) {
            return sym("true");
        }
        return quote(newCons());
    }

    private int pset_e(int frame) {
        int values = pairGet(frame, "values");
        int list = pop(values);
        String key = atomString(pop(values));
        int value = pop(values);
        return pairSet(list, key, value);
    }

    private int pget_e(int frame) {
        int values = pairGet(frame, "values");
        int list = pop(values);
        String key = atomString(pop(values));
        return pairGet(list, key);
    }

    private int symbol_e(int frame) {
        int values = pairGet(frame, "values");
        int name = pop(values);
        int value = pop(values);
        int syms = pairGet(frame, "symbols");
        pairSet(syms, atomString(name), value);
        return 0;
    }

    private int frame_e(int frame) {
        int values = pairGet(frame, "values");
        int parentfr = list2(sym("parentfr"), frame);
        int symbols = list2(sym("symbols"), pop(values));
        int variables = list2(sym("variables"), pop(values));
        int stack = list2(sym("stack"), pop(values));
        int nvalues = list2(sym("values"), newCons());
        int nframe = list5(parentfr, symbols, variables, stack, nvalues);
        pairSet(this.root, "frame", nframe);
        return 0;
    }

    private int dump_e(int frame) {
        int values = pairGet(frame, "values");
        int i = pop(values);
        dump("value", values);
        return 0;
    }

    private int symbols_e(int frame) {
        int values = pairGet(frame, "values");
        int namespace = pop(values);
        int symbols = pairGet(this.root, "symbols");
        int sym = pairGet(symbols, atomString(namespace));
        if (sym == 0) {
            sym = newCons();
            pairSet(symbols, atomString(namespace), sym);
        }
        return quote(sym);
    }

    private int leta_e(int frame) {
        int values = pairGet(frame, "values");
        String name = atomString(pop(values));
        int value = pop(values);
        int vars = pairGet(frame, "variables");
        int ret = pairSet(vars, name, value);
        return quote(ret);
    }

    private int jbyte(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Byte obj = Byte.parseByte(val);
        return obj(obj);
    }

    private int jshort(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Short obj = Short.parseShort(val);
        return obj(obj);
    }

    private int jint(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        int integer = Integer.parseInt(val);
        return obj(new Integer(integer));
    }

    private int jlong(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Long obj = Long.parseLong(val);
        return obj(obj);
    }

    private int jfloat(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Float obj = Float.parseFloat(val);
        return obj(obj);
    }

    private int jdouble(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Double obj = Double.parseDouble(val);
        return obj(obj);
    }

    private int jchar(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Character obj = new Character(val.charAt(0));
        return obj(obj);
    }

    private int jboolean(int frame) {
        int values = pairGet(frame, "values");
        String val = atomString(pop(values));
        Boolean obj = Boolean.parseBoolean(val);
        return obj(obj);
    }

    private int jnew_e(int frame) {
        int values = pairGet(frame, "values");
        String clstr = atomString(pop(values));
        Class cl;
        try {
            cl = Class.forName(clstr);
        }
        catch (ClassNotFoundException e) { System.out.println(e.toString()); return 0; }
        String argcs = atomString(pop(values));
        int argc = Integer.parseInt(argcs);
        Object[] args = new Object[argc];
        Class[] types = new Class[argc];
        for (int i = 0; i < argc; i++) {
            int arg = pop(values);
            Object obj = atomObject(arg);
            args[i] = obj;
            types[i] = obj.getClass();
        }
        Constructor con;
        try {
            con = cl.getConstructor(types);
        }
        catch (NoSuchMethodException e) { return HALT(e.toString()); }
        catch (SecurityException e) { return HALT(e.toString()); }
        Object object;
        try {
            object = con.newInstance(args);
        }
        catch (InstantiationException e) { return HALT(e.toString()); }
        catch (IllegalAccessException e) { return HALT(e.toString()); }
        catch (IllegalArgumentException e) { return HALT(e.toString()); }
        catch (InvocationTargetException e) { return HALT(e.toString()); }
        return obj(object);
    }

    private int jmethod_e(int frame) {
        int values = pairGet(frame, "values");
        String sym = atomString(pop(values));
        String argcs = atomString(pop(values));
        int argc = Integer.parseInt(argcs);
        Object[] args = new Object[argc];
        Class[] types = new Class[argc];
        for (int i = 0; i < argc; i++) {
            int arg = pop(values);
            Object obj = atomObject(arg);
            args[i] = obj;
            types[i] = obj.getClass();
        }
        Method method;
        try {
            method = this.getClass().getMethod(sym, types);
        }
        catch (NoSuchMethodException e) { return HALT(e.toString()); }
        catch (SecurityException e) { return HALT(e.toString()); }
        return 0;
    }

    public int dispatch(int funcsym, int frame) {
        Object thing = atomObject(funcsym);
        if (thing instanceof Method) {
            Method method = (Method)thing;
            try {
                return (int)method.invoke(this, frame);
            }
            catch (InvocationTargetException e) { return HALT(e.toString()); }
            catch (IllegalAccessException e) { return HALT(e.toString()); }
        }
        System.out.println("thing was not an instance of a method!");
        return 0;
    }

    private Method getMethod(String sym) {
        Class[] args = new Class[1];
        args[0] = int.class;
        try {
            return this.getClass().getDeclaredMethod(sym, args);
        }
        catch (NoSuchMethodException e) { System.out.println(e.toString()); return null; }
        catch (SecurityException e) { System.out.println(e.toString()); return null; }
    }

    private void addBuiltin(int env, String symbol, int args, String builtin) {
        Method method = getMethod(builtin);
        if (method == null) {
            return;
        }
        push(env, list2(sym(symbol), list3(sym("lambda"), args, obj(method))));
    }

    public int buildEnv() {
        int env = newCons();
        addBuiltin(env, "NIL", newCons(), "nil_e");
        addBuiltin(env, "cons", newCons(), "cons_e");
        addBuiltin(env, "list", list1(sym("car")), "list_e");
        addBuiltin(env, "leta", list2(sym("name"), sym("value")), "leta_e");
        addBuiltin(env, "+", list2(sym("a"), sym("b")), "plus_e");
        addBuiltin(env, ">", list2(sym("a"), sym("b")), "greaterthan_e");
        addBuiltin(env, "-", list2(sym("a"), sym("b")), "minus_e");
        addBuiltin(env, "pset", list3(sym("list"), sym("key"), sym("value")), "pset_e");
        addBuiltin(env, "pget", list2(sym("list"), sym("key")), "pget_e");
        addBuiltin(env, "symbol", list2(sym("name"), sym("value")), "symbol_e");
        addBuiltin(env, "symbols", list1(sym("namespace")), "symbols_e");
        addBuiltin(env, "frame", list3(sym("symbols"), sym("variables"), sym("stack")), "frame_e");
        addBuiltin(env, "dump", list1(sym("object")), "dump_e");
        addBuiltin(env, "jbyte", list1(sym("byte")), "jbyte");
        addBuiltin(env, "jshort", list1(sym("short")), "jshort");
        addBuiltin(env, "jint", list1(sym("integer")), "jint");
        addBuiltin(env, "jlong", list1(sym("long")), "jlong");
        addBuiltin(env, "jfloat", list1(sym("float")), "jfloat");
        addBuiltin(env, "jdouble", list1(sym("double")), "jdouble");
        addBuiltin(env, "jchar", list1(sym("char")), "jchar");
        addBuiltin(env, "jboolean", list1(sym("boolean")), "jboolean");
        addBuiltin(env, "jnew", list3(sym("method"), sym("argc"), sym("args")), "jnew_e");
        addBuiltin(env, "jmethod", list3(sym("method"), sym("argc"), sym("args")), "jmethod_e");
        return env;
    }

    public void pushStack(int e) {
        int frame = getCurrentFrame();
        int stack = pairGet(frame, "stack");
        push(stack, e);
    }

    public void evalLoop() {
        while (eval()) {}
    }

    public void evalExpression(int start) {
        pushStack(start);
        evalLoop();
    }

    public int getCurrentFrame() {
        return pairGet(this.root, "frame");
    }

    public int getValues() {
        return pairGet(getCurrentFrame(), "values");
    }

    public int getStack() {
        return pairGet(getCurrentFrame(), "stack");
    }

    public int getRoot() {
        return this.root;
    }

    public int getBuiltins() {
        return pairGet(this.root, "builtins");
    }

    public int getSymbol(int table, String symbol) {
        return pairGet(table, symbol);
    }

    public int result() {
        return this.result;
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

    private boolean popFrame(int frame) {
        int values = pairGet(frame, "values");
        int last_val = pop(values);
        int last_frame = pairGet(frame, "parentfr");
        if (last_frame == 0) {
            this.result = last_val;
            return false;
        }
        int last_stack = pairGet(last_frame, "stack");
        push(last_stack, last_val);
        pairSet(this.root, "frame", last_frame);
        return true;
    }

    public boolean eval() {
        int frame = pairGet(this.root, "frame");
        int stack = pairGet(frame, "stack");
        if (empty(stack)) {
            return popFrame(frame);
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
            int body = cdr(cdr(car));
            if (atom(body)) {
                int val = dispatch(body, frame);
                if (val > 0) {
                    push(stack, val);
                }
                return true;
            }
            frame = newFrame(frame);
            stack = pairGet(frame, "stack");
            vars = pairGet(frame, "variables");
            int arg = car(cdr(car));
            while (arg != 0) {
                int val = pop(values);
                pairSet(vars, atomString(arg), val);
                arg = cdr(arg);
            }
            push(stack, copy(body));
            return true;
        }
        if (symbolEq(car, "HALT")) {
            push(stack, e);
            return false;
        }
        if (symbolEq(car, "progn")) {
            int exp = car(reverse(cdr(car)));
            while (exp != 0) {
                push(stack, copy(exp));
                exp = cdr(exp);
            }
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
            int test = cdr(car);
            int body = cdr(test);
            push(stack, copy(e)); // push original while again
            push(stack, list2(sym("then"), copy(body)));
            push(stack, copy(test));
            return true;
        }
        else if (symbolEq(car, "then")) {
            int test = pop(values);
            if (isTrue(test)) {
                pop(stack); // remove cond
                push(stack, copy(cdr(car)));
            }
            return true;
        }
        else if (symbolEq(car, ".variables")) {
            push(values, copy(vars));
            return true;
        }
        else if (symbolEq(car, ".symbols")) {
            push(values, copy(syms));
            return true;
        }
        else if (symbolEq(car, ".frame")) {
            push(values, copy(frame));
            return true;
        }
        e = car(e);
        while (e != 0) {
            push(stack, copy(e)); // possibly needs to be a deep copy
            e = cdr(e);
        }
        return true;
    }

    public void markCons(int i) {
        flag.set(i);
        int n = i;
        while (n > 0) {
            if (atom(n)) {
                flag.set(n);
            }
            else {
                markCons(heap[n * 2]);
            }
            n = heap[(n * 2) + 1];
        }
    }

    public float GCReport() {
        flag.clear();
        markCons(this.root);
        int unmarked = 0;
        int inuse = 0;
        for (int i = 0; i < heap_size; i++) {
            if ((heap[i * 2] != 0) || (heap[(i * 2) + 1] != 0)) {
                inuse++;
            }
            if (flag.get(i)) {
                continue;
            }
            unmarked++;
        }
        int extra = inuse - unmarked;
        if (extra == 0) {
            return 0;
        }
        float pct = ((float)extra / (float)unmarked) * 100;
        return pct;
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
        if (flag.get(i)) {
            System.out.print("!");
            return;
        }
        flag.set(i);
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
        flag.clear();
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