package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static pl.gdela.concurrency.BalancerUtils.busySpinWaitOperation;

class AtomicIntegerCASetBalancer implements Balancer {

    private final List<String> pool;

    private final AtomicInteger index = new AtomicInteger();

    public AtomicIntegerCASetBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int readIndex;
        for(;;) {
            readIndex = index.get();
            int nextIndex = readIndex + 1 < pool.size() ? readIndex + 1 : 0;
            if (index.compareAndSet(readIndex, nextIndex)) break;
            busySpinWaitOperation();
        }
        return pool.get(readIndex);
    }
}
