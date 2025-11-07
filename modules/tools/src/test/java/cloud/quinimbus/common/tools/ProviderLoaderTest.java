package cloud.quinimbus.common.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cloud.quinimbus.common.annotations.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProviderLoaderTest {

    interface TestSpi {
        String id();
    }

    @Provider(
            id = "a",
            alias = {"aa", "A"},
            priority = 10)
    static class ImplA implements TestSpi {
        @Override
        public String id() {
            return "A";
        }
    }

    @Provider(
            id = "b",
            alias = {"bb"},
            priority = 5)
    static class ImplB implements TestSpi {
        @Override
        public String id() {
            return "B";
        }
    }

    static class ImplNoAnno implements TestSpi {
        @Override
        public String id() {
            return "X";
        }
    }

    @Provider(
            id = "x",
            alias = {"a"},
            priority = 7)
    static class ImplAliasDup implements TestSpi {
        @Override
        public String id() {
            return "XD";
        }
    }

    private static ServiceLoader.Provider<TestSpi> providerOf(Class<? extends TestSpi> implClass) {
        return new ServiceLoader.Provider<>() {
            @Override
            public Class<? extends TestSpi> type() {
                return implClass;
            }

            @Override
            public TestSpi get() {
                try {
                    return implClass.getDeclaredConstructor().newInstance();
                } catch (IllegalAccessException
                        | IllegalArgumentException
                        | InstantiationException
                        | NoSuchMethodException
                        | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static ServiceLoader<TestSpi> serviceLoaderOf(Stream<ServiceLoader.Provider<TestSpi>> providers) {
        ServiceLoader<TestSpi> sl = mock(ServiceLoader.class);
        when(sl.stream()).thenReturn(providers);
        return sl;
    }

    private static void withMockedServiceLoader(ServiceLoader<TestSpi> sl, Runnable body) {
        try (MockedStatic<ServiceLoader> mocked = mockStatic(ServiceLoader.class)) {
            mocked.when(() -> ServiceLoader.load(TestSpi.class)).thenReturn(sl);
            body.run();
        }
    }

    @Test
    void loadsProviders_withAliasesSupported_collectsAllNamesAndAliases() {
        var sl = serviceLoaderOf(Stream.of(providerOf(ImplA.class), providerOf(ImplB.class)));

        withMockedServiceLoader(sl, () -> {
            var map = ProviderLoader.loadProviders(TestSpi.class, ServiceLoader::load, true);

            assertEquals(5, map.size());
            assertEquals(ImplA.class, map.get("a").getType());
            assertEquals(ImplA.class, map.get("aa").getType());
            assertEquals(ImplA.class, map.get("A").getType());
            assertEquals(ImplB.class, map.get("b").getType());
            assertEquals(ImplB.class, map.get("bb").getType());
        });
    }

    @Test
    void aliasNotSupported_butAliasPresent_raisesError() {
        var sl = serviceLoaderOf(Stream.of(providerOf(ImplA.class)));

        withMockedServiceLoader(sl, () -> {
            var ex = assertThrows(
                    IllegalStateException.class,
                    () -> ProviderLoader.loadProviders(TestSpi.class, ServiceLoader::load, false));
            assertTrue(ex.getMessage().toLowerCase().contains("alias"));
            assertTrue(ex.getMessage().contains(ImplA.class.getName()));
        });
    }

    @Test
    void duplicateNameOrAlias_raisesError_andListsImplementations() {
        var sl = serviceLoaderOf(Stream.of(providerOf(ImplA.class), providerOf(ImplAliasDup.class)));

        withMockedServiceLoader(sl, () -> {
            var ex = assertThrows(
                    IllegalStateException.class,
                    () -> ProviderLoader.loadProviders(TestSpi.class, ServiceLoader::load, true));
            assertTrue(ex.getMessage().contains("id or alias"));
            assertTrue(ex.getMessage().contains("a"));
            assertTrue(ex.getMessage().contains(ImplA.class.getName()));
            assertTrue(ex.getMessage().contains(ImplAliasDup.class.getName()));
        });
    }

    @Test
    void missingProviderAnnotation_throwsImmediately() {
        var sl = serviceLoaderOf(Stream.of(providerOf(ImplNoAnno.class)));

        withMockedServiceLoader(sl, () -> {
            var ex = assertThrows(
                    IllegalStateException.class,
                    () -> ProviderLoader.loadProviders(TestSpi.class, ServiceLoader::load, true));
            assertTrue(ex.getMessage().contains("missing the @Provider annotation"));
            assertTrue(ex.getMessage().contains(TestSpi.class.getSimpleName()));
        });
    }

    @Nested
    class PriorityAndStabilityChecks {
        @Provider(id = "same", priority = 1)
        static class P1 implements TestSpi {
            @Override
            public String id() {
                return "P1";
            }
        }

        @Provider(id = "same", priority = 2)
        static class P2 implements TestSpi {
            @Override
            public String id() {
                return "P2";
            }
        }

        @Test
        void lowerPriorityWins_orderedBefore_higherPriority_butStillErrorOnDuplicateKey() {
            var sl = serviceLoaderOf(Stream.of(providerOf(P1.class), providerOf(P2.class)));

            withMockedServiceLoader(sl, () -> {
                var ex = assertThrows(
                        IllegalStateException.class,
                        () -> ProviderLoader.loadProviders(TestSpi.class, ServiceLoader::load, true));
                assertTrue(ex.getMessage().contains("same"));
                assertTrue(ex.getMessage().contains(P1.class.getName()));
                assertTrue(ex.getMessage().contains(P2.class.getName()));
            });
        }
    }
}
