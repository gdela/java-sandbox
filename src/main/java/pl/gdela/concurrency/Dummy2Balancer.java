package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

class Dummy2Balancer implements Balancer {

    private final List<String> pool;

    public Dummy2Balancer(List<String> pool) {
        checkArgument(!pool.isEmpty(), "pool is empty");
        this.pool = List.copyOf(pool);
    }

    @Override
    public String getNext() {
        return pool.get(0);
    }
}
