package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.List.copyOf;

class SynchronizedMethodBalancer implements Balancer {

    private final List<String> pool;

    private int index = 0;

    public SynchronizedMethodBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = copyOf(pool);
    }

    @Override
    public synchronized String getNext() {
        String item = pool.get(index++);
        if (index > pool.size()-1) index = 0;
        return item;
    }
}
