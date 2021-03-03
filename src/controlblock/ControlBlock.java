package controlblock;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ControlBlock {
    public static void main(String[] args) {
        ConsHeap heap = new ConsHeap(1024);
        Parser parser = new Parser();
        boolean replMode = false;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-r")) {
                replMode = true;
                continue;
            }
            File file = new File(args[i]);
            if (!file.exists()) {
                continue;
            }
            try {
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                int e = parser.parseBuffer(heap, reader);
                heap.evalExpression(e);
                if (!replMode) {
                    int result = heap.result();
                    System.out.println(heap.atomString(result));
                }
            }
            catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        if (!replMode) {
            return;
        }
        Console console = System.console();
        try {
            System.out.print(">> ");
            String chunk = console.readLine();
            while (chunk != null) {
                int e = parser.parseString(heap, chunk);
                heap.pushStack(e);
                heap.dumpScope();
                console.readLine();
                while (heap.eval()) {
                    heap.dumpScope();
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