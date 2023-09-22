package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

class DummyFirstStringBalancer implements Balancer {

    private final List<String> pool;

    public DummyFirstStringBalancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        return pool.get(0);
    }
}
