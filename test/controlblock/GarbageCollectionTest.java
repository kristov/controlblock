package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GarbageCollectionTest {
    private void checkForLeaks(ConsHeap heap) {
        int used = heap.nrUsedCons();
        int reachable = heap.nrReachableCons();
        assertEquals(reachable, used);
    }

/*
    @Test
    public void testNewConsBasicRef() {
        ConsHeap heap = new ConsHeap(255);
        int cons1 = heap.newCons();
        assertEquals(1, heap.refCount(cons1));
        int cons2 = heap.newCons();
        int list1 = heap.list2(cons1, cons2);
        assertEquals(1, heap.refCount(list1));
        assertEquals(2, heap.refCount(cons1));
        assertEquals(2, heap.refCount(cons2));
        heap.reap(list1);
        assertEquals(0, heap.refCount(list1));
        assertEquals(0, heap.refCount(cons1));
        assertEquals(0, heap.refCount(cons2));
        int cons1fr = heap.newCons();
        assertEquals(cons1, cons1fr);
    }

    @Test
    public void testNewConsSublistDeref() {
        ConsHeap heap = new ConsHeap(255);
        int cons1 = heap.newCons();
        assertEquals(1, heap.refCount(cons1));
        int cons2 = heap.newCons();
        int list1 = heap.list2(cons1, cons2);
        int list2 = heap.newCons();
        heap.push(list2, list1);
        assertEquals(1, heap.refCount(list2));
        assertEquals(2, heap.refCount(list1));
        assertEquals(2, heap.refCount(cons1));
        assertEquals(2, heap.refCount(cons2));
        heap.reap(list2);
        assertEquals(0, heap.refCount(list2));
        assertEquals(0, heap.refCount(list1));
        assertEquals(0, heap.refCount(cons1));
        assertEquals(0, heap.refCount(cons2));
    }

    @Test
    public void testNewConsSublistPop() {
        ConsHeap heap = new ConsHeap(255);
        int cons1 = heap.newCons();
        assertEquals(1, heap.refCount(cons1));
        int cons2 = heap.newCons();
        int list1 = heap.list2(cons1, cons2);
        int list2 = heap.newCons();
        heap.push(list2, list1);
        assertEquals(1, heap.refCount(list2));
        assertEquals(2, heap.refCount(list1));
        assertEquals(2, heap.refCount(cons1));
        assertEquals(2, heap.refCount(cons2));
        list1 = heap.pop(list2);
        assertEquals(1, heap.refCount(list1));
        heap.reap(list1);
        assertEquals(0, heap.refCount(list1));
        assertEquals(0, heap.refCount(cons2));
    }

    @Test
    public void testBasicEval() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "A");
        heap.evalExpression(e);
        checkForLeaks(heap);
    }

    @Test
    public void testQuotedList() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "quote (A B)");
        heap.evalExpression(e);
        checkForLeaks(heap);
    }
*/

    @Test
    public void testProgn() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int e = parser.parseString(heap, "progn ((symbol sum (quote (lambda (a b) (+ 2 (+ a b))))) (sum 2 4))");
        heap.evalExpression(e);
        heap.printOrphaned();
        checkForLeaks(heap);
    }
}