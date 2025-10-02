package cloud.quinimbus.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// This is a generic annotation to mark the implementation of a service interface (SPI). Refer to the specific
/// interface for specific instructions. The implementations will be loaded using the [java.util.ServiceLoader].
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Provider {

    /// The id to use to refer to this implementation
    /// @return The implementation id
    String id();

    /// The human-readable name of this implementation, this will be used for example for more informative logging. If
    /// empty the id will be used.
    /// @return The human-readable name of the implementation
    String name() default "";

    /// Additional string to be used to refer this implementation as alias to the id. This will not be supported by
    /// every system using this annotation.
    /// @return The array of aliases for the id of this implementation
    String[] alias() default {};

    /// The priority of this implementation, the higher the more important. Some systems will use the highest priority
    /// implementation to be found on the classpath of will use all implementations in priority order. Some systems
    /// will simply ignore this value.
    /// @return The priority of this implementation
    int priority() default 0;
}
