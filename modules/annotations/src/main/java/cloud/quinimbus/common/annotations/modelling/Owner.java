package cloud.quinimbus.common.annotations.modelling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Owner {

    Class<? extends Record> owningEntity();

    String field();
}
