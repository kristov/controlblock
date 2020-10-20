package controlblock;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        int cell = heap.newCons();
        System.out.println(cell);
    }
}
