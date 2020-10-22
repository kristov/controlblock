package controlblock;

public class Parser {
    private int[] stack;
    private int stack_ptr = 0;

    public Parser() {
        stack = new int[1024];
    }

    /* Parses the string and returns the root id */
    public int parseString(ConsHeap heap, String chunk) {
        stack_ptr = 0;
        int list = heap.newCons();
        stack[stack_ptr] = list;
        StringBuilder word = new StringBuilder();
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
                //int qlist = heap.newCons();
                //int quote = heap.newSymbol("quote");
                //heap.appendList(qlist, quote);
                // TODO: wrap this in (quote ...)
            }
            if (c == ')') {
                int last = stack[stack_ptr -1];
                heap.appendList(last, list);
                word_complete = true;
            }
            if ((c > 32 && c < 40) || (c > 41 && c < 127)) {
                word.append(c);
            }
            if (word_complete && word.length() > 0) {
                int symbol = heap.newSymbol(word.toString());
                heap.appendList(list, symbol);
                word.delete(0, word.length());
            }
            word_complete = false;
        }
        if (word.length() > 0) {
            int symbol = heap.newSymbol(word.toString());
            heap.appendList(list, symbol);
        }
        return stack[0];
    }
}
