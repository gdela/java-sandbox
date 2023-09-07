package pl.gdela.concurrency;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

class Dummy1Balancer implements Balancer {

    public Dummy1Balancer(List<String> pool) {
        // noop
    }

    @Override
    public String getNext() {
        return "X";
    }
}
