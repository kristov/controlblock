package controlblock;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        int root = heap.newCons();
        int helo = heap.newSymbol("Hello");
        int comb = heap.cons(helo, root);
        heap.dump(comb);
    }
}
