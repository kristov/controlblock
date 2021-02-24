package controlblock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Parser {
    private int[] stack;
    private int stack_ptr = 0;

    public Parser() {
        stack = new int[1024];
    }

    private void appendIfWord(ConsHeap heap, int list, StringBuilder word) {
        if (word.length() > 0) {
            int symbol = heap.sym(word.toString());
            heap.append(list, symbol);
            word.delete(0, word.length());
        }
    }

    public int parseString(ConsHeap heap, String chunk) {
        Reader inputString = new StringReader(chunk);
        BufferedReader reader = new BufferedReader(inputString);
        int ret = 0;
        try {
            ret = parseBuffer(heap, reader);
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
        return ret;
    }

    public int parseBuffer(ConsHeap heap, BufferedReader reader) throws IOException {
        stack_ptr = 0;
        int qlist = 0;

        // Create the root list and put it on the stack
        int list = heap.newCons();
        stack[stack_ptr] = list;

        // A string for appending token characters to
        StringBuilder word = new StringBuilder();

        // A boolean to mark when the end of a token is detected
        boolean word_complete = false;

        char c = 0;
        while ((c = readChar(reader)) > 0) {
            if (c == ' ') {
                word_complete = true;
            }
            if (c == '(') {
                list = heap.newCons();
                stack_ptr++;
                stack[stack_ptr] = list;
                word_complete = true;
            }
            if (c == 39) { // single tick
                int ql = heap.newCons();
                stack_ptr++;
                stack[stack_ptr] = list;
                int quote = heap.sym("quote");
                heap.append(ql, quote);
                heap.append(list, ql);
                list = ql;
            }
            if (c == ')') {
                appendIfWord(heap, list, word);
                int prev = stack[--stack_ptr];
                heap.append(prev, list);
                list = prev;
                word_complete = true;
            }
            if ((c > 32 && c < 39) || (c > 39 && c < 40) || (c > 41 && c < 127)) {
                word.append(c);
            }
            if (word_complete) {
                appendIfWord(heap, list, word);
            }
            word_complete = false;
        }
        appendIfWord(heap, list, word);
        return stack[0];
    }

    private char readChar(BufferedReader reader) {
        char c = 0;
        try {
            int i = reader.read();
            if (i != -1) {
                c = (char)i;
            }
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
        return c;
    }
}
