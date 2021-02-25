package controlblock;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlagTest {    
    @Test
    public void testFlagSet() {
        int i = 0;
        Flag flag = new Flag(256);
        flag.set(2);
        flag.set(3);
        flag.set(4);
    }

    @Test
    public void testFlagGet() {
        Flag flag = new Flag(256);
        flag.set(2);
        flag.set(3);
        flag.set(4);
        assertEquals(false, flag.get(0));
        assertEquals(false, flag.get(1));
        assertEquals(true, flag.get(2));
        assertEquals(true, flag.get(3));
        assertEquals(true, flag.get(4));
        assertEquals(false, flag.get(5));
        assertEquals(false, flag.get(6));
    }
}
