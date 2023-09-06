package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;

class AtomicIntegerWeakCASPlainBalancer implements Balancer {

    private final List<String> pool;

    private AtomicInteger next = new AtomicInteger();

    public AtomicIntegerWeakCASPlainBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        // todo: understand Memory Order Modes, https://gee.cs.oswego.edu/dl/html/j9mm.html and see if this implementation makes sense
        int currIdx, nextIdx;
        do {
            currIdx = next.get();
            nextIdx = currIdx + 1 < pool.size() ? currIdx + 1 : 0;
        } while (!next.weakCompareAndSetPlain(currIdx, nextIdx));

        return pool.get(currIdx);
    }
}
