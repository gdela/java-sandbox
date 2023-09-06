package pl.gdela.concurrency;

import java.util.List;

class SynchronizedMethodBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new SynchronizedMethodBalancer(pool);
    }
}