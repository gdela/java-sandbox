package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

class AtomicIntegerCAExchangeBalancer implements Balancer {

    private final List<String> pool;

    private final AtomicInteger next = new AtomicInteger();

    public AtomicIntegerCAExchangeBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int readIdx = next.get();
        int currIdx, nextIdx;
        do {
            currIdx = readIdx;
            nextIdx = currIdx + 1 < pool.size() ? currIdx + 1 : 0;
            readIdx = next.compareAndExchange(currIdx, nextIdx);
        } while (readIdx != currIdx);

        return pool.get(currIdx);
    }
}
