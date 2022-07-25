package cloud.quinimbus.common.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IDsTest {
    
    @Test
    public void testToPlural() {
        assertEquals("persons", IDs.toPlural("person"));
        assertEquals("entries", IDs.toPlural("entry"));
    }
}
