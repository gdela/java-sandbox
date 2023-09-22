package pl.gdela.concurrency;

final class BalancerUtils {

    /**
     * The operation that is performed after each unsuccessful attempt in a busy-spin
     * loop that some of the {@link Balancer} implementations do under the hood.
     */
    static void busySpinWaitOperation() {
        //Thread.yield();
        //Thread.onSpinWait();
        //LockSupport.parkNanos(1);
        //LockSupport.parkNanos(10);
        //LockSupport.parkNanos(100);
        //LockSupport.parkNanos(1000);
        //System.nanoTime();
    }
}
