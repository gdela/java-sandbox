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

@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 10, time = 5)
@Fork(1)
@BenchmarkMode(Throughput)
@OutputTimeUnit(MICROSECONDS)
public class BalancersBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {
        @Param({
                "NonThreadSafeBalancer",
                "SynchronizedMethodBalancer",
                "SynchronizedBlockBalancer",
                "AtomicIntegerBalancer",
                "AtomicIntegerExchangeBalancer",
                "AtomicIntegerWeakCASPlainBalancer",
                "AtomicIntegerWeakCASReleaseAcquireBalancer",
                "SemaphoreBalancer",
        })
        public String balancerClass;

        private Balancer balancer;

        @Setup(Level.Iteration)
        @SuppressWarnings("unchecked")
        public void setUp() throws ReflectiveOperationException {
            List<String> pool = List.of("A", "B", "C", "D", "E");
            Class<Balancer> clazz = (Class<Balancer>) Class.forName("pl.gdela.concurrency." + balancerClass);
            balancer = clazz.getConstructor(List.class).newInstance(pool);
        }
    }

    @Benchmark
    @Threads(1)
    public String _1_thread(MyState state) throws InterruptedException {
        return state.balancer.getNext();
    }

    @Benchmark
    @Threads(2)
    public String _2_threads(MyState state) throws InterruptedException {
        return state.balancer.getNext();
    }

    @Benchmark
    @Threads(4)
    public String _4_threads(MyState state) throws InterruptedException {
        return state.balancer.getNext();
    }

    @Benchmark
    @Threads(6)
    public String _6_threads(MyState state) throws InterruptedException {
        return state.balancer.getNext();
    }

    @Benchmark
    @Threads(8)
    public String _8_threads(MyState state) throws InterruptedException {
        return state.balancer.getNext();
    }

    @Benchmark
    @Threads(16)
    public String _16_threads(MyState state) throws InterruptedException {
        return state.balancer.getNext();
    }
}
