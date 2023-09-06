package pl.gdela.concurrency;

import java.util.List;

class SynchronizedBlockBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new SynchronizedBlockBalancer(pool);
    }
}