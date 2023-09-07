package pl.gdela.concurrency;

import java.util.List;

class AtomicIntegerLambdaBalancerTest extends BalancerTest {
    
    @Override
    protected Balancer provideBalancer(List<String> pool) {
        return new AtomicIntegerLambdaBalancer(pool);
    }
}