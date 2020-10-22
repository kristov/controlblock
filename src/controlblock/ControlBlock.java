package controlblock;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        //int root = heap.list();
        //int helo = heap.newSymbol("Hello");
        //int comb = heap.cons(helo, root);
        Parser parser = new Parser();
        int comb = parser.parseString(heap, "hello (helo world)");
        heap.dumpHeap();
        heap.dump(comb);
    }
}
