package cloud.quinimbus.common.tools;

import java.util.Locale;

public class Records {
    
    public static String idFromRecordClass(Class<? extends Record> recordClass) {
        return idFromClassName(recordClass.getSimpleName());
    }
    
    public static String idFromType(NamedType type) {
        return idFromClassName(type.getSimpleName());
    }
    
    private static String idFromClassName(String name) {
        return name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
    }
}
