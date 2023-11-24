package pl.gdela.bounds;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@Warmup(iterations = 4, time = 2)
@Measurement(iterations = 6, time = 2)
@Fork(1)
@BenchmarkMode(AverageTime)
@OutputTimeUnit(MICROSECONDS)
public class CpuBenchmark {

    @Benchmark
    public long sum_with_one_var() throws InterruptedException {
        return CpuHeavy.sum_with_one_var();
    }

    @Benchmark
    public long sum_with_2_vars() throws InterruptedException {
        return CpuHeavy.sum_with_2_vars();
    }

    @Benchmark
    public long sum_with_4_vars() throws InterruptedException {
        return CpuHeavy.sum_with_4_vars();
    }

    @Benchmark
    public long sum_with_6_vars() throws InterruptedException {
        return CpuHeavy.sum_with_6_vars();
    }

    @Benchmark
    public long sum_with_8_vars() throws InterruptedException {
        return CpuHeavy.sum_with_8_vars();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CpuBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
