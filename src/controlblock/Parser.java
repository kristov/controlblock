package controlblock;

public class Parser {
    private int[] stack;
    private int stack_ptr = 0;

    public Parser() {
        stack = new int[1024];
    }

    private void appendIfWord(ConsHeap heap, int list, StringBuilder word) {
        if (word.length() > 0) {
            int symbol = heap.newSymbol(word.toString());
            heap.append(list, symbol);
            word.delete(0, word.length());
        }
    }

    /* Parses the string and returns the root id */
    public int parseString(ConsHeap heap, String chunk) {
        stack_ptr = 0;
        int qlist = 0;

        // Create the root list and put it on the stack
        int list = heap.newCons();
        stack[stack_ptr] = list;

        // A string for appending token characters to
        StringBuilder word = new StringBuilder();

        // A boolean to mark when the end of a token is detected
        boolean word_complete = false;

        for (int i = 0; i < chunk.length(); i++) {
            char c = chunk.charAt(i);
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
                int quote = heap.newSymbol("quote");
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
}
