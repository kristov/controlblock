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
        int MAIN = this.heap.buildEnv();
        int menv = this.heap.list2(this.heap.newSymbol("MAIN"), MAIN);
        int symbols = this.heap.list2(this.heap.newSymbol("symbols"), this.heap.list1(menv));
        this.heap.push(this.root, symbols);
        this.heap.push(this.root, frames);
        this.heap.push(this.root, frame);
    }

    public int newFrame() {
        int stack = this.heap.list2(this.heap.newSymbol("stack"), this.heap.newCons());
        int vars = this.heap.list2(this.heap.newSymbol("variables"), this.heap.newCons());
        int values = this.heap.list2(this.heap.newSymbol("values"), this.heap.newCons());
        int frame = this.heap.list3(stack, vars, values);
        int curr_frame = this.heap.pairGet(this.root, "frame");
        if (curr_frame > 0) {
            int frames = this.heap.pairGet(this.root, "frames");
            this.heap.push(frames, curr_frame);
        }
        this.heap.pairSet(this.root, "frame", frame);
        return frame;
    }

    public int result() {
        int frames = this.heap.pairGet(this.root, "frames");
        int frame = this.heap.pop(frames);
        int values = this.heap.pairGet(frame, "values");
        return heap.pop(values);
    }

    public void prepareFirstFrame(int start) {
        int frame = newFrame();
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
        int values = this.heap.pairGet(frame, "values");
        int e = this.heap.pop(stack);
        if (heap.atom(e)) {
            int symbols = this.heap.pairGet(this.root, "symbols");
            int main = this.heap.pairGet(symbols, "MAIN");
            e = resolveSymbol(main, e);
            e = resolveSymbol(vars, e);
        }
        if (!heap.atom(e)) {
            int car = this.heap.car(e);
            if (this.heap.symbolEq(car, "lambda")) {
                frame = newFrame();
                stack = this.heap.pairGet(frame, "stack");
                int nvars = heap.pairGet(frame, "variables");
                int arg = heap.car(heap.cdr(car));
                while (arg != 0) {
                    int val = heap.pop(values);
                    heap.pairSet(nvars, heap.atomString(arg), val);
                    arg = heap.cdr(arg);
                };
                int body = heap.cdr(heap.cdr(car));
                if (heap.atom(body)) {
                    // this only works because "values" here is from the prev
                    // frame, not newFrame above. This should properly inject a
                    // "return" form which should pop the last value from the
                    // current frame, restore the last frame and push the value
                    // there.
                    heap.push(values, heap.dispatch(body, nvars));
                }
                else {
                    heap.push(stack, body);
                }
                return true;
            }
            else if (heap.symbolEq(car, "quote")) {
                e = heap.cdr(e);
                heap.push(values, e);
                return true;
            }
            else if (heap.symbolEq(car, "setq")) {
                // (set (quote *foo*) 42)
            }
            else if (heap.symbolEq(car, "cond")) {
                int cond = heap.pop(e);
                int first = heap.pop(e);
                if (first == 0) {
                    return true;
                }
                int test = heap.car(first);
                heap.push(e, cond);
                heap.push(stack, e);
                heap.push(stack, heap.list2(heap.newSymbol("then"), heap.cdr(test)));
                heap.push(stack, test);
                return true;
            }
            else if (heap.symbolEq(car, "while")) {
                int test = heap.cdr(e);
                int body = heap.cdr(test);
                heap.push(stack, e); // push original while again
                heap.push(stack, heap.list2(heap.newSymbol("then"), heap.cdr(body)));
                heap.push(stack, test);
                return true;
            }
            else if (heap.symbolEq(car, "then")) {
                int test = heap.pop(values);
                if (heap.isTrue(test)) {
                    heap.pop(stack); // remove cond
                    heap.push(stack, heap.cdr(car));
                }
                return true;
            }
            else {
                e = heap.car(e);
                while (e != 0) {
                    heap.push(stack, heap.copy(e));
                    e = heap.cdr(e);
                }
                return true;
            }
        }
        heap.push(values, heap.copy(e));
        return true;
    }

    public void dumpAll() {
        System.out.print("root: ");
        heap.dump(this.root);
        System.out.println();
    }
}
