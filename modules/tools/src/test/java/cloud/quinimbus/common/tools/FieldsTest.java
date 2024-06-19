package cloud.quinimbus.common.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FieldsTest {

    @Test
    public void testToGetterName() {
        assertEquals("getName", Fields.toGetterName("name"));
    }
}
