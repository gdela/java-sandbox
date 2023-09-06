package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkArgument;

class SemaphoreBalancer implements Balancer {

    private final List<String> pool;

    private final Semaphore semaphore = new Semaphore(1);
    private int next = 0;

    public SemaphoreBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int idx;
        semaphore.acquireUninterruptibly();
        try {
            idx = next;
            next = idx + 1 < pool.size() ? idx + 1 : 0;
        } finally {
            semaphore.release();
        }
        return pool.get(idx);
    }
}
