package cloud.quinimbus.common.tools;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RecordsTest {

    @Test
    public void testIdFromRecordClass() {
        assertEquals("myTestRecord", Records.idFromRecordClass(MyTestRecord.class));
    }

    @Test
    public void testFieldValueGetter() {
        var r = new MyTestRecord("TEST");
        var fieldValueGetter = Records.fieldValueGetter(MyTestRecord.class, "strField");
        assertEquals("TEST", fieldValueGetter.apply(r));
    }
}
