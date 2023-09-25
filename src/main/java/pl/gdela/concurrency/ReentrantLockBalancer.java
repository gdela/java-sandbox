package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkArgument;

class ReentrantLockBalancer implements Balancer {

    private final List<String> pool;

    private final ReentrantLock lock = new ReentrantLock();
    private int index = 0;

    public ReentrantLockBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int i;
        lock.lock();
        try {
            i = index++;
            if (index > pool.size()-1) index = 0;
        } finally {
            lock.unlock();
        }
        return pool.get(i);
    }
}
