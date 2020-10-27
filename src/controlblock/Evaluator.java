package controlblock;

class Evaluator {
    private ConsHeap heap;
    private int stack;
    private int env;
    
    public Evaluator(ConsHeap heap) {
        this.heap = heap;
        this.env = heap.list(); // where user-defined functions will go
        this.stack = heap.list(); // a stack for expressions
        int stacke = heap.list2(heap.newSymbol("$STACK"), stack);
        heap.append(env, stacke);
    }

    public void eval(int start) {
        int frame = heap.list2(start, env);
        heap.cons(frame, stack);
        while (!heap.empty(stack)) {
            step();
        }
    }

    public void step() {
        if (heap.empty(stack)) {
            return;
        }
        int frame = heap.car(stack);
        int e = heap.car(frame);
        int env = heap.cdr(frame);
        if (heap.atom(e) == e) {
            // atoms do not require eval
            // push onto frame args and check if frame complete
            return;
        }
        int e1 = heap.car(e);
        if (heap.atom(e1) == e1) {
            String symbol = heap.atomString(e1);
            // first item in expression is an atom
            if (symbol.equals("quote")) {
                return;
            }
            if (symbol.equals("cond")) {
                return;
            }
            // it's a "normal" form. Prepare to eval by pushing a frame
            
        }
    }
}
