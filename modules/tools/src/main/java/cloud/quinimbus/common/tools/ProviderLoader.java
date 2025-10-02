package cloud.quinimbus.common.tools;

import cloud.quinimbus.tools.function.LazySingletonSupplier;
import cloud.quinimbus.tools.lang.TypeRef;
import cloud.quinimbus.common.annotations.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Gatherer;

/// Discovers and indexes implementations of a service interface via [java.util.ServiceLoader] that are annotated with
/// [cloud.quinimbus.common.annotations.Provider].
///
/// ## Contract
/// - **Annotation required:** Every discovered implementation must be annotated with [Provider]. A missing annotation
///   causes an immediate [IllegalStateException].
/// - **Keys:** Each provider contributes its declared `id` and—if enabled—its `aliases`. All keys for a provider
///   address the same underlying provider.
/// - **Priority order preserved:** The returned map preserves priority order: higher-priority providers appear first;
///   within each provider, the **id** precedes its **aliases** in their declared order.
/// - **Conflicts:** If multiple providers claim the same key (id or alias), the conflict is recorded; priority does
///   **not** resolve it.
/// - **Alias policy:** When alias support is disabled, any declared aliases are recorded as validation errors.
/// - **Failure aggregation:** After processing, if any validation errors were recorded (e.g., aliases while disabled,
///   duplicate keys), a single [IllegalStateException] is thrown containing all messages.
///
/// ## Result
/// - Returns an **unmodifiable** [java.util.SequencedMap] from key → [java.util.ServiceLoader.Provider].
/// - Providers are **lazy**: instances are created only when [java.util.ServiceLoader.Provider#get()] is called.
///
/// @since 0.2
public class ProviderLoader {

    private record MappedProvider<P>(String id, String[] alias, int priority, LazySingletonSupplier<P> supplier) {}

    private record ProviderMapping<P>(String key, LazySingletonSupplier<P> supplier) {}

    @FunctionalInterface
    private interface TriConsumer<P1, P2, P3> {
        void consume(P1 p1, P2 p2, P3 p3);
    }

    private ProviderLoader() {}

    /// Discovers implementations of `providerInterface` and returns an **unmodifiable**, priority-ordered
    /// [java.util.SequencedMap] from provider keys (id and, if enabled, aliases) to their
    /// [java.util.ServiceLoader.Provider], according to the class-level contract of
    /// [cloud.quinimbus.common.tools.ProviderLoader].
    ///
    /// @param <S> the raw variant of the service (SPI) type
    /// @param <T> the service (SPI) type
    /// @param providerInterface the SPI interface used for discovery
    /// @param loader The serviceloader to use.
    /// @param aliasSupported whether `@Provider(alias)` entries are allowed
    /// @return an unmodifiable, priority-ordered `SequencedMap` from keys to providers
    /// @throws IllegalStateException
    ///   - **Immediate:** if any discovered type lacks `@Provider`.
    ///   - **Aggregated after processing:** if aliases are present while
    ///     `aliasSupported == false`, or if duplicate keys are detected.
    public static <S, T> SequencedMap<String, LazySingletonSupplier<T>> loadProviders(
            Class<T> providerInterface, Function<Class<T>, ServiceLoader<S>> loader, boolean aliasSupported) {
        return loadProviders(TypeRef.of(providerInterface), loader, aliasSupported);
    }

    /// Discovers implementations of `providerInterface` and returns an **unmodifiable**, priority-ordered
    /// [java.util.SequencedMap] from provider keys (id and, if enabled, aliases) to their
    /// [java.util.ServiceLoader.Provider], according to the class-level contract of
    /// [cloud.quinimbus.common.tools.ProviderLoader].
    ///
    /// @param <S> the raw variant of the service (SPI) type
    /// @param <T> the service (SPI) type
    /// @param providerInterface the SPI interface used for discovery
    /// @param loader The serviceloader to use.
    /// @param aliasSupported whether `@Provider(alias)` entries are allowed
    /// @return an unmodifiable, priority-ordered `SequencedMap` from keys to providers
    /// @throws IllegalStateException
    ///   - **Immediate:** if any discovered type lacks `@Provider`.
    ///   - **Aggregated after processing:** if aliases are present while
    ///     `aliasSupported == false`, or if duplicate keys are detected.
    public static <S, T> SequencedMap<String, LazySingletonSupplier<T>> loadProviders(
            TypeRef<T> providerInterface, Function<Class<T>, ServiceLoader<S>> loader, boolean aliasSupported) {
        var errorMessages = new ArrayList<String>();
        var providers = loader.apply((Class<T>) providerInterface.getRawClass()).stream()
                .map(p -> (ServiceLoader.Provider<T>) p)
                .map(p -> readProviderInfo(providerInterface, p))
                .sorted((mp1, mp2) -> Integer.compare(mp2.priority(), mp1.priority()))
                .gather(gatherMappings(providerInterface, aliasSupported, errorMessages::add))
                .collect(toMap(
                        ProviderMapping::key,
                        pm -> pm.supplier(),
                        (key, pm1, pm2) -> errorMessages.add(
                                "There are at least two implementations of %s for the id or alias %s: %s and %s"
                                        .formatted(
                                                providerInterface,
                                                key,
                                                pm1.getRawType().getName(),
                                                pm2.getRawType().getName()))));
        if (!errorMessages.isEmpty()) {
            throw new IllegalStateException("Errors occurred while loading providers for %s:\n%s"
                    .formatted(providerInterface, errorMessages.stream().collect(Collectors.joining("\n"))));
        }
        return Collections.unmodifiableSequencedMap(providers);
    }

    private static <P, T> MappedProvider<P> readProviderInfo(
            TypeRef<T> providerInterface, ServiceLoader.Provider<P> p) {
        var providerAnno = p.type().getAnnotation(Provider.class);
        if (providerAnno == null) {
            throw new IllegalStateException("%s %s is missing the @Provider annotation"
                    .formatted(providerInterface, p.type().getName()));
        }
        return new MappedProvider<>(
                providerAnno.id(),
                providerAnno.alias(),
                providerAnno.priority(),
                new LazySingletonSupplier<>(p, p.type()));
    }

    private static <T> Gatherer<MappedProvider<T>, ?, ProviderMapping<T>> gatherMappings(
            TypeRef<T> providerInterface, boolean aliasSupported, Consumer<String> errorHandler) {
        return Gatherer.ofSequential((_, element, downstream) -> {
            downstream.push(new ProviderMapping<>(element.id(), element.supplier()));
            if (!aliasSupported && element.alias().length > 0) {
                errorHandler.accept(
                        "Setting an alias in @Provider is not supported for implementations of %s, please review %s"
                                .formatted(
                                        providerInterface,
                                        element.supplier().getRawType().getName()));
            }
            for (String alias : element.alias()) {
                downstream.push(new ProviderMapping<>(alias, element.supplier()));
            }
            return true;
        });
    }

    private static <T, K, V> Collector<T, ?, SequencedMap<K, V>> toMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper,
            TriConsumer<K, V, V> duplicateKey) {
        return Collector.of(
                LinkedHashMap::new,
                (m, t) -> {
                    K k = keyMapper.apply(t);
                    V v = Objects.requireNonNull(valueMapper.apply(t));
                    if (m.putIfAbsent(k, v) != null) {
                        duplicateKey.consume(k, m.get(k), v);
                    }
                },
                (m1, m2) -> {
                    m2.forEach((k, v) -> {
                        if (m1.putIfAbsent(k, v) != null) {
                            duplicateKey.consume(k, m1.get(k), v);
                        }
                    });
                    return m1;
                });
    }
}
