package controlblock;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print(">> ");
            String chunk = br.readLine();
            while (chunk != null) {
                int e = parser.parseString(heap, chunk);
                evaluator.prepareStack(e);
                evaluator.dumpAll();
                br.readLine();
                while (evaluator.evalStep()) {
                    evaluator.dumpAll();
                    br.readLine();
                }
                System.out.print("<< ");
                int result = evaluator.result();
                heap.dump(result);
                System.out.print(">> ");
                chunk = br.readLine();
            }
        }
        catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }
}