package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

class DummyOneStringBalancer implements Balancer {

    public DummyOneStringBalancer(List<String> pool) {
        // noop
    }

    @Override
    public String getNext() {
        return "Hardcoded";
    }
}
