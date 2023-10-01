# Hidden memory effects of BigDecimal

This article describes a bunch of things that you must be aware of when your application
is using `BigDecimal` objects.

## Forced BigDecimal inflation

Many serialization frameworks perform serialization and deserialization of `BigDecimal`
objects in a way similar to this:

```java
// serialization
BigDecimal decimal = ...; // object to serialize
byte[] unscaledValue = decimal.unscaledValue().toByteArray();
int scale = decimal.scale()
output.writeInt(unscaledValue.length);
for (int i = 0; i < unscaledValue.length; i++)
	output.writeByte(unscaledValue[i]);
output.writeInt(scale);
```

```java
// deserialization
int length = input.readInt();
byte[] unscaledValue = new byte[length];
for (int i = 0; i < length; i++)
    unscaledValue[i] = output.readByte();
int scale = input.readInt();
BigDecimal decimal = new BigDecimal(new BigInteger(unscaledValue), scale);
```

What's wrong with that would you ask? In the end that's what `BigDecimal` is – it consist
of an arbitrarily large integer value and scale which tells where to put the decimal point
in this value. Well there is a performance problem with memory and CPU usage. Here's
a simple test that may surprise you:

```java
var before = BigDecimal.valueOf(..., ...);
out.println("object size before: " + GraphLayout.parseInstance(before).totalSize());
byte[] unscaledValue = before.unscaledValue().toByteArray();
int scale = before.scale();
var after = new BigDecimal(new BigInteger(unscaledValue), scale);
assertEquals(before, after); // this is true
out.println("object size after: " + GraphLayout.parseInstance(after).totalSize());
```

Even though the `BigDecimal`s before and after and completely equal, the `before`
object is **40 bytes** in size, whereas the `after` object is **104 bytes** in size.
That's two and a half times more! There's no reason for the deserialization framework
to create a bigger object, when equally good will be a smaller object. Granted that
serialization frameworks typically strive for the smallest size of serialized form
of the object, but for some applications the size of the object after being
deserialized also matters. Especially when the application keeps the serialized
object for a longer period of time, for example caches some financial information
for later use.

