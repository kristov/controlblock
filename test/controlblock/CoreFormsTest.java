package controlblock;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreFormsTest {
    private void checkForLeaks(ConsHeap heap) {
        int used = heap.nrUsedCons();
        int reachable = heap.nrReachableCons();
        assertEquals(reachable, used);
    }

    private String runString(String test) {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, test);
        heap.evalExpression(e);
        checkForLeaks(heap);
        return heap.atomString(heap.result());
    }

    @Test
    public void testNIL() {
        assertEquals("NIL", runString("NIL"));
    }

    @Test
    public void testCons() {
        // TODO: test structures somehow
        assertEquals("NIL", runString("cons boo (quote ())"));
        assertEquals("NIL", runString("cons a (cons b (cons c (quote ())))"));
    }

    @Test
    public void testCar() {
        assertEquals("a", runString("car (quote (a b c))"));
    }

    @Test
    public void testCdr() {
        assertEquals("NIL", runString("cdr (quote (a b c))"));
    }

    @Test
    public void testPlus() {
        assertEquals("2.0", runString("+ 1 1"));
    }

    @Test
    public void testMinus() {
        assertEquals("3.0", runString("- 6 3"));
    }

    @Test
    public void testGT() {
        assertEquals("true", runString("> 6 3"));
        assertEquals("NIL", runString("> 3 6"));
    }

    @Test
    public void testAddNested() {
        assertEquals("10.0", runString("+ (+ 2 2) (+ 3 3)"));
    }

    @Test
    public void testVar() {
        assertEquals("geldof", runString("var bob geldof"));
    }

    @Test
    public void testVarStructure() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "var mylist (quote (1 2 3))");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("1", heap.atomString(heap.car(result)));
        assertEquals("2", heap.atomString(heap.cdr(heap.car(result))));
        assertEquals("3", heap.atomString(heap.cdr(heap.cdr(heap.car(result)))));
    }

    @Test
    public void testInlineLambda() {
        assertEquals("5.0", runString("(lambda (d e) (+ d e)) 2 3"));
    }

    @Test
    public void testCond() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "cond (0 1) (0 2) (1 3)");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("3", heap.atomString(result));
    }

    @Test
    public void testProgn() {
        assertEquals("4", runString("progn (1 2 3 4)"));
    }

    @Test
    public void testQuote() {
        ConsHeap heap = new ConsHeap(256);
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
    public void testSymbol() {
        assertEquals("8.0", runString("progn ((sym sum (quote (lambda (a b) (+ 2 (+ a b))))) (sum 2 4))"));
    }

    @Test
    public void testScope() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "scope (.symbols) (quote ()) (quote ((progn ((var v 12) (dump (.scope))))))");
        heap.evalExpression(e);
        //heap.pushStack(e);
        checkForLeaks(heap);
        int scope = heap.getCurrentScope();
        heap.dump("scope", scope);
    }

/*
    @Test
    public void testJInt() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "jint 5");
        heap.evalExpression(e);
        int result = heap.result();
        assertEquals("5", heap.atomString(result));
    }

    @Test
    public void testJNew() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "jnew java.lang.Integer 1 10");
        heap.evalExpression(e);
        int result = heap.result();
        heap.dump("result", result);
        //assertEquals("8.0", heap.atomString(result));
    }

    @Test
    public void testJMethod() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "progn ((leta theInt (jnew java.lang.Integer 1 10)) (jmethod toString 1 theInt))");
        heap.evalExpression(e);
        int stack = heap.getStack();
        heap.dump("stack", stack);
        //assertEquals("8.0", heap.atomString(result));
    }
*/
}