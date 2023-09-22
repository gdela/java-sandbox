package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.List.copyOf;

class SynchronizedBlockBalancer implements Balancer {

    private final List<String> pool;

    private int index = 0;

    public SynchronizedBlockBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = copyOf(pool);
    }

    @Override
    public String getNext() {
        int i;
        synchronized (this) {
            i = index++;
            if (index > pool.size()-1) index = 0;
        }
        return pool.get(i);
    }
}
