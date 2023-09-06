package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerBalancer(pool);
    }
}