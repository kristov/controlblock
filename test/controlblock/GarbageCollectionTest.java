package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GarbageCollectionTest {
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
*/
    
    @Test
    public void testBasicEval() {
        ConsHeap heap = new ConsHeap(256);
        Parser parser = new Parser();
        int preUsed = heap.nrUsedCons();
        int preMarked = heap.nrMarkedCons();
        System.out.println("used: " + preUsed + " marked: " + preMarked);
        int e = parser.parseString(heap, "quote (A B)");
        heap.evalExpression(e);
        int result = heap.result();
        heap.dump("result", result);
        System.out.println("result: " + result);
        int postUsed = heap.nrUsedCons();
        int postMarked = heap.nrMarkedCons();
        System.out.println("used: " + postUsed + " marked: " + postMarked);
    }
}