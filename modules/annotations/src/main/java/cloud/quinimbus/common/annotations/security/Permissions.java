package cloud.quinimbus.common.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Permissions {

    Type value();

    String[] roles() default {};

    public enum Type {
        ANONYMOUS,
        AUTHENTICATED,
        ROLES
    }
}
