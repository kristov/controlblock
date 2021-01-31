package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConsPairTest {
    @Test
    public void testAppendCons() {
        ConsHeap heap = new ConsHeap(255);
        int cons1 = heap.newCons();
        int cons2 = heap.newCons();
        int res = heap.appendCons(cons1, cons2);
        assertEquals(heap.cdr(cons1), cons2);
        int cons3 = heap.newCons();
        res = heap.appendCons(cons1, cons3);
        assertEquals(heap.cdr(cons2), cons3);
    }

    @Test
    public void testNewPair() {
        ConsHeap heap = new ConsHeap(255);
        int cons = heap.newCons();
        int val1 = heap.sym("VAL1");
        heap.pairSet(cons, "key1", val1);
        assertEquals(heap.atomString(heap.car(heap.car(cons))), "key1");
        assertEquals(heap.cdr(heap.car(heap.car(cons))), val1);
        int val2 = heap.sym("VAL2");
        heap.pairSet(cons, "key2", val2);
        assertEquals(heap.atomString(heap.car(heap.cdr(heap.car(cons)))), "key2");
        int val3 = heap.sym("VAL3");
        heap.pairSet(cons, "key1", val3);
        assertEquals(heap.atomString(heap.cdr(heap.car(heap.car(cons)))), heap.atomString(val3));
    }

    @Test
    public void testNewPairGet() {
        ConsHeap heap = new ConsHeap(255);
        int cons = heap.newCons();
        int val1 = heap.sym("VAL1");
        heap.pairSet(cons, "key1", val1);
        int gotval = heap.pairGet(cons, "key1");
        assertEquals(val1, gotval);
    }
}