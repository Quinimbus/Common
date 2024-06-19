package cloud.quinimbus.common.tools;

public class Fields {

    public static String toGetterName(String fieldName) {
        return "get%s".formatted(toMethodPrefix(fieldName));
    }

    private static String toMethodPrefix(String fieldName) {
        var result = new StringBuilder();
        result.append(fieldName.substring(0, 1).toUpperCase());
        result.append(fieldName.substring(1));
        return result.toString();
    }
}
