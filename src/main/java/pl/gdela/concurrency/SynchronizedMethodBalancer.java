package pl.gdela.concurrency;

import java.util.List;

class SynchronizedMethodBalancer extends NonThreadSafeBalancer {

    public SynchronizedMethodBalancer(List<String> pool) {
        super(pool);
    }

    @Override
    public synchronized String getNext() {
        return super.getNext();
    }
}
