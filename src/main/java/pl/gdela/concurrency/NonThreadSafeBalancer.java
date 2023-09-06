package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

class NonThreadSafeBalancer implements Balancer {

    private final List<String> pool;

    private int next = 0;

    public NonThreadSafeBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        int idx = next; // copy to avoid 'index out of bound' due to data races
        next = idx + 1 < pool.size() ? idx + 1 : 0;
        return pool.get(idx);
    }
}
