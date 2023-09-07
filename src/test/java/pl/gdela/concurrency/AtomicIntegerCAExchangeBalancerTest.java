package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerCAExchangeBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerCAExchangeBalancer(pool);
    }
}