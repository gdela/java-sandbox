package pl.gdela.bounds;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.HdrHistogram.Histogram;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;

import static java.lang.System.nanoTime;
import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static java.lang.invoke.MethodType.methodType;

public class Measure {

    @Parameter(description = "Class name and method name to be measured, for example MyClass.myMethod")
    private String target;

    @Parameter(names = "-i", required = true, description = "Number of iterations, how many times to call the method")
    private int iterations;

    @Parameter(names = "-w", description = "Number of warmups to do before actual measurement")
    private int warmups = 0;

    @Parameter(names = "-h", description = "Whether to capture call times and print histogram")
    private boolean withHistogram = false;

    @Parameter(names = "-s", description = "Whether to capture call times and print simple stats")
    private boolean withStats = false;

    private static final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private static final Runtime runtime = Runtime.getRuntime();

    private long maxSampleTime;

    private Histogram histogram;
    private int[] statsLowRange;
    private int[] statsHighRange;
    private int statsOutsideRange;

    public static void main(String[] args) throws Throwable {
        out.printf("have %d processors%n", runtime.availableProcessors());
        out.printf("method main() entered at %d ms%n", runtimeBean.getUptime());
        Measure main = new Measure();
        JCommander.newBuilder().addObject(main).build().parse(args);
        main.run();
    }

    private void run() throws Throwable {
        String targetClass = target.split("\\.")[0];
        String targetMethod = target.substring(targetClass.length() + 1);
        Class<?> clazz = Class.forName("pl.gdela.bounds." + targetClass);
        var lookup = MethodHandles.lookup();
        var method = lookup.findStatic(clazz, targetMethod, methodType(long.class));

        if (withHistogram) histogram = new Histogram(1000, 1_000_000_000L, 3);
        if (withStats) { statsLowRange = new int[100]; statsHighRange = new int[100]; }

        out.printf("executions started at %d ms%n", runtimeBean.getUptime());
        if (warmups > 0) {
            for (int i = 1; i <= warmups; i++) {
                execute(method, iterations, i + "-warmup");
                out.printf("warmup %d finished at %d ms%n", i, runtimeBean.getUptime());
            }
            sleep(5000);
            out.printf("sleep after warmups finished at %d ms%n", runtimeBean.getUptime());
        }
        execute(method, iterations, "main");
        out.printf("measurement finished at %d ms%n", runtimeBean.getUptime());
        runtime.halt(0);
    }

    private void execute(MethodHandle method, long iterations, String name) throws Throwable {
        maxSampleTime = 0;
        if (withHistogram) histogram.reset();
        if (withStats) { Arrays.fill(statsLowRange, 0); Arrays.fill(statsHighRange, 0); statsOutsideRange = 0; }

        long startTime = nanoTime();
        long result = 0;
        for (long i = 0; i < iterations; i++) {
            long sampleStartTime = nanoTime();
            result += (long) method.invokeExact();
            long sampleFinishTime =  nanoTime();
            long sampleTime = sampleFinishTime - sampleStartTime;
            maxSampleTime = Math.max(maxSampleTime, sampleTime);
            if (withHistogram) histogram.recordValue(sampleTime);
            if (withStats) statsRecordValue(sampleTime);
        }
        long finishTime = nanoTime();
        out.printf("dummy result is %s%n", result);

        out.printf("%d executions took %.1f ms (%.3f ms per execution, %.3f ms maximum)%n",
                iterations,
                (finishTime-startTime) / 1000.0 / 1000.0,
                (finishTime-startTime) / 1000.0 / 1000.0 / iterations,
                maxSampleTime / 1000.0 / 1000.0
        );

        if (withHistogram) printHistogram(name);
        if (withStats) printStats();
    }

    private void printHistogram(String name) throws FileNotFoundException {
        String fileName = "histogram-" + name + ".log";
        double scalingRatio = 1_000_000.0;
        try (PrintStream log = new PrintStream(fileName)) {
            histogram.outputPercentileDistribution(log, scalingRatio);
        }
        out.printf("mean %.3f ms, std dev %.3f ms, %s%n",
                histogram.getMean() / scalingRatio,
                histogram.getStdDeviation() / scalingRatio,
                fileName
        );
        out.printf("min %.3f ms, med %.3f ms, max %.3f ms%n",
            histogram.getMinValue() / scalingRatio,
            histogram.getValueAtPercentile(0.5) / scalingRatio,
            histogram.getMaxValue() / scalingRatio
        );
        long minPlusSomeDelta = (long) (histogram.getMinValue() * 1.05);
        out.printf("%d samples greater than %.3f ms (%.3f percentile)%n",
                histogram.getCountBetweenValues(minPlusSomeDelta, histogram.getMaxValue()),
                minPlusSomeDelta / scalingRatio,
                100 - histogram.getPercentileAtOrBelowValue(minPlusSomeDelta)
        );
    }

    private void statsRecordValue(long sampleTime) {
        int diff = (int) (sampleTime / 1000 / 10);
        if (diff < statsLowRange.length) {
            statsLowRange[diff]++;
        }
        else if (diff/10 < statsHighRange.length) {
            statsHighRange[diff/10]++;
        }
        else {
            statsOutsideRange++;
        }
    }

    private void printStats() {
        for (int i = 0; i < statsLowRange.length; i++) {
            if (statsLowRange[i] > 0) {
                out.printf("%.2f ms: %d%n", i/100.0, statsLowRange[i]);
            }
        }
        for (int i = 0; i < statsHighRange.length; i++) {
            if (statsHighRange[i] > 0) {
                out.printf("%.1f ms: %d%n", i/10.0, statsHighRange[i]);
            }
        }
        if (statsOutsideRange > 0) {
            out.printf(">=10 ms: %d%n", statsOutsideRange);
        }
    }
}
