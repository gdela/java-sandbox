package pl.gdela.concurrency;

import java.util.List;

import static java.lang.System.nanoTime;
import static java.lang.System.out;

class PrintAssembly {
    public static void main(String[] args) {
        Balancer balancer = new NonThreadSafeBalancer(List.of("A", "B", "C", "D"));
        long sum = 0;
        for (int j = 0; j < 10; j++) {
            long loops = 10_000_000;
            long start = nanoTime();
            for (int i = 0; i < loops; i++) {
                sum += balancer.getNext().length();
            }
            long stop = nanoTime();
            out.printf("average time per iteration [ns]: %.3f%n", ((stop-start) / 1.0d / loops));
        }
        out.println(sum);
    }
}
