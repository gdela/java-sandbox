package pl.gdela.bounds;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.HdrHistogram.Histogram;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static java.lang.System.nanoTime;
import static java.lang.System.out;
import static java.lang.invoke.MethodType.methodType;

public class Measure {

    @Parameter(description = "Class name and method name to be measured, for example MyClass.myMethod")
    private String target;

    @Parameter(names = "-i", required = true, description = "Number of iterations, how many times to call the method")
    private int iterations;

    @Parameter(names = "-w", description = "Whether to do warmup before actual measurement")
    private boolean doWarmup = false;

    @Parameter(names = "-h", description = "Whether to capture call times and print histogram")
    private boolean useHistogram = false;

    private static final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    private Histogram histogram;

    public static void main(String[] args) throws Throwable {
        out.printf("# Method main() entered at %d ms%n", runtimeBean.getUptime());
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

        if (useHistogram) histogram = new Histogram(1000, 1_000_000_000L, 3);

        if (doWarmup) {
            execute(method, iterations / 2, "1st-warmup");
            out.printf("# Warmup 1 finished at %d ms%n", runtimeBean.getUptime());

            execute(method, iterations / 2, "2nd-warmup");
            out.printf("# Warmup 2 finished at %d ms%n", runtimeBean.getUptime());
        }

        execute(method, iterations, "main");
        out.printf("# Measurement finished at %d ms%n", runtimeBean.getUptime());
    }

    private void execute(MethodHandle method, long iterations, String name) throws Throwable {
        if (useHistogram) histogram.reset();

        long startTime = nanoTime();
        long result = 0;
        for (long i = 0; i < iterations; i++) {
            long sampleStartTime = nanoTime();
            result += (long) method.invokeExact();
            long sampleFinishTime =  nanoTime();
            if (useHistogram) histogram.recordValue(sampleFinishTime - sampleStartTime);
        }
        long finishTime = nanoTime();

        out.printf("# Dummy result is %s%n", result);

        out.printf("# Execution of %d iterations took %.1f ms (%.3f ms per iteration)%n",
                iterations,
                (finishTime-startTime) / 1000.0 / 1000.0,
                (finishTime-startTime) / 1000.0 / 1000.0 / iterations
        );

        if (useHistogram) {
            double scalingRatio = 1_000_000.0;
            String fileName = "histogram-" + name + ".log";
            try (PrintStream log = new PrintStream(fileName)) {
                histogram.outputPercentileDistribution(log, scalingRatio);
            }
            out.printf("# Min %.3f, Mean %.3f, Max %.3f, StdDev %.3f, %s%n",
                histogram.getMinValue() / scalingRatio,
                histogram.getMean() / scalingRatio,
                histogram.getMaxValue() / scalingRatio,
                histogram.getStdDeviation() / scalingRatio,
                fileName
            );
        }

    }
}
