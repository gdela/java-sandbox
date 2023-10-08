package pl.gdela.bigdecimal;

import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.lang.Integer.parseInt;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.openjdk.jmh.annotations.Mode.SingleShotTime;

@Warmup(iterations = 20, time = 2)
@Measurement(iterations = 80, time = 2)
@Fork(1)
@BenchmarkMode(SingleShotTime)
@OutputTimeUnit(MICROSECONDS)
public class BigDecimalDeconstructionBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        @Param({
                "zero", "one", "0",
                "2", "10", "max_in_long", "20", // twenty is more than the number of digits in Long.MAX_VALUE
                "-2", "-10", "min_in_long", "-20" // twenty is more than the number of digits in Long.MIN_VALUE
        })
        String numOfDigits = "5";
        int scale = 2;

        // many instances to avoid the effect of decimal.precision() caching
        BigDecimal[] decimals = new BigDecimal[1_000_000];

        @Setup(Level.Iteration)
        public void setUp() {
            for (int i = 0; i < decimals.length; i++) {
                decimals[i] = switch (numOfDigits) {
                    case "zero" -> ZERO;
                    case "one" -> ONE;
                    case "0" -> BigDecimal.valueOf(0, scale);
                    case "max_in_long" -> BigDecimal.valueOf(Long.MAX_VALUE, scale);
                    case "min_in_long" -> BigDecimal.valueOf(Long.MIN_VALUE, scale);
                    default -> {
                        int digits = parseInt(numOfDigits.replace("-", ""));
                        var d = BigDecimal.valueOf(10, 1 - digits).subtract(ONE).scaleByPowerOfTen(-scale); // '9' repeated numOfDigit times
                        yield numOfDigits.charAt(0) != '-' ? d : d.negate();
                    }
                };
            }
        }
    }

    @Benchmark
    public long using_unscaled_big(MyState state) throws InterruptedException {
        long unscaledLong = 0;
        for (int i = 0; i < state.decimals.length; i++) {
            BigInteger unscaledBig = state.decimals[i].unscaledValue();
            if (unscaledBig.bitLength() <= 63) {
                unscaledLong += unscaledBig.longValue();
            } else {
                unscaledLong += -1; // dummy, as unscaled value won't fit in long
            }
        }
        return unscaledLong;
    }

    @Benchmark
    public long avoiding_unscaled_big(MyState state) throws InterruptedException {
        long unscaledLong = 0;
        for (int i = 0; i < state.decimals.length; i++) {
            BigDecimal decimal = state.decimals[i];
            BigInteger unscaledBig = null; // avoid getting it from BigDecimal, as non-inflated BigDecimal will have to create it
            boolean compactForm = decimal.precision() < 19; // less than nineteen decimal digits for sure fits in a long
            if (compactForm) {
                unscaledLong = decimal.scaleByPowerOfTen(decimal.scale()).longValue(); // best way to get unscaled long value without creating unscaled BigInteger on the way
            } else {
                unscaledBig = decimal.unscaledValue(); // get and remember for possible use in inflated form serialization
                compactForm = unscaledBig.bitLength() <= 63; // check exactly if unscaled value will fit in a long
                if (compactForm) {
                    unscaledLong += unscaledBig.longValue();
                } else {
                    unscaledLong += -1; // dummy, as unscaled value won't fit in long
                }
            }
        }
        return unscaledLong;
    }
}
