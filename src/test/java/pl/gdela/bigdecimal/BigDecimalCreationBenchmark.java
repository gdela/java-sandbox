package pl.gdela.bigdecimal;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.math.BigDecimal.TEN;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@BenchmarkMode(AverageTime)
@OutputTimeUnit(NANOSECONDS)
public class BigDecimalCreationBenchmark {

	@State(Scope.Benchmark)
	public static class ParamsSmall {
		@Param({"0", "1", "123456789", "9223372036854775807"}) // the last one is Long.MAX_VALUE
		public long unscaledLongValue;
		public BigInteger unscaledBigValue;
		public byte[] unscaledBytesValue;
		String valueString;

		@Param({"0", "2"})
		int scale = 2;

		@Setup(Level.Iteration)
		public void setUp() {
			unscaledBigValue = BigInteger.valueOf(unscaledLongValue);
			unscaledBytesValue = unscaledBigValue.toByteArray();
			valueString = BigDecimal.valueOf(unscaledLongValue, scale).toPlainString();
		}
	}

	@State(Scope.Benchmark)
	public static class ParamsBigger {
		public BigInteger unscaledBigValue;
		public byte[] unscaledBytesValue;
		String valueString;
		int scale = 2;

		@Setup(Level.Iteration)
		public void setUp() {
			BigDecimal decimal = BigDecimal.valueOf(Long.MAX_VALUE, scale).add(TEN);
			unscaledBigValue = decimal.unscaledValue();
			unscaledBytesValue = unscaledBigValue.toByteArray();
			valueString = decimal.toPlainString();
		}
	}

	@Benchmark
	public static BigDecimal value_of(ParamsSmall params) throws InterruptedException {
		return BigDecimal.valueOf(params.unscaledLongValue, params.scale);
	}

	@Benchmark
	public static BigDecimal ctor_having_big_integer(ParamsSmall params) throws InterruptedException {
		return new BigDecimal(params.unscaledBigValue, params.scale);
	}

	@Benchmark
	public static BigDecimal ctor_creating_big_integer_from_long(ParamsSmall params) throws InterruptedException {
		BigInteger unscaledValue = BigInteger.valueOf(params.unscaledLongValue);
		return new BigDecimal(unscaledValue, params.scale);
	}

	@Benchmark
	public static BigDecimal ctor_creating_big_integer_from_bytes(ParamsSmall params) throws InterruptedException {
		BigInteger unscaledValue = new BigInteger(params.unscaledBytesValue);
		return new BigDecimal(unscaledValue, params.scale);
	}

	@Benchmark
	public static BigDecimal ctor_having_string(ParamsSmall params) throws InterruptedException {
		return new BigDecimal(params.valueString);
	}

	@Benchmark
	public static BigDecimal BIGGER_ctor_having_big_integer(ParamsBigger params) throws InterruptedException {
		return new BigDecimal(params.unscaledBigValue, params.scale);
	}

	@Benchmark
	public static BigDecimal BIGGER_ctor_creating_big_integer_from_bytes(ParamsBigger params) throws InterruptedException {
		BigInteger unscaledValue = new BigInteger(params.unscaledBytesValue);
		return new BigDecimal(unscaledValue, params.scale);
	}

	@Benchmark
	public static BigDecimal BIGGER_ctor_having_string(ParamsBigger params) throws InterruptedException {
		return new BigDecimal(params.valueString);
	}
}
