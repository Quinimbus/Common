package cloud.quinimbus.common.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class IDsTest {

    @Test
    public void testToPlural() {
        assertEquals("persons", IDs.toPlural("person"));
        assertEquals("entries", IDs.toPlural("entry"));
    }
}
