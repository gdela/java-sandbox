package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerWeakCASRABalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerWeakCASReleaseAcquireBalancer(pool);
    }
}