package controlblock;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {    
    @Test
    public void testParseStringBasic() {
        ConsHeap heap = new ConsHeap(255);
        String chunk = "hello world";
        Parser instance = new Parser();
        int expResult = 0;
        int result = instance.parseString(heap, chunk);
        heap.dump(result);
    }

    @Test
    public void testParseStringQuote() {
        ConsHeap heap = new ConsHeap(255);
        String chunk = "hello 'world";
        Parser instance = new Parser();
        int expResult = 0;
        int result = instance.parseString(heap, chunk);
        heap.dump(result);
    }
}
