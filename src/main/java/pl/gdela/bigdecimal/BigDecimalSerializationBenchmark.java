package pl.gdela.bigdecimal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.lang.System.nanoTime;
import static java.lang.System.out;
import static java.math.BigDecimal.ONE;

/**
 * Simulates different ways to serialize and deserialize a BigDecimal object. For simplicity,
 * we are not actually writing and reading bytes, but we deconstruct a BigDecimal into primitive
 * values (ints, bytes, longs) and then reconstruct new BigDecimal  from those primitive values,
 * so that it is equal to the original one.
 */
class BigDecimalSerializationBenchmark {

    public static void main(String[] args) {
        for (int iterations = 0; iterations < 50; iterations++) {
            measureSerialization();
        }
    }

    /*
    Can't use JMH and benchmark just one decimal million of times, because the call to precision()
    first time has different cost than calling second and next times, as the precision is remembered
    by BigDecimal. Realistic case for serialization and deserialization is that BigDecimal objects
    are many and different to each other.
     */
    private static void measureSerialization() {
        int rangeSize = 1_000_000;
        var rangeStarts = List.of(0L, 10_000_000L, Long.MAX_VALUE - rangeSize, Long.MAX_VALUE);
        int scale = 0; // this does not affect performance at all

        BigDecimal[] decimals = new BigDecimal[rangeSize];
        int blackhole = 0;
        for (long rangeStart : rangeStarts) {
            BigDecimal current = BigDecimal.valueOf(rangeStart, scale);
            for (int i = 0; i < decimals.length; i++) {
                decimals[i] = current;
                current = current.add(ONE);
            }
            long startTime = nanoTime();
            for (int i = 0; i < decimals.length; i++) {
                BigDecimal copy = serializeDeserializeNoop(decimals[i]);
                //BigDecimal copy = serializeDeserializeClassic(decimals[i]);
                //BigDecimal copy = serializeDeserializeOptimized(decimals[i]);
                blackhole += copy.signum();
                decimals[i] = copy;
            }
            long stopTime = nanoTime();
            out.printf(
                    "- %3d ns/op average for %d decimals from %s to %s%n",
                    (stopTime-startTime) / decimals.length, decimals.length, decimals[0], decimals[decimals.length-1]);
        }
        out.println("blackhole: " + blackhole);
    }

    private static BigDecimal serializeDeserializeNoop(BigDecimal decimal) {
        return decimal; // no serialization/deserialization, just return same instance
    }

    private static BigDecimal serializeDeserializeClassic(BigDecimal decimal) {
        // serialization
        int scale = decimal.scale();
        byte[] unscaledValueBytes = decimal.unscaledValue().toByteArray();
        // deserialization
        BigInteger unscaledValue = new BigInteger(unscaledValueBytes);
        return new BigDecimal(unscaledValue, scale);
    }

    private static BigDecimal serializeDeserializeOptimized(BigDecimal decimal) {
        // serialization
        int scale = decimal.scale();
        BigInteger unscaledBig = null; // avoid getting it from BigDecimal, as non-inflated BigDecimal will have to create it
        boolean compactForm = decimal.precision() < 19; // less than nineteen decimal digits for sure fits in a long
        if (!compactForm) {
            unscaledBig = decimal.unscaledValue(); // get and remember for possible use in non-compact form
            compactForm = unscaledBig.bitLength() <= 63; // check exactly if unscaled value will fit in a long
        }
        byte[] unscaledValueBytes;
        long unscaledValueLong;
        if (compactForm) {
            unscaledValueLong = decimal.scaleByPowerOfTen(decimal.scale()).longValue();
            unscaledValueBytes = null;
        } else {
            unscaledValueBytes = unscaledBig.toByteArray();
            unscaledValueLong = 0;
        }
        // deserialization
        if (compactForm) {
            return BigDecimal.valueOf(unscaledValueLong, scale);
        } else {
            BigInteger unscaledValue = new BigInteger(unscaledValueBytes);
            return new BigDecimal(unscaledValue, scale);
        }
    }
}
