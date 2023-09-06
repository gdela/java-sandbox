package pl.gdela.concurrency;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemaphoreBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new SemaphoreBalancer(pool);
    }
}