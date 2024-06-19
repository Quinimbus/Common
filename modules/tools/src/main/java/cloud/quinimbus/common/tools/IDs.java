package cloud.quinimbus.common.tools;

public class IDs {

    public static String toPlural(String id) {
        if (id.endsWith("y")) {
            return id.substring(0, id.length() - 1).concat("ies");
        } else {
            return id.concat("s");
        }
    }
}
