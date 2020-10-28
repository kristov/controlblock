package controlblock;

public class Process {
    private final ConsHeap heap;
    private final Evaluator evaluator;
    private final Parser parser;
    private int env = 0;
    private int stack = 0;

    public Process() {
        heap = new ConsHeap(1024);
        evaluator = new Evaluator(heap);
        parser = new Parser();
    }

    public void evalString(String chunk) {
        int e = parser.parseString(heap, chunk);
        evaluator.eval(e);
    }
    
    public String result() {
        int result = evaluator.result();
        return heap.atomString(result);
    }
}