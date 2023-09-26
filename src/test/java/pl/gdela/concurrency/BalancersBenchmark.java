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

import static java.util.concurrent.TimeUnit.*;
import static org.openjdk.jmh.annotations.Mode.*;

@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@BenchmarkMode(Throughput)
@OutputTimeUnit(MICROSECONDS)
public class BalancersBenchmark {

    @State(Scope.Benchmark)
    public static class BalancersFactory {
        @Param({
                "DummyOneStringBalancer",
                "DummyFirstStringBalancer",
                "NonThreadSafeBalancer",
                "SynchronizedMethodBalancer",
                "SynchronizedBlockBalancer",
                "SemaphoreBalancer",
                "ReentrantLockBalancer",
                "AtomicIntegerCASetBalancer",
                "AtomicIntegerCAExchangeBalancer",
                "AtomicIntegerLambdaBalancer",
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

    private enum UseType {DONOOP, GETLENGTH, SUMBYTES, TOUPPER}

    private static final UseType useType = UseType.DONOOP;

    /**
     * Simulates how the items returned by balancer will is used.
     */
    private static Object use(String item) {
        return switch (useType) {
            case DONOOP -> item;
            case GETLENGTH -> item.length();
            case SUMBYTES -> throw new UnsupportedOperationException("not yet implemented");
            case TOUPPER -> item.toUpperCase();
        };
    }

    @Benchmark @Threads(1)
    public Object _01_thread(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(2)
    public Object _02_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(3)
    public Object _03_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(4)
    public Object _04_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(5)
    public Object _05_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(6)
    public Object _06_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(7)
    public Object _07_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(8)
    public Object _08_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(9)
    public Object _09_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(10)
    public Object _10_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(11)
    public Object _11_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(12)
    public Object _12_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(13)
    public Object _13_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(14)
    public Object _14_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(15)
    public Object _15_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(16)
    public Object _16_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(17)
    public Object _17_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }

    @Benchmark @Threads(18)
    public Object _18_threads(BalancersFactory factory) throws InterruptedException {
        return use(factory.balancer.getNext());
    }
}
