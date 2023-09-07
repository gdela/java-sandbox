package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

class AtomicIntegerLambdaBalancer implements Balancer {

    private final List<String> pool;

    private final AtomicInteger next = new AtomicInteger();

    public AtomicIntegerLambdaBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int idx = next.getAndUpdate(currIdx -> currIdx + 1 < pool.size() ? currIdx + 1 : 0);
        return pool.get(idx);
    }
}
