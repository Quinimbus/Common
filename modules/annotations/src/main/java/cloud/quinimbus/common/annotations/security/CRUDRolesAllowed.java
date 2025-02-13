package cloud.quinimbus.common.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CRUDRolesAllowed {

    Permissions create();

    Permissions read();

    Permissions update();

    Permissions delete();
}
