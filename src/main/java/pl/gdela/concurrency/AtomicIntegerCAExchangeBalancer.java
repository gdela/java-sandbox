package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static pl.gdela.concurrency.BalancerUtils.busySpinWaitOperation;

class AtomicIntegerCAExchangeBalancer implements Balancer {

    private final List<String> pool;

    private final AtomicInteger index = new AtomicInteger();

    public AtomicIntegerCAExchangeBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int readIndex = index.get();
        for(;;) {
            int currIndex = readIndex;
            int nextIndex = currIndex + 1 < pool.size() ? currIndex + 1 : 0;
            readIndex = index.compareAndExchange(currIndex, nextIndex);
            if (readIndex == currIndex) break;
            busySpinWaitOperation();
        }
        return pool.get(readIndex);
    }
}
