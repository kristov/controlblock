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
        int cons = heap.sym("Hello");
        boolean result = heap.atom(cons);
        assertEquals(true, result);
    }

    @Test
    public void testEq() {
        ConsHeap heap = new ConsHeap(255);
        int a = heap.sym("samesame");
        int b = heap.sym("samesame");
        int c = heap.sym("but different");
        boolean result = heap.eq(a, b);
        assertEquals(true, result);
        result = heap.eq(b, c);
        assertEquals(false, result);
    }

    @Test
    public void testPairGet() {
        ConsHeap heap = new ConsHeap(255);
        int look = heap.sym("key2");
        int k1 = heap.sym("key1");
        int v1 = heap.sym("value1");
        int p1 = heap.list2(k1, v1);
        int k2 = heap.sym("key2");
        int v2 = heap.sym("value2");
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
            heap.sym("1"),
            heap.sym("2"),
            heap.sym("3"),
            heap.sym("4"),
            heap.sym("5")
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