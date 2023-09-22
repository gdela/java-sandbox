package pl.gdela.concurrency;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.openjdk.jmh.annotations.Mode.Throughput;

@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@BenchmarkMode(Throughput)
@OutputTimeUnit(MICROSECONDS)
public class BalancersBenchmark {

    private static final boolean REALISTIC = false;

    @State(Scope.Benchmark)
    public static class BalancersFactory {
        @Param({
                "DummyOneStringBalancer",
                "DummyFirstStringBalancer",
                "NonThreadSafeBalancer",
                "SynchronizedMethodBalancer",
                "SynchronizedBlockBalancer",
                "AtomicIntegerCASetBalancer",
                "AtomicIntegerCAExchangeBalancer",
                "AtomicIntegerLambdaBalancer",
                "SemaphoreBalancer",
        })
        public String balancerClass;

        private Balancer balancer;

        @Setup(Level.Trial)
        @SuppressWarnings("unchecked")
        public void setUp() throws ReflectiveOperationException {
            List<String> pool = List.of("Resource-A", "Resource-B", "Resource-C", "Resource-D", "Resource-E");
            Class<Balancer> clazz = (Class<Balancer>) Class.forName("pl.gdela.concurrency." + balancerClass);
            balancer = clazz.getConstructor(List.class).newInstance(pool);
        }
    }

    /**
     * Simulates how the items returned by balancer will is used.
     */
    private static String use(String item) {
        if (REALISTIC) {
            return item;
        } else {
            return item.toUpperCase();
        }
    }

    @Benchmark @Threads(1)
    public String _01_thread(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(2)
    public String _02_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(3)
    public String _03_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(4)
    public String _04_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(5)
    public String _05_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(6)
    public String _06_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(7)
    public String _07_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(8)
    public String _08_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(9)
    public String _09_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(10)
    public String _10_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(11)
    public String _11_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(12)
    public String _12_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }
}
