package pl.gdela.bigdecimal;

import org.openjdk.jol.info.GraphLayout;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.lang.System.out;

class BigDecimalMemoryEffects {

    public static void main(String[] args) {
        recreatedObjectSize();
        printedObjectSize();
    }

    private static void recreatedObjectSize() {
        out.println("--- recreating object ---");
        BigDecimal before = BigDecimal.valueOf(123456, 3); // same result with new BigDecimal("123.456");
        out.println("object size before: " + GraphLayout.parseInstance(before).totalSize());
        out.println("object footprint before:\n" + GraphLayout.parseInstance(before).toFootprint());
        byte[] unscaledValue = before.unscaledValue().toByteArray();
        int scale = before.scale();
        // recreate object
        var after = new BigDecimal(new BigInteger(unscaledValue), scale);
        if (!after.equals(before)) throw new RuntimeException("after should be equal to before");
        out.println("object size after: " + GraphLayout.parseInstance(after).totalSize());
        out.println("object footprint after:\n" + GraphLayout.parseInstance(after).toFootprint());
    }

    private static void printedObjectSize() {
        out.println("--- printing object ---");
        BigDecimal decimal = BigDecimal.valueOf(123_456_789, 3);
        out.println("object size before: " + GraphLayout.parseInstance(decimal).totalSize());
        out.println("object footprint before:\n" + GraphLayout.parseInstance(decimal).toFootprint());
        // print object
        out.println("decimal is " + decimal); // try instead decimal.toPlainString();
        out.println("object size after: " + GraphLayout.parseInstance(decimal).totalSize());
        out.println("object footprint after:\n" + GraphLayout.parseInstance(decimal).toFootprint());
    }
}
