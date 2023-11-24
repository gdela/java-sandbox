package pl.gdela.concurrency;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MINUTES;

class BalancersMain {
    public static void main(String[] args) throws InterruptedException {
        //Balancer balancer = new NonThreadSafeBalancer(List.of("A", "B", "C", "D", "E"));
        Balancer balancer = new SynchronizedMethodBalancer(List.of("A", "B", "C", "D", "E"));
        //Balancer balancer = new AtomicIntegerCAExchangeBalancer(List.of("A", "B", "C", "D", "E"));
        ExecutorService executor = Executors.newCachedThreadPool();

        int numOfThreads = 4;
        for (int threadNr = 0; threadNr < numOfThreads; threadNr++) {
            executor.submit(() -> {
                int blackhole = 0;
                for (int i = 0; i < 500_000_000; i++) {
                    blackhole += balancer.getNext().length();
                }
                System.out.println(blackhole);
            });
        }
        executor.shutdown();
        executor.awaitTermination(5, MINUTES);
    }
}
