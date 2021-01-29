package controlblock;

class Evaluator {
    private ConsHeap heap;
    private int root;
    
    public Evaluator(ConsHeap heap) {
        this.heap = heap;
        prepareHeap();
    }

    private void prepareHeap() {
        this.root = this.heap.newCons();
        int frame = this.heap.list1(this.heap.newSymbol("frame"));
        int frames = this.heap.list2(this.heap.newSymbol("frames"), this.heap.newCons());
        int main = this.heap.buildEnv();
        int menv = this.heap.list2(this.heap.newSymbol("main"), main);
        int symbols = this.heap.list2(this.heap.newSymbol("symbols"), this.heap.list1(menv));
        this.heap.push(this.root, symbols);
        this.heap.push(this.root, frames);
        this.heap.push(this.root, frame);
    }

    public int newFrame(int parent) {
        int parentfr = this.heap.list2(this.heap.newSymbol("parentfr"), parent);
        int stack = this.heap.list2(this.heap.newSymbol("stack"), this.heap.newCons());
        int vars = this.heap.list2(this.heap.newSymbol("variables"), this.heap.newCons());
        int syms = this.heap.list2(this.heap.newSymbol("symbols"), this.heap.newCons());
        int values = this.heap.list2(this.heap.newSymbol("values"), this.heap.newCons());
        int frame = this.heap.list5(parentfr, stack, vars, syms, values);
        this.heap.pairSet(this.root, "frame", frame);
        return frame;
    }

    public int result() {
        int frame = this.heap.pairGet(this.root, "frame");
        int values = this.heap.pairGet(frame, "values");
        return heap.pop(values);
    }

    public void prepareFirstFrame(int start) {
        int frame = newFrame(0);
        int stack = this.heap.pairGet(frame, "stack");
        this.heap.push(stack, start);
    }

    public void eval(int start) {
        prepareFirstFrame(start);
        evalStep();
        while (evalStep()) {}
    }

    private int resolveSymbol(int table, int symbol) {
        if (!this.heap.atom(symbol)) {
            return symbol;
        }
        int r = this.heap.pairGet(table, this.heap.atomString(symbol));
        return (r > 0) ? r : symbol;
    }

    public boolean evalStep() {
        int frame = this.heap.pairGet(this.root, "frame");
        int stack = this.heap.pairGet(frame, "stack");
        if (this.heap.empty(stack)) {
            return false;
        }
        int vars = this.heap.pairGet(frame, "variables");
        int syms = this.heap.pairGet(frame, "symbols");
        int values = this.heap.pairGet(frame, "values");
        int e = this.heap.pop(stack);
        if (this.heap.atom(e)) {
            int symbols = this.heap.pairGet(this.root, "symbols");
            int main = this.heap.pairGet(symbols, "main");
            e = resolveSymbol(main, e);
            e = resolveSymbol(syms, e);
            e = resolveSymbol(vars, e);
        }
        if (this.heap.atom(e)) {
            this.heap.push(values, this.heap.copy(e));
            return true;
        }
        int car = this.heap.car(e);
        if (this.heap.symbolEq(car, "lambda")) {
            frame = newFrame(frame);
            stack = this.heap.pairGet(frame, "stack");
            vars = this.heap.pairGet(frame, "variables");
            int arg = this.heap.car(this.heap.cdr(car));
            while (arg != 0) {
                int val = this.heap.pop(values);
                this.heap.pairSet(vars, this.heap.atomString(arg), val);
                arg = this.heap.cdr(arg);
            };
            int body = this.heap.cdr(this.heap.cdr(car));
            if (this.heap.atom(body)) {
                this.heap.push(stack, this.heap.list1(this.heap.newSymbol("pop-frame")));
                values = this.heap.pairGet(frame, "values");
                this.heap.push(values, this.heap.dispatch(body, vars));
            }
            else {
                this.heap.push(stack, this.heap.list1(this.heap.newSymbol("pop-frame")));
                this.heap.push(stack, body);
            }
            return true;
        }
        if (this.heap.symbolEq(car, "progn")) {
            int exp = this.heap.car(this.heap.reverse(this.heap.cdr(car)));
            while (exp != 0) {
                this.heap.push(stack, this.heap.copy(exp));
                exp = this.heap.cdr(exp);
            }
            return true;
        }
        else if (this.heap.symbolEq(car, "pop-frame")) {
            int last_val = this.heap.pop(values);
            int last_frame = this.heap.pairGet(frame, "parentfr");
            if (last_frame == 0) {
                System.out.println("last_frame was zero");
                return true;
            }
            int last_values = this.heap.pairGet(last_frame, "values");
            this.heap.push(last_values, last_val);
            this.heap.pairSet(this.root, "frame", last_frame);
            return true;
        }
        else if (this.heap.symbolEq(car, "quote")) {
            e = this.heap.cdr(e);
            this.heap.push(values, e);
            return true;
        }
        else if (this.heap.symbolEq(car, "vassign")) {
            this.heap.pairSet(vars, this.heap.atomString(this.heap.cdr(car)), this.heap.cdr(this.heap.cdr(car)));
            this.heap.push(values, this.heap.cdr(this.heap.cdr(car)));
            return true;
        }
        else if (this.heap.symbolEq(car, "sassign")) {
            this.heap.pairSet(syms, this.heap.atomString(this.heap.cdr(car)), this.heap.cdr(this.heap.cdr(car)));
            this.heap.push(values, this.heap.cdr(this.heap.cdr(car)));
            return true;
        }
        else if (this.heap.symbolEq(car, "cond")) {
            int cond = this.heap.pop(e);
            int first = this.heap.pop(e);
            if (first == 0) {
                return true;
            }
            int test = this.heap.car(first);
            this.heap.push(e, cond);
            this.heap.push(stack, e);
            this.heap.push(stack, this.heap.list2(this.heap.newSymbol("then"), this.heap.cdr(test)));
            this.heap.push(stack, test);
            return true;
        }
        else if (this.heap.symbolEq(car, "while")) {
            int test = this.heap.cdr(e);
            int body = this.heap.cdr(test);
            this.heap.push(stack, e); // push original while again
            this.heap.push(stack, this.heap.list2(this.heap.newSymbol("then"), this.heap.cdr(body)));
            this.heap.push(stack, test);
            return true;
        }
        else if (this.heap.symbolEq(car, "then")) {
            int test = this.heap.pop(values);
            if (this.heap.isTrue(test)) {
                this.heap.pop(stack); // remove cond
                this.heap.push(stack, this.heap.cdr(car));
            }
            return true;
        }
        e = this.heap.car(e);
        while (e != 0) {
            this.heap.push(stack, this.heap.copy(e));
            e = this.heap.cdr(e);
        }
        return true;
    }

    public void dumpAll() {
        int frame = this.heap.pairGet(this.root, "frame");
        heap.dump("frame", frame);
        int stack = this.heap.pairGet(frame, "stack");
        heap.dump("stack", stack);
        int values = this.heap.pairGet(frame, "values");
        heap.dump("values", values);
    }
}
