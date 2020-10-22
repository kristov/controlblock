package controlblock;

class Evaluator {
    private ConsHeap heap;
    private int stack;
    private int env;
    
    public Evaluator(ConsHeap pHeap, int pStack, int pEnv) {
        heap = pHeap;
        stack = pStack;
        env = pEnv;
    }

    public void eval(int start) {
        int frame = heap.pair(start, env);
        heap.append(stack, frame);
        while (!heap.empty(stack)) {
            step();
        }
    }

    public void step() {
        if (heap.empty(stack)) {
            return;
        }
        int frame = heap.pop(stack);
        int e = heap.car(frame);
        int env = heap.cdr(frame);
        if (heap.atom(e) == e) {
            // atoms do not require eval
            return;
        }
    }
}
