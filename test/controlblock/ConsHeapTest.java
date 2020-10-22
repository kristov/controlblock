package controlblock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConsHeapTest {
    @Test
    public void testNewCons() {
        ConsHeap heap = new ConsHeap(255);
        int cons = heap.newCons();
        assertEquals(1, cons);
    }

    @Test
    public void testNewSymbol() {
        ConsHeap heap = new ConsHeap(255);
        int expResult = 0;
        int cons = heap.newSymbol("Hello");
        assertEquals(1, cons);
    }

    @Test
    public void testAtom() {
        ConsHeap heap = new ConsHeap(255);
        int cons = heap.newSymbol("Hello");
        int result = heap.atom(cons);
        assertEquals(cons, result);
    }

    @Test
    public void testCar() {
    }

    @Test
    public void testCdr() {
    }

    @Test
    public void testCons() {
    }

    @Test
    public void testList() {
    }

    @Test
    public void testAppendList() {
    }
}