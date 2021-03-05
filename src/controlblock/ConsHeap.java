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
        int scope = list1(sym("scope"));
        int builtins = list2(sym("builtins"), buildEnv());
        int symbols = list2(sym("symbols"), newCons());
        int result = list2(sym("result"), newCons());
        this.root = list4(scope, builtins, symbols, result);
        ref(this.root);
        newScope(0);
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

    public void setcar(int i, int v) {
        this.heap[i * 2] = v;
        ref(v);
    }

    public void setcdr(int i, int v) {
        this.heap[(i * 2) + 1] = v;
        ref(v);
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
        setcar(p, a);
        return p;
    }

    public int list2(int a, int b) {
        int p = newCons();
        setcar(p, a);
        setcdr(a, b);
        return p;
    }

    public int list3(int a, int b, int c) {
        int p = newCons();
        setcar(p, a);
        setcdr(a, b);
        setcdr(b, c);
        return p;
    }

    public int list4(int a, int b, int c, int d) {
        int p = newCons();
        setcar(p, a);
        setcdr(a, b);
        setcdr(b, c);
        setcdr(c, d);
        return p;
    }

    public int list5(int a, int b, int c, int d, int e) {
        int p = newCons();
        setcar(p, a);
        setcdr(a, b);
        setcdr(b, c);
        setcdr(c, d);
        setcdr(d, e);
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
        while (cdr(cons) > 0) {
            cons = cdr(cons);
        }
        setcdr(cons, item);
        return cons;
    }

    public int appendList(int list, int item) {
        if (atom(list)) {
            return 0;
        }
        if (empty(list)) {
            setcar(list, item);
            return item;
        }
        int head = car(list);
        return appendCons(head, item);
    }

    public int pairGet(int a, String key) {
        if (a == 0) {
            return 0;
        }
        if (atom(a)) {
            return 0;
        }
        int i = car(a);
        while (i > 0) {
            int pair = car(i);
            if (atomString(pair).equals(key)) {
                return cdr(pair);
            }
            i = cdr(i);
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
        if (empty(a)) {
            int pair = list2(sym(key), v);
            setcar(a, pair);
            return v;
        }
        int i = car(a);
        int last = i;
        while (i > 0) {
            int pair = car(i);
            if (atomString(pair).equals(key)) {
                int cdr_pair = cdr(pair);
                setcdr(pair, v);
                if (cdr_pair != 0) {
                    reap(cdr_pair);
                }
                return v;
            }
            last = i;
            i = cdr(i);
        }
        int pair = list2(sym(key), v);
        setcdr(last, pair);
        return v;
    }

    public void append(int a, int b) {
        if (car(a) == 0) {
            setcar(a, b);
            return;
        }
        a = car(a);
        while (cdr(a) > 0) {
            a = cdr(a);
        }
        setcdr(a, b);
    }

    public void push(int list, int item) {
        if (atom(list)) {
            return;
        }
        if (car(list) != 0) {
            // do not change to setcdr(item, car(list))
            this.heap[(item * 2) + 1] = this.heap[list * 2];
        }
        setcar(list, item);
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
            setcdr(n, prev);
            prev = n;
            i = cdr(i);
        }
        setcar(rev, prev);
        return rev;
    }

    public boolean empty(int i) {
        return car(i) == 0;
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
        int idx = addObject(symbol);
        int i = newCons();
        setcar(i, idx);
        return i;
    }

    public int obj(Object obj) {
        int idx = addObject(obj);
        int i = newCons();
        setcar(i, idx);
        return i;
    }

    public int copy(int i) {
        int n = newCons();
        int dst = car(i);
        setcar(n, dst);
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

    private int nil_e(int scope) {
        return 0;
    }

    private int cons_e(int scope) {
        int values = pairGet(scope, "values");
        int item = pop(values);
        int list = pop(values);
        push(list, item);
        return quote(list);
    }

    private int list_e(int scope) {
        return quote(newCons());
    }

    private int car_e(int scope) {
        int values = pairGet(scope, "values");
        int list = pop(values);
        if (atom(list)) {
            reap(list);
            return HALT("CAR on non-list!");
        }
        int ret = quote(copy(car(list)));
        reap(list);
        return ret;
    }

    private int cdr_e(int scope) {
        int values = pairGet(scope, "values");
        int list = pop(values);
        if (atom(list)) {
            reap(list);
            return HALT("CDR on non-list!");
        }
        int n = newCons();
        setcar(n, cdr(car(list)));
        reap(list);
        return quote(n);
    }

    private int plus_e(int scope) {
        int values = pairGet(scope, "values");
        int a = pop(values);
        int b = pop(values);
        Float r = Float.valueOf(atomString(a)) + Float.valueOf(atomString(b));
        reap(a);
        reap(b);
        return sym(r.toString());
    }

    private int minus_e(int scope) {
        int values = pairGet(scope, "values");
        int a = pop(values);
        int b = pop(values);
        Float r = Float.valueOf(atomString(a)) - Float.valueOf(atomString(b));
        reap(a);
        reap(b);
        return sym(r.toString());
    }

    private int greaterthan_e(int scope) {
        int values = pairGet(scope, "values");
        int a = pop(values);
        int b = pop(values);
        int ret = 0;
        if (Float.valueOf(atomString(a)) > Float.valueOf(atomString(b))) {
            reap(a);
            reap(b);
            return sym("true");
        }
        reap(a);
        reap(b);
        return quote(newCons());
    }

    private int pset_e(int scope) {
        int values = pairGet(scope, "values");
        int list = pop(values);
        int key = pop(values);
        int value = pop(values);
        int ret = pairSet(list, atomString(key), value);
        reap(key);
        return ret;
    }

    private int pget_e(int scope) {
        int values = pairGet(scope, "values");
        int list = pop(values);
        String key = atomString(pop(values));
        return pairGet(list, key);
    }

    private int sym_e(int scope) {
        int values = pairGet(scope, "values");
        int name = pop(values);
        int value = pop(values);
        int syms = pairGet(scope, "symbols");
        pairSet(syms, atomString(name), value);
        reap(name);
        return 0;
    }

    private int scope_e(int scope) {
        int values = pairGet(scope, "values");
        int parentfr = list2(sym("parentfr"), scope);
        int symbols = list2(sym("symbols"), pop(values));
        int variables = list2(sym("variables"), pop(values));
        int stack = list2(sym("stack"), pop(values));
        int nvalues = list2(sym("values"), newCons());
        int nscope = list5(parentfr, symbols, variables, stack, nvalues);
        pairSet(this.root, "scope", nscope);
        return 0;
    }

    private int dump_e(int scope) {
        int values = pairGet(scope, "values");
        int i = pop(values);
        dump("value", i);
        reap(i);
        return 0;
    }

    private int symbols_e(int scope) {
        int values = pairGet(scope, "values");
        int namespace = pop(values);
        int symbols = pairGet(this.root, "symbols");
        int sym = pairGet(symbols, atomString(namespace));
        if (sym == 0) {
            sym = newCons();
            pairSet(symbols, atomString(namespace), sym);
        }
        return quote(sym);
    }

    private int var_e(int scope) {
        int values = pairGet(scope, "values");
        int name = pop(values);
        int value = pop(values);
        int vars = pairGet(scope, "variables");
        int ret = pairSet(vars, atomString(name), copy(value));
        reap(value);
        reap(name);
        return quote(ret);
    }

    private int call_e(int scope) {
        int values = pairGet(scope, "values");
        int lambda = pop(values);
        int stack = newCons();
        int args = car(cdr(car(lambda)));
        push(stack, copy(lambda));
        while (args > 0) {
            int val = pop(values);
            push(stack, copy(val));
            reap(val);
            args = cdr(args);
        }
        reap(lambda);
        return list4(sym("scope"), list1(sym(".symbols")), list2(sym("quote"), newCons()), quote(stack));
    }

/*
    private int jbyte(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Byte obj = Byte.parseByte(val);
        return obj(obj);
    }

    private int jshort(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Short obj = Short.parseShort(val);
        return obj(obj);
    }

    private int jint(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        int integer = Integer.parseInt(val);
        return obj(new Integer(integer));
    }

    private int jlong(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Long obj = Long.parseLong(val);
        return obj(obj);
    }

    private int jfloat(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Float obj = Float.parseFloat(val);
        return obj(obj);
    }

    private int jdouble(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Double obj = Double.parseDouble(val);
        return obj(obj);
    }

    private int jchar(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Character obj = new Character(val.charAt(0));
        return obj(obj);
    }

    private int jboolean(int scope) {
        int values = pairGet(scope, "values");
        String val = atomString(pop(values));
        Boolean obj = Boolean.parseBoolean(val);
        return obj(obj);
    }

    private int jnew_e(int scope) {
        int values = pairGet(scope, "values");
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

    private int jmethod_e(int scope) {
        int values = pairGet(scope, "values");
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
*/

    public int dispatch(int funcsym, int scope) {
        Object thing = atomObject(funcsym);
        if (thing instanceof Method) {
            Method method = (Method)thing;
            try {
                return (int)method.invoke(this, scope);
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
        addBuiltin(env, "cons", list2(sym("item"), sym("list")), "cons_e");
        addBuiltin(env, "list", newCons(), "list_e");
        addBuiltin(env, "car", list1(sym("list")), "car_e");
        addBuiltin(env, "cdr", list1(sym("list")), "cdr_e");
        addBuiltin(env, "var", list2(sym("name"), sym("value")), "var_e");
        addBuiltin(env, "call", list1(sym("lambda")), "call_e");
        addBuiltin(env, "+", list2(sym("a"), sym("b")), "plus_e");
        addBuiltin(env, ">", list2(sym("a"), sym("b")), "greaterthan_e");
        addBuiltin(env, "-", list2(sym("a"), sym("b")), "minus_e");
        addBuiltin(env, "pset", list3(sym("list"), sym("key"), sym("value")), "pset_e");
        addBuiltin(env, "pget", list2(sym("list"), sym("key")), "pget_e");
        addBuiltin(env, "sym", list2(sym("name"), sym("value")), "sym_e");
        addBuiltin(env, "symbols", list1(sym("namespace")), "symbols_e");
        addBuiltin(env, "scope", list3(sym("symbols"), sym("variables"), sym("stack")), "scope_e");
        addBuiltin(env, "dump", list1(sym("object")), "dump_e");
/*
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
*/
        return env;
    }

    public void pushStack(int e) {
        int scope = getCurrentScope();
        int stack = pairGet(scope, "stack");
        push(stack, e);
    }

    public void evalLoop() {
        while (eval()) {}
    }

    public void evalExpression(int start) {
        pushStack(start);
        evalLoop();
    }

    public int getCurrentScope() {
        return pairGet(this.root, "scope");
    }

    public int getValues() {
        return pairGet(getCurrentScope(), "values");
    }

    public int getStack() {
        return pairGet(getCurrentScope(), "stack");
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
        return pairGet(this.root, "result");
    }

    public int newScope(int parent) {
        int parentfr = list2(sym("parentfr"), parent);
        int stack = list2(sym("stack"), newCons());
        int vars = list2(sym("variables"), newCons());
        int psyms = pairGet(parent, "symbols");
        if (psyms == 0) {
            psyms = newCons();
        }
        int syms = list2(sym("symbols"), psyms);
        int values = list2(sym("values"), newCons());
        int scope = list5(parentfr, stack, vars, syms, values);
        pairSet(this.root, "scope", scope);
        return scope;
    }

    private int resolveSymbol(int table, int symbol) {
        if (!atom(symbol)) {
            return symbol;
        }
        int r = pairGet(table, atomString(symbol));
        if (r > 0) {
            reap(symbol);
            ref(r);
            return r;
        }
        return symbol;
    }

    private boolean popScope(int scope) {
        int values = pairGet(scope, "values");
        int result = pop(values);
        int last_scope = pairGet(scope, "parentfr");
        if (last_scope == 0) {
            pairSet(this.root, "result", result);
            return false;
        }
        int last_stack = pairGet(last_scope, "stack");
        push(last_stack, result);
        pairSet(this.root, "scope", last_scope);
        reap(scope);
        return true;
    }

    public boolean eval() {
        int scope = pairGet(this.root, "scope");
        int stack = pairGet(scope, "stack");
        if (empty(stack)) {
            return popScope(scope);
        }
        int vars = pairGet(scope, "variables");
        int syms = pairGet(scope, "symbols");
        int values = pairGet(scope, "values");
        int e = pop(stack);
        if (atom(e)) {
            int builtins = pairGet(this.root, "builtins");
            e = resolveSymbol(builtins, e);
            e = resolveSymbol(syms, e);
            e = resolveSymbol(vars, e);
        }
        if (atom(e)) {
            push(values, copy(e));
            reap(e);
            return true;
        }
        int car = car(e);
        if (symbolEq(car, "lambda")) {
            int body = cdr(cdr(car));
            if (atom(body)) {
                int val = dispatch(body, scope);
                if (val > 0) {
                    push(stack, val);
                }
                reap(e);
                return true;
            }
            scope = newScope(scope);
            stack = pairGet(scope, "stack");
            vars = pairGet(scope, "variables");
            int arg = car(cdr(car));
            while (arg != 0) {
                int val = pop(values);
                pairSet(vars, atomString(arg), val);
                arg = cdr(arg);
            }
            push(stack, copy(body));
            reap(e);
            return true;
        }
        if (symbolEq(car, "HALT")) {
            push(stack, e);
            return false;
        }
        if (symbolEq(car, "progn")) {
            int rev = reverse(cdr(car));
            int i = car(rev);
            while (i != 0) {
                push(stack, copy(i));
                i = cdr(i);
            }
            reap(rev);
            reap(e);
            return true;
        }
        else if (symbolEq(car, "quote")) {
            push(values, copy(cdr(car)));
            reap(e);
            return true;
        }
        else if (symbolEq(car, "cond")) {
            int cond = pop(e); // wrong?
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
        else if (symbolEq(car, ".scope")) {
            push(values, copy(scope));
            return true;
        }
        int i = car(e);
        while (i != 0) {
            push(stack, copy(i));
            i = cdr(i);
        }
        reap(e);
        return true;
    }

    public void markCons(int i) {
        while (i > 0) {
            flag.set(i);
            if (!atom(i)) {
                markCons(car(i));
            }
            i = cdr(i);
        }
    }

    public int nrUsedCons() {
        int inuse = 0;
        for (int i = 1; i < heap_size; i++) {
            //if ((car(i) != 0) || (cdr(i) != 0)) {
            if (refcount[i] > 0) {
                inuse++;
            }
        }
        return inuse;
    }

    public int nrReachableCons() {
        flag.clear();
        markCons(this.root);
        int marked = 0;
        for (int i = 1; i < heap_size; i++) {
            if (flag.get(i)) {
                marked++;
            }
        }
        return marked;
    }

    public void printOrphanedMagic() {
        flag.clear();
        markCons(this.root);
        for (int i = 1; i < heap_size; i++) {
            if ((refcount[i] <= 0) && flag.get(i)) {
                dumpSys(i);
            }
        }
    }

    public void printOrphaned() {
        flag.clear();
        markCons(this.root);
        for (int i = 1; i < heap_size; i++) {
            if (((car(i) != 0) || (cdr(i) != 0)) && !flag.get(i)) {
                dumpSys(i);
            }
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

    public void dumpIndent(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }
    }

    public void dumpConsCell(int indent, int i) {
        if (atom(i)) {
            System.out.print(i + "[\"" + atomString(i) + "\":" + cdr(i) + ":R" + refcount[i] + "]");
            return;
        }
        System.out.print(i + "[L" + length(i) + ":" + cdr(i) + ":R" + refcount[i] + "](\n");
        dumpIndent(indent + 1);
        dumpConsSys(indent + 1, car(i));
        dumpIndent(indent);
        System.out.print(")");
    }

    public void dumpConsSys(int indent, int i) {
        while (i > 0) {
            dumpConsCell(indent, i);
            if (cdr(i) > 0) {
                System.out.print(".");
            }
            else {
                System.out.print("\n");
            }
            i = cdr(i);
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

    public void dumpScope() {
        int scope = pairGet(this.root, "scope");
        int stack = pairGet(scope, "stack");
        int vals = pairGet(scope, "values");
        int vars = pairGet(scope, "variables");
        System.out.println("> SCOPE: " + scope);
        dump(">  stack", stack);
        dump("> values", vals);
        dump(">   vars", vars);
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

    public int refObjectCount(int i) {
        return this.objrefcount[0 - i];
    }

    private void ref(int id) {
        if (id < 0) {
            objrefcount[0 - id]++;
            return;
        }
        refcount[id]++;
    }

    private void reapObject(int id) {
        id = 0 - id;
        objrefcount[id]--;
        if (objrefcount[id] <= 1) {
            objrefcount[id] = 0;
            objects[id] = null;
        }
    }

    public void reap(int i) {
        while (i > 0) {
            this.refcount[i]--;
            if (this.refcount[i] > 1) {
                return;
            }
            int j = this.heap[(i * 2) + 1];
            this.refcount[i] = 0;
            if (this.heap[i * 2] < 0) {
                reapObject(this.heap[i * 2]);
            }
            if (this.heap[i * 2] > 0) {
                reap(this.heap[i * 2]);
            }
            this.heap[i * 2] = 0;
            this.heap[(i * 2) + 1] = 0;
            i = j;
        }
    }
}