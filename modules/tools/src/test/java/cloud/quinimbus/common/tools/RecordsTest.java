package cloud.quinimbus.common.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RecordsTest {

    @Test
    public void testIdFromRecordClass() {
        assertEquals("myTestRecord", Records.idFromRecordClass(MyTestRecord.class));
    }
    
    @Test
    public void testFieldValueGetter() {
        var r = new MyTestRecord("TEST");
        assertEquals("TEST", Records.fieldValueGetter(MyTestRecord.class, "strField").apply(r));
    }
}