Looking under the hood what's in the `BigDecimal` class it becomes obvious what
happens - the good folks that implemented this class know that in majority of
the cases the values kept in `BigDecimal` won't be actually so big, and they
use a `long` to keep the unscaled value. Only when the unscaled value becomes
too big to fit in a `long`, they create the `BigInteger` object to keep the
unscaled value there. Internally this is called that the `BigDecimal` has been
inflated. But the `new BigDecimal(BigInteger unscaledValue, int scale)` constructor
always creates `BigDecimal` in the inflated form, even if the unscaled value would
fit in a `long`. This can be seen in the summary printed by [JOL](https://github.com/openjdk/jol)'s `toFootprint()` method:

``` 
object footprint before:
java.math.BigDecimal@b1bc7edd footprint:
     COUNT       AVG       SUM   DESCRIPTION
         1        40        40   java.math.BigDecimal
         1                  40   (total)

object footprint after:
java.math.BigDecimal@6df97b55d footprint:
     COUNT       AVG       SUM   DESCRIPTION
         1        24        24   [I
         1        40        40   java.math.BigDecimal
         1        40        40   java.math.BigInteger
         3                 104   (total)
```

In the inflated form the `BigDecimal` object has reference to a `BigInteger` object,
and the `BigInteger` object (40 bytes) has reference to array of integers that constitute
the unscaled value. This is the `[I` object (24 bytes).

## Avoid BigDecimal inflation

To have the more compact form of `BigDecimal`, you have to use either `BigDecimal.valueOf(long
unscaledValue, int scale) ` factory method or the `new BigDecimal(String value)` constructor.
That's what serialization frameworks should use when deserializing `BigDecimal` and when
they determine that the unscaled value fits in a `long`. To determine it is pretty easy:

```java
BigInteger unscaledValue = decimal.unscaledValue()
boolean fitsInLong = unscaledValue.bitLength() <= 63; // as long is 64 bit and one bit is for the sign
BigDecimal decimal = fitsInLong ? BigDecimal.valueOf(unscaledValue.longValue(), scale) : new BigDecimal(unscaledValue, scale);
```

It would be best done even not in the deserialization phase, but in the serialization
phase, to avoid creating a temporary `BigInteger` instance on each deserialization.
Actually we can do even better - we can examine the `BigDecimal` object if it has
small enough value even without causing it to produce a `BigInteger` (in the majority
of the cases):

```java
BigInteger unscaledBig = null; // avoid getting it from BigDecimal, as non-inflated BigDecimal will have to create it
boolean compactForm = decimal.precision() < 19; // less than nineteen decimal digits for sure fits in a long
if (!compactForm) {
	unscaledBig = decimal.unscaledValue(); // get and remember for possible use in non-compact form
	compactForm = unscaledBig.bitLength() <= 63; // check exactly if unscaled value will fit in a long
}
```

Though then the fastest way to get the unscaled value as `long` without inflicting
`BigInteger` creation must be then a little bit convoluted:

```java
long unscaledValue = decimal.scaleByPowerOfTen(decimal.scale()).longValue();
```

We could do it more easily with accessing `BigDecimal`'s private field `intCompact`,
for example using a `VarHandle`. If it is different than `Long.MIN_VALUE` then it
means that the value of `intCompact` is the unscaled value, and `BigInteger` is not
needed for it. Otherwise `BigDecimal`'s private field `intVal` will already be initialized
with a `BigInteger` which is the unscaled value of `BigDecimal`. This has of course
downsides, because you break encapsulation, and have to use `--add-opens java.base/java.math=ALL-UNNAMED`,
and you are risking that your code will break with future versions of Java. In fact,
when you are reading this article the internal implementation of `BigDecimal` may be
already different. So let's toss this idea out.

## BigDecimal creation benchmarks

Even though the serialization/deserialization code that avoids inflating `BigDecimal`
is somewhat convoluted, it actually has a nice side effect of having better performance.
Here are results from my simple benchmark performed on OpenJDK 64-Bit Server VM, 17.0.8+7
in Windows 10 Pro running on Intel(R) Core(TM) i5-8400 CPU with 6 cores:

| BigDecimal's unscaled value range | baseline (noop) | classic serialization | optimized serialization |
| --------------------------------- | --------------: | --------------------: | ----------------------: |
| 0 to 999 999                      |         8 ns/op |              40 ns/op |                14 ns/op |
| 2000000 to 2999999                |         8 ns/op |              39 ns/op |                13 ns/op |
| 10 000 000 to 10 999 999          |         8 ns/op |              41 ns/op |                13 ns/op |
| 1 million below `Long.MAX_VALUE`  |         8 ns/op |              55 ns/op |                19 ns/op |
| 1 million above `Long.MAX_VALUE`  |         8 ns/op |              51 ns/op |                53 ns/op |

The "baseline" code is just returning the same instance of `BigDecimal`, the "classic"
code is always getting unscaled value as `BigInteger` and uses it to recreate `BigDecimal`,
the "optimized" code examines `BigDecimal` and when possible uses unscaled value as `long`,
avoiding creation of `BigDecimal` on both sides - during serialization and deserialization.
See [source code](https://github.com/gdela/java-sandbox/tree/master/src/test/java/pl/gdela/bigdecimal)
for details.

There was lot of variation in the results of this benchmark, most probably due to GC,
so I took the best values among all the iterations of the benchmark that were made.
But the GC stats for a fifty-iterations run of the benchmark also tell a story:

- baseline: 48 GCs with total time 1.8 seconds
- classic: 106 GCs with total time 3.5 seconds
- optimized: 71 GCs with total time 2.5 seconds

For completeness, here are the results from benchmarking different ways of creating
a `BigDecimal`: the recommended one using `valueOf()`, when you have `long` unscaled
value and scale; the ones using constructor, when you either have `BigInteger` unscaled
value or need to create it; and the one where the value for `BigDecimal` comes from
a `String`. The results highly depend on the magnitude of unscaled value, i.e. how
large it is, so the unscaled value is a parameter in this benchmark:

```
Benchmark                                 (unscaledValue)  Mode  Cnt    Score    Error  Units

value_of                                                0  avgt    3    0,885 ±  0,021  ns/op
ctor_having_big_integer                                 0  avgt    3    4,141 ±  2,112  ns/op
ctor_creating_big_integer_from_long                     0  avgt    3    4,010 ±  0,850  ns/op
ctor_creating_big_integer_from_bytes                    0  avgt    3   11,274 ±  1,482  ns/op
ctor_having_string                                      0  avgt    3   14,536 ±  6,909  ns/op

value_of                                                1  avgt    3    4,080 ±  2,500  ns/op
ctor_having_big_integer                                 1  avgt    3    4,239 ±  0,920  ns/op
ctor_creating_big_integer_from_long                     1  avgt    3    4,586 ±  0,925  ns/op
ctor_creating_big_integer_from_bytes                    1  avgt    3   12,411 ±  7,313  ns/op
ctor_having_string                                      1  avgt    3   16,720 ±  2,102  ns/op

value_of                                        123456789  avgt    3    4,049 ±  0,920  ns/op
ctor_having_big_integer                         123456789  avgt    3    4,202 ±  0,099  ns/op
ctor_creating_big_integer_from_long             123456789  avgt    3   10,814 ±  4,594  ns/op
ctor_creating_big_integer_from_bytes            123456789  avgt    3   15,400 ±  1,181  ns/op
ctor_having_string                              123456789  avgt    3   30,481 ±  2,653  ns/op

value_of                              9223372036854775807  avgt    3    4,135 ±  1,173  ns/op
ctor_having_big_integer               9223372036854775807  avgt    3    4,355 ±  0,560  ns/op
ctor_creating_big_integer_from_long   9223372036854775807  avgt    3   10,925 ±  6,597  ns/op
ctor_creating_big_integer_from_bytes  9223372036854775807  avgt    3   20,982 ±  2,960  ns/op
ctor_having_string                    9223372036854775807  avgt    3  119,507 ± 12,859  ns/op

value_of                              9223372036854775817  avgt                  [impossible]
ctor_having_big_integer               9223372036854775817  avgt    3    3,885 ±  0,706  ns/op
ctor_creating_big_integer_from_long   9223372036854775817  avgt                  [impossible]
ctor_creating_big_integer_from_bytes  9223372036854775817  avgt    3   24,740 ± 13,161  ns/op
ctor_having_string                    9223372036854775817  avgt    3  119,263 ± 15,868  ns/op
```

The next to last group of results is for the unscaled value equal to `Long.MAX_VALUE`,
and the last group of result is for `Long.MAX_VALUE + 10`, that's why not all method
of creation were possible for this unscaled value.

## Printing `BigDecimal` makes it bigger

There's another surprise with `BigDecimal`. Let's do something very innocent - print
the value of `BigDecimal` to the logs:

```java
var decimal = BigDecimal.valueOf(123_456_789, 3);
out.println("object size before: " + GraphLayout.parseInstance(decimal).totalSize());
log.info("doing something with {}", decimal);
out.println("object size after: " + GraphLayout.parseInstance(decimal).totalSize());
```

This simple action of printing the object increased its size from **40 bytes** to **96
bytes**. That's because `BigDecimal` caches the result of `toString()` invocation for
later use, which kind of makes sense. Constructing string representation of `BigDecimal`
is somewhat costly, and developers often print objects to console or to log, or to
user-facing messages, so caching `toString()` result improves performance, when it's
used multiple times on the same instance. Usually applications forget `BigDecimal`
shortly after it was used, for example they load data from database or from another
system, perform some computations using it, then move on to do other things, so then
the `BigDecimal` can be garbage collected. But if your application remembers
`BigDecimal` objects for longer periods, unknowingly by just adding a log/debug
statement in your code you may increase memory usage twofold.

You may avoid this effect by using `toPlainString()` or `toEngineeringString()`
instead of `toString()`. In Java 17 they are not cached.

## Summary

The `BigDecimal` is a very useful utility, especially for financial applications,
as it enables arbitrarily large precision and values and full control over rounding.
I highly recommend using it, but in some cases, mostly when you keep `BigDecimal`
object longer than just for a single request, you may have to take into account
its memory effects. Knowing how it works under the hood, you may avoid some gotchas.