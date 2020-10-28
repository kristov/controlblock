package controlblock;

public class ControlBlock {
    public static void main(String[] args) {
        Process proc = new Process();
        proc.evalString("+ 5 2");
        System.out.println(proc.result());
    }
}