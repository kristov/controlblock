package controlblock;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessTest {
    
    @Test
    public void testBasic() {
        String chunk = "+ 5 2";
        Process proc = new Process();
        proc.evalString(chunk);
        String r = proc.result();
        assertEquals("7.0", r);
    }
}
