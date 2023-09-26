package pl.gdela.concurrency;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.Collections.nCopies;
import static java.util.concurrent.Executors.callable;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.assertj.core.api.Assertions.assertThat;

abstract class BalancerTest {

    private final ExecutorService executor = newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("balancer-test-thread-%d").build());

    protected abstract Balancer provideBalancer(List<String> pool);

    @Test
    void balances_uniformly() {
        // given
        List<String> pool = List.of("A", "B");
        var balancer = provideBalancer(pool);

        // when
        String r1 = balancer.getNext();
        String r2 = balancer.getNext();
        String r3 = balancer.getNext();
        String r4 = balancer.getNext();

        // then
        assertThat(r1).isIn("A", "B"); // both are possible, as none was yet used
        assertThat(r2).isIn("A", "B").isNotEqualTo(r1); // one was used, so it must be the other
        assertThat(r3).isIn("A", "B"); // again both are possible as both were used one time each
        assertThat(r4).isIn("A", "B").isNotEqualTo(r3); // one was used again, so it must be the other
    }

    @Test
    void is_thread_safe() throws InterruptedException {
        // given
        List<String> pool = List.of("A", "B", "C", "D");
        var balancer = provideBalancer(pool);
        int numOfIterations = 1000;
        int numOfParallelTasks = 2 * getRuntime().availableProcessors();

        // when
        var histogram = new TreeMap<String, LongAdder>();
        pool.forEach(item -> histogram.put(item, new LongAdder()));

        var task = callable(() -> {
            for (int i = 0; i < pool.size() * numOfIterations; i++) {
                String item = balancer.getNext();
                histogram.get(item).increment();
            }
        });
        executor.invokeAll(nCopies(numOfParallelTasks, task));

        // then
        out.printf("got %s from %s\n", histogram, balancer.getClass().getSimpleName());
        pool.forEach(item -> {
            int timesItemReturned = (int) histogram.get(item).sum();
            assertThat(timesItemReturned)
                    .overridingErrorMessage("unequal distribution: %s", histogram)
                    .isEqualTo(numOfParallelTasks * numOfIterations);
        });
    }
}