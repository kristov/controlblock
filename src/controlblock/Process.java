package controlblock;

public class Process {
    private final ConsHeap heap;
    private final Evaluator evaluator;
    private final Parser parser;
    private int env = 0;
    private int stack = 0;

    public Process() {
        heap = new ConsHeap(1024);
        env = heap.list(); // where user-defined functions will go
        stack = heap.list(); // a stack for the evaluator

        // Store the stack in the environment so the user can see it
        int stacke = heap.pair(heap.newSymbol("$STACK"), stack);
        heap.append(env, stacke);
        
        // Create an evaluator
        evaluator = new Evaluator(heap, stack, env);
        
        parser = new Parser();
    }

    public void evalString(String chunk) {
        int e = parser.parseString(heap, chunk);
        heap.dump(e);
        eval(e);
        heap.dumpHeap();
    }

    public void eval(int e) {
        evaluator.eval(e);
    }
}
