package cloud.quinimbus.common.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.function.Function;

public class Records {

    public static String idFromClass(Class<?> recordClass) {
        if (!Record.class.isAssignableFrom(recordClass)) {
            throw new IllegalArgumentException("%s is not a record class".formatted(recordClass.getSimpleName()));
        }
        return idFromClassName(recordClass.getSimpleName());
    }

    public static String idFromRecordClass(Class<? extends Record> recordClass) {
        return idFromClassName(recordClass.getSimpleName());
    }

    public static String idFromType(NamedType type) {
        return idFromClassName(type.getSimpleName());
    }

    public static <T extends Record, V> Function<T, V> fieldValueGetter(Class<T> recordClass, String field) {
        return r -> {
            try {
                return (V) recordClass.getMethod(field).invoke(r);
            } catch (NoSuchMethodException
                    | SecurityException
                    | IllegalAccessException
                    | InvocationTargetException ex) {
                throw new IllegalArgumentException(
                        "Cannot read field %s on record of type %s".formatted(field, recordClass.getName()), ex);
            }
        };
    }

    private static String idFromClassName(String name) {
        return name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
    }
}
