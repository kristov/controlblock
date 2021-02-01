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
        Parser parser = new Parser();
        int e = parser.parseString(heap, "+ 1 1");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("2.0", heap.atomString(result));
    }

    @Test
    public void testAddNested() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "+ (+ 2 2) (+ 3 3)");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("10.0", heap.atomString(result));
    }

    @Test
    public void testVassign() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "vassign bob geldof");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("geldof", heap.atomString(result));
    }

    @Test
    public void testVassignStructure() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "vassign mylist (1 2 3)");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("1", heap.atomString(heap.car(result)));
        assertEquals("2", heap.atomString(heap.cdr(heap.car(result))));
        assertEquals("3", heap.atomString(heap.cdr(heap.cdr(heap.car(result)))));
    }

    @Test
    public void testInlineLambda() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "(lambda (d e) ((+ d e))) 2 3");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("5.0", heap.atomString(result));
    }

    @Test
    public void testCond() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "cond (0 1) (0 2) (1 3)");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("3", heap.atomString(result));
    }

    @Test
    public void testProgn() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "progn (1 2 3 4)");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("4", heap.atomString(result));
    }

    @Test
    public void testQuote() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "quote (1 2 3 4)");
        heap.evalExpression(e);
        int result = heap.result();
        result = heap.car(result);
        assertEquals("1", heap.atomString(result)); result = heap.cdr(result);
        assertEquals("2", heap.atomString(result)); result = heap.cdr(result);
        assertEquals("3", heap.atomString(result)); result = heap.cdr(result);
        assertEquals("4", heap.atomString(result));
    }

    @Test
    public void testDefun() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "progn ((defun sum (quote (a b)) (quote (+ 2 (+ a b)))) (sum 2 4))");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("8.0", heap.atomString(result));
    }

    @Test
    public void testDefine() {
        ConsHeap heap = new ConsHeap(255);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "progn ((define sum (lambda (a b) (+ 2 (+ a b)))) (sum 2 4))");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("8.0", heap.atomString(result));
    }
}