package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CoreFormsTest {
    @Test
    public void testAddSimple() {
        ConsHeap heap = new ConsHeap(255);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "+ 1 1");
        evaluator.eval(e);
        int result = evaluator.result();
        heap.dump(result);
    }
}
