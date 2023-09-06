package pl.gdela.concurrency;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class NonThreadSafeBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new NonThreadSafeBalancer(pool);
    }

    @Test
    @Override
    void is_thread_safe() throws InterruptedException {
        try {
            super.is_thread_safe();
            // it may happen that the distribution is equal by pure chance, so do not fail() here
        } catch (AssertionError thrown) {
            // suppress test failure, as non thread safe implementation is not expected to give equal distribution
            assertThat(thrown).hasMessageContaining("unequal distribution");
        }
    }
}