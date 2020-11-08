package controlblock;

public class Process {
    private final ConsHeap heap;
    private final Evaluator evaluator;
    private final Parser parser;

    public Process() {
        heap = new ConsHeap(1024);
        evaluator = new Evaluator(heap);
        parser = new Parser();
    }

    public void evalString(String chunk) {
        int e = parser.parseString(heap, chunk);
        evaluator.eval(e);
    }

    public void debugEvalString(String chunk) {
        int e = parser.parseString(heap, chunk);
        evaluator.prepareStack(e);
    }

    public void dumpAll() {
        evaluator.dumpAll();
    }

    public boolean debugEvalStep() {
        return evaluator.evalStep();
    }

    public String result() {
        int result = evaluator.result();
        return heap.atomString(result);
    }
}