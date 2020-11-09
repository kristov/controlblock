package controlblock;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        Console console = System.console();
        try {
            System.out.print(">> ");
            String chunk = console.readLine();
            while (chunk != null) {
                int e = parser.parseString(heap, chunk);
                evaluator.prepareStack(e);
                evaluator.dumpAll();
                console.readLine();
                while (evaluator.evalStep()) {
                    evaluator.dumpAll();
                    console.readLine();
                }
                System.out.print("<< ");
                int result = evaluator.result();
                heap.dump(result);
                System.out.print(">> ");
                chunk = console.readLine();
            }
        }
        catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }
}