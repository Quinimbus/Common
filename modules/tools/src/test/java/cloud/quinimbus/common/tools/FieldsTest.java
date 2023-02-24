package cloud.quinimbus.common.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FieldsTest {
    
    @Test
    public void testToGetterName() {
        assertEquals("getName", Fields.toGetterName("name"));
    }
}
