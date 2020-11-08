package controlblock;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ControlBlock {
    public static void main(String[] args) {
        Process proc = new Process();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            String line = br.readLine();
            while (line != null) {
                proc.debugEvalString(line);
                line = br.readLine();
                while (line != null) {
                    if (!proc.debugEvalStep()) {
                        line = null;
                        continue;
                    }
                    proc.dumpAll();
                    line = br.readLine();
                }
                System.out.println(proc.result());
                line = br.readLine();
            }
        }
        catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }
}