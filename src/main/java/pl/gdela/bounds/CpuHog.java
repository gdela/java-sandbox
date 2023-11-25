package pl.gdela.bounds;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import static com.google.common.util.concurrent.Uninterruptibles.awaitTerminationUninterruptibly;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class CpuHog {

    private enum GcPressure { HIGH_GC, LOW_GC }

    @Parameter(names = "-t", required = true, description = "Number of threads")
    private int numberOfThreads;

    @Parameter(names = "-d", required = true, description = "Duration in seconds")
    private int duration;

    @Parameter(names = "-g", description = "Level of GC pressure, either HIGH_GC or LOW_GC")
    private GcPressure gc = GcPressure.LOW_GC;

    public static void main(String[] args) throws Throwable {
        CpuHog main = new CpuHog();
        JCommander.newBuilder().addObject(main).build().parse(args);
        main.run();
    }

    private void run() throws ExecutionException, InterruptedException {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("cpu-hog-%d").build();
        var executor = newFixedThreadPool(numberOfThreads, threadFactory);

        List<Future<BigDecimal>> results = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            results.add(executor.submit(this::hog));
        }

        Thread.sleep(Duration.ofSeconds(duration));
        executor.shutdownNow();

        BigDecimal sum = ZERO;
        for (Future<BigDecimal> result : results) {
            sum = sum.add(result.get());
        }
        out.printf("dummy result is %s%n", sum);

        awaitTerminationUninterruptibly(executor);
    }

    private BigDecimal hog() {
        return switch (gc) {
            case HIGH_GC -> highGcHog();
            case LOW_GC -> lowGcHog();
        };
    }

    private static BigDecimal highGcHog() {
        BigDecimal value = BigDecimal.valueOf(Long.MAX_VALUE);
        while (!currentThread().isInterrupted()) {
            value = value.add(BigDecimal.ONE);
        }
        return value;
    }

    private static BigDecimal lowGcHog() {
        long value = 1;
        while (!currentThread().isInterrupted()) {
            for (long j = 0; j < 100; j++) {
                value += value * j;
            }
        }
        return BigDecimal.valueOf(value);
    }

}
