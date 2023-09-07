package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

class AtomicIntegerCASetBalancer implements Balancer {

    private final List<String> pool;

    private final AtomicInteger next = new AtomicInteger();

    public AtomicIntegerCASetBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int currIdx, nextIdx;
        do {
            currIdx = next.get();
            nextIdx = currIdx + 1 < pool.size() ? currIdx + 1 : 0;
        } while (!next.compareAndSet(currIdx, nextIdx));

        return pool.get(currIdx);
    }
}
