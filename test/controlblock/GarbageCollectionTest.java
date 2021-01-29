package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class GarbageCollectionTest {
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
        heap.deref(list1);
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
        heap.deref(list2);
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
        heap.deref(list1);
        assertEquals(0, heap.refCount(list1));
        assertEquals(0, heap.refCount(cons2));
    }
}