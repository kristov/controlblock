package controlblock;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreFormsTest {
    private void checkForLeaks(ConsHeap heap) {
        int used = heap.nrUsedCons();
        int reachable = heap.nrReachableCons();
        if (used != reachable) {
            System.out.println("---------- ORPHANED ----------");
            heap.printOrphaned();
            System.out.println("------------------------------");
        }
        assertEquals(reachable, used);
    }

    private String runString(String test) {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, test);
        heap.evalExpression(e);
        checkForLeaks(heap);
        return heap.atomString(heap.getOutput());
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
    public void testDump() {
        assertEquals("(1 2 3 boo)", runString("dump (quote (1 2 3 boo))"));
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
        int result = heap.getOutput();
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
        assertEquals("3", runString("cond (0 1) (0 2) (1 3)"));
    }

    @Test
    public void testWhile() {
        assertEquals("0.0", runString("progn (quote ((var (quote c) 3) (while (> c 0) (var (quote c) (- c 1))) (c)))"));
    }

    @Test
    public void testProgn() {
        assertEquals("4", runString("progn (quote (1 2 3 4))"));
    }

    @Test
    public void testQuote() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "quote (1 2 3 4)");
        heap.evalExpression(e);
        int result = heap.getOutput();
        result = heap.car(result);
        assertEquals("1", heap.atomString(result)); result = heap.cdr(result);
        assertEquals("2", heap.atomString(result)); result = heap.cdr(result);
        assertEquals("3", heap.atomString(result)); result = heap.cdr(result);
        assertEquals("4", heap.atomString(result));
    }

    @Test
    public void testSymbol() {
        assertEquals("8.0", runString("progn (quote ((sym sum (quote (lambda (a b) (+ 2 (+ a b))))) (sum 2 4)))"));
    }

    @Test
    public void testListn() {
        assertEquals("(4 5)", runString("dump (listn 2 4 5)"));
    }

    @Test
    public void testFscope() {
        assertEquals("5.0", runString("fscope (quote (lambda (d e) (+ d e))) 2 3"));
    }

    @Test
    public void testImport() {
        assertEquals("14.0", runString(
            "progn (quote (" +
                "(symbind (quote (sym sumand2 (quote (fscope (quote (lambda (a b) (+ 2 (+ a b)))))))) (symbols MYNS)) " +
                "(import MYNS (quote sumand2) (quote MYNS_sumand2)) " +
                "(MYNS_sumand2 6 6)" +
            "))"
        ));
    }

    @Test
    public void testCustomScope() {
        assertEquals("12", runString("(sym fnscope (quote (lambda (nstack) (rvar scope (listn 5 (listn 2 (quote parentfr) (.scope)) (listn 2 (quote symbols) (.symbols)) (listn 2 (quote variables) (quote (ref ()))) (listn 2 (quote stack) (listn 1 nstack)) (listn 2 (quote values) (.values)))))))"));
    }
/*
    @Test
    public void testJInt() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "jint 5");
        heap.evalExpression(e);
        int output = heap.output();
        assertEquals("5", heap.atomString(output));
    }

    @Test
    public void testJNew() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "jnew java.lang.Integer 1 10");
        heap.evalExpression(e);
        int output = heap.output();
        heap.dump("output", output);
        //assertEquals("8.0", heap.atomString(output));
    }

    @Test
    public void testJMethod() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "progn ((leta theInt (jnew java.lang.Integer 1 10)) (jmethod toString 1 theInt))");
        heap.evalExpression(e);
        int stack = heap.getStack();
        heap.dump("stack", stack);
        //assertEquals("8.0", heap.atomString(output));
    }
*/
}