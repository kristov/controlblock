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
        assertEquals("2.0", heap.atomString(result));
    }

    @Test
    public void testAddNested() {
        ConsHeap heap = new ConsHeap(255);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "+ (+ 2 2) (+ 3 3)");
        evaluator.eval(e);
        int result = evaluator.result();
        assertEquals("10.0", heap.atomString(result));
    }

    @Test
    public void testAssignq() {
        ConsHeap heap = new ConsHeap(255);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "assignq bob geldof");
        evaluator.eval(e);
        int result = evaluator.result();
        assertEquals("geldof", heap.atomString(result));
    }

    @Test
    public void testAssignqStructure() {
        ConsHeap heap = new ConsHeap(255);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "assignq mylist (1 2 3)");
        evaluator.eval(e);
        int result = evaluator.result();
        assertEquals("1", heap.atomString(heap.car(result)));
        assertEquals("2", heap.atomString(heap.cdr(heap.car(result))));
        assertEquals("3", heap.atomString(heap.cdr(heap.cdr(heap.car(result)))));
    }

    @Test
    public void testInlineLambda() {
        ConsHeap heap = new ConsHeap(255);
        Evaluator evaluator = new Evaluator(heap);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "(lambda (d e) (+ d e)) 2 3");
        evaluator.eval(e);
        int result = evaluator.result();
        assertEquals("5.0", heap.atomString(result));
    }
}
