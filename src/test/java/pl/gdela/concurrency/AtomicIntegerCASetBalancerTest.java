package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerCASetBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerCASetBalancer(pool);
    }
}