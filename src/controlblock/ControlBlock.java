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
                proc.evalString(line);
                System.out.println(proc.result());
                line = br.readLine();
            }
        }
        catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
        }
    }
}