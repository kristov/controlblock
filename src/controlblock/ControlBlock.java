package controlblock;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        Parser parser = new Parser();
        Console console = System.console();
        try {
            System.out.print(">> ");
            String chunk = console.readLine();
            while (chunk != null) {
                int e = parser.parseString(heap, chunk);
                heap.prepareFirstFrame(e);
                console.readLine();
                while (heap.eval()) {
                    console.readLine();
                }
                int result = heap.result();
                heap.dump("<< ", result);
                System.out.print(">> ");
                chunk = console.readLine();
            }
        }
        catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }
}