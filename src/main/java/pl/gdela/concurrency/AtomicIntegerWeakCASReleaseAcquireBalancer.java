package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

class AtomicIntegerWeakCASReleaseAcquireBalancer implements Balancer {

    private final List<String> pool;

    private final AtomicInteger next = new AtomicInteger();

    public AtomicIntegerWeakCASReleaseAcquireBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        // todo: understand Memory Order Modes, https://gee.cs.oswego.edu/dl/html/j9mm.html and see if this implementation makes sense
        int currIdx, nextIdx;
        do {
            currIdx = next.getAcquire();
            nextIdx = currIdx + 1 < pool.size() ? currIdx + 1 : 0;
        } while (!next.weakCompareAndSetRelease(currIdx, nextIdx));

        return pool.get(currIdx);
    }
}
