package pl.gdela.concurrency;

import java.util.List;

class ReentrantLockBalancerTest extends BalancerTest {

    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new ReentrantLockBalancer(pool);
    }
}