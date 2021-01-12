package controlblock;

class Evaluator {
    private ConsHeap heap;
    private int stack;
    private int ENV;
    private int values;
    
    public Evaluator(ConsHeap heap) {
        this.heap = heap;
        this.ENV = heap.buildEnv();
        this.stack = heap.newCons(); // a stack for expressions
        this.values = heap.newCons();
    }

    public int result() {
        return heap.pop(values);
    }

    public void prepareStack(int start) {
        int env = heap.newCons();
        int vars = heap.newCons();
        int frame = heap.list3(start, env, vars);
        heap.push(stack, frame);
    }

    public void eval(int start) {
        prepareStack(start);
        evalStep();
        while (evalStep()) {}
    }

    public void dumpAll() {
        System.out.print("stack: ");
        heap.dump(stack);
        System.out.println();
        System.out.print("values: ");
        heap.dump(values);
    }

    private int resolve_env(int env, int e) {
        int r = heap.pairGet(env, e);
        return (r > 0) ? r : e;
    }

    public boolean evalStep() {
        if (heap.empty(stack)) {
            return false;
        }
        int fr = heap.pop(stack);
        int e = heap.car(fr);
        int env = heap.cdr(e);
        int vals = heap.cdr(env);
        if (heap.atom(e)) {
            e = resolve_env(env, e);
            e = resolve_env(ENV, e);
        }
        if (!heap.atom(e)) {
            int car = heap.car(e);
            if (heap.symbolEq(car, "lambda")) {
                int nvals = heap.newCons();
                int arg = heap.car(heap.cdr(car));
                do {
                    int val = heap.pop(values);
                    heap.append(nvals, heap.list2(heap.copy(arg), val));
                    arg = heap.cdr(arg);
                } while (arg != 0);
                int body = heap.cdr(heap.cdr(car));
                if (heap.atom(body)) {
                    heap.push(values, heap.dispatch(body, nvals));
                }
                else {
                    heap.push(stack, heap.list3(body, env, nvals));
                }
                return true;
            }
            else if (heap.symbolEq(car, "bind")) {
                int nenv = heap.cdr(car);
                int body = heap.cdr(nenv);
                heap.push(stack, heap.list2(body, nenv));
                return true;
            }
            else if (heap.symbolEq(car, "quote")) {
                e = heap.cdr(e);
                heap.push(values, e);
                return true;
            }
            else if (heap.symbolEq(car, "cond")) {
                int cond = heap.pop(e);
                int first = heap.pop(e);
                if (first == 0) {
                    return true;
                }
                int test = heap.car(first);
                heap.push(e, cond);
                heap.push(stack, heap.list3(e, env, vals));
                heap.push(stack, heap.list3(heap.list2(heap.newSymbol("then"), heap.cdr(test)), env, vals));
                heap.push(stack, heap.list3(test, env, vals));
                return true;
            }
            else if (heap.symbolEq(car, "then")) {
                int test = heap.pop(values);
                if (heap.isTrue(test)) {
                    heap.pop(stack); // remove cond
                    heap.push(stack, heap.list3(heap.cdr(car), env, vals));
                }
                return true;
            }
            else {
                e = heap.car(e);
                do {
                    int nfr = heap.list2(heap.copy(e), env);
                    heap.push(stack, nfr);
                    e = heap.cdr(e);
                } while (e != 0);
                return true;
            }
        }
        heap.push(values, heap.copy(e));
        return true;
    }
}
