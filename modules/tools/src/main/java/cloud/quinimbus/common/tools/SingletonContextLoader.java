package cloud.quinimbus.common.tools;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class SingletonContextLoader {

    private static final SingletonContextLoader instance = new SingletonContextLoader();

    private final ConcurrentMap<Class, Object> contextInstances;

    public SingletonContextLoader() {
        this.contextInstances = new ConcurrentHashMap<>();
    }

    public static <T> T loadContext(Class<T> cls, Function<Class<T>, ServiceLoader<T>> loader) {
        return (T) instance.contextInstances.computeIfAbsent(cls, c -> createContext(c, loader));
    }

    private static <T> T createContext(Class<T> cls, Function<Class<T>, ServiceLoader<T>> loader) {
        return loader.apply(cls)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Cannot find any %s implementation".formatted(cls.getSimpleName())));
    }
}
