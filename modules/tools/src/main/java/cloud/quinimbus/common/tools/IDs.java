package cloud.quinimbus.common.tools;

public class IDs {

    public static String toPlural(String id) {
        if (id.endsWith("y")) {
            return id.substring(0, id.length() - 1).concat("ies");
        } else {
            return id.concat("s");
        }
    }

    public static String toSingular(String id) {
        if (id.endsWith("ies")) {
            return id.substring(0, id.length() - 3).concat("y");
        }
        if (id.endsWith("s")) {
            return id.substring(0, id.length() - 1);
        }
        return id;
    }
}
