package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConsHeapTest {
    @Test
    public void testAtom() {
        ConsHeap heap = new ConsHeap(255);
        int cons = heap.newSymbol("Hello");
        boolean result = heap.atom(cons);
        assertEquals(true, result);
    }

    @Test
    public void testEq() {
        ConsHeap heap = new ConsHeap(255);
        int a = heap.newSymbol("samesame");
        int b = heap.newSymbol("samesame");
        int c = heap.newSymbol("but different");
        boolean result = heap.eq(a, b);
        assertEquals(true, result);
        result = heap.eq(b, c);
        assertEquals(false, result);
    }

    @Test
    public void testPairGet() {
        ConsHeap heap = new ConsHeap(255);
        int look = heap.newSymbol("key2");
        int k1 = heap.newSymbol("key1");
        int v1 = heap.newSymbol("value1");
        int p1 = heap.list2(k1, v1);
        int k2 = heap.newSymbol("key2");
        int v2 = heap.newSymbol("value2");
        int p2 = heap.list2(k2, v2);
        int list = heap.newCons();
        heap.append(list, p1);
        heap.append(list, p2);
        int result = heap.pairGet(list, "key2");
        assertEquals(v2, result);
    }
    
    @Test
    public void testReverse() {
        ConsHeap heap = new ConsHeap(255);
        int l1 = heap.list5(
            heap.newSymbol("1"),
            heap.newSymbol("2"),
            heap.newSymbol("3"),
            heap.newSymbol("4"),
            heap.newSymbol("5")
        );
        int l2 = heap.reverse(l1);
        l2 = heap.car(l2);
        assertEquals("5", heap.atomString(l2)); l2 = heap.cdr(l2);
        assertEquals("4", heap.atomString(l2)); l2 = heap.cdr(l2);
        assertEquals("3", heap.atomString(l2)); l2 = heap.cdr(l2);
        assertEquals("2", heap.atomString(l2)); l2 = heap.cdr(l2);
        assertEquals("1", heap.atomString(l2));
    }
}