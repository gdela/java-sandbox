package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkArgument;

class SemaphoreBalancer implements Balancer {

    private final List<String> pool;

    private final Semaphore semaphore = new Semaphore(1);
    private int index = 0;

    public SemaphoreBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int i;
        semaphore.acquireUninterruptibly();
        try {
            i = index++;
            if (index > pool.size()-1) index = 0;
        } finally {
            semaphore.release();
        }
        return pool.get(i);
    }
}
