package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerExchangeBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerExchangeBalancer(pool);
    }
}