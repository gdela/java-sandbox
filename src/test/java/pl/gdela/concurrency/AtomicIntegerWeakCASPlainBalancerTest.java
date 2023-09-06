package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerWeakCASPlainBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerWeakCASPlainBalancer(pool);
    }
}