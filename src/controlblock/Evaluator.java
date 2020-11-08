package controlblock;

class Evaluator {
    private ConsHeap heap;
    private int stack;
    private int ENV;
    private int vals;
    
    public Evaluator(ConsHeap heap) {
        this.heap = heap;
        ENV = heap.buildEnv();
        stack = heap.newCons(); // a stack for expressions
        vals = heap.newCons();
    }

    public int result() {
        return heap.pop(vals);
    }

    public void eval(int start) {
        int env = heap.newCons();
        int frame = heap.list2(start, env);
        heap.cons(frame, stack);
        stack_eval();
        while (!heap.empty(stack)) {
            stack_eval();
        }
    }

    private int resolve_env(int env, int e) {
        int r = heap.pairGet(env, e);
        return (r > 0) ? r : e;
    }

    public void stack_eval() {
        if (heap.empty(stack)) {
            return;
        }
        int fr = heap.pop(stack);
        int e = heap.car(fr);
        int env = heap.cdr(e);
        if (heap.atom(e)) {
            e = resolve_env(env, e);
            e = resolve_env(ENV, e);
        }
        if (!heap.atom(e)) {
            int car = heap.car(e);
            if (heap.symbolEq(car, "lambda")) {
                int nenv = heap.newCons();
                int arg = heap.car(heap.cdr(car));
                do {
                    int val = heap.pop(vals);
                    heap.append(nenv, heap.list2(heap.copy(arg), val));
                    arg = heap.cdr(arg);
                } while (arg != 0);
                int body = heap.cdr(heap.cdr(car));
                if (heap.atom(body)) {
                    heap.push(vals, heap.run(body, nenv));
                }
                else {
                    heap.push(stack, heap.list2(body, nenv));
                }
                return;
            }
            else if (heap.symbolEq(car, "quote")) {
                e = heap.cdr(e);
            }
            else if (heap.symbolEq(car, "cond")) {
                int cond = heap.pop(e);
                int first = heap.pop(e);
                if (first == 0) {
                    return;
                }
                heap.push(stack, heap.list2(heap.list2(cond, e), env));
                heap.push(stack, heap.list2(heap.list2(heap.newSymbol("then"), heap.cdr(first)), env));
                heap.push(stack, heap.list2(heap.car(first), env));
                return;
            }
            else if (heap.symbolEq(car, "then")) {
                int test = heap.pop(vals);
                if (heap.isTrue(test)) {
                    heap.pop(stack); // remove cond
                    heap.push(stack, heap.list2(heap.cdr(e), env));
                }
                return;
            }
            else {
                e = heap.car(e);
                do {
                    int nfr = heap.list2(heap.copy(e), env);
                    heap.push(stack, nfr);
                    e = heap.cdr(e);
                } while (e != 0);
            }
        }
        heap.push(vals, e);
    }
}
