package pl.gdela.concurrency;

/**
 * Balances access to a list of strings so that each of the strings is used the same number of times.
 */
public interface Balancer {

    /**
     * Returns the next string to be used.
     */
    String getNext();
}
