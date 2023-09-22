package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.List.copyOf;

class NonThreadSafeBalancer implements Balancer {

    private final List<String> pool;

    private int index = 0;

    public NonThreadSafeBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = copyOf(pool);
    }

    @Override
    public String getNext() {
        int i = index; // copy to avoid IndexOutOfBoundsException due to data races
        index = i + 1 < pool.size() ? i + 1 : 0;
        return pool.get(i);
    }
}
