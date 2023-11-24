package pl.gdela.bounds;

public class CpuHeavy {

    private static final long SUM_FROM = 0;
    public static final long SUM_TO = 1_000_000;

    static long sum_with_one_var() {
        long sum = 0;
        for (long i = SUM_FROM; i < SUM_TO; i++) {
            sum += i;
        }
        return sum;
    }

    static long sum_with_2_vars() {
        long sum1 = 0;
        long sum2 = 0;
        for (long i = SUM_FROM; i < SUM_TO; i += 2) {
            sum1 += i;
            sum2 += i+1;
        }
        return sum1 + sum2;
    }

    static long sum_with_4_vars() {
        long sum1 = 0;
        long sum2 = 0;
        long sum3 = 0;
        long sum4 = 0;
        for (long i = SUM_FROM; i < SUM_TO; i += 4) {
            sum1 += i;
            sum2 += i+1;
            sum3 += i+2;
            sum4 += i+3;
        }
        return sum1 + sum2 + sum3 + sum4;
    }

    static long sum_with_6_vars() {
        long sum1 = 0;
        long sum2 = 0;
        long sum3 = 0;
        long sum4 = 0;
        long sum5 = 0;
        long sum6 = 0;
        for (long i = SUM_FROM; i < SUM_TO; i += 6) {
            sum1 += i;
            sum2 += i+1;
            sum3 += i+2;
            sum4 += i+3;
            sum5 += i+4;
            sum6 += i+5;
        }
        return sum1 + sum2 + sum3 + sum4 + sum5 + sum6;
    }

    static long sum_with_8_vars() {
        long sum1 = 0;
        long sum2 = 0;
        long sum3 = 0;
        long sum4 = 0;
        long sum5 = 0;
        long sum6 = 0;
        long sum7 = 0;
        long sum8 = 0;
        for (long i = SUM_FROM; i < SUM_TO; i += 8) {
            sum1 += i;
            sum2 += i+1;
            sum3 += i+2;
            sum4 += i+3;
            sum5 += i+4;
            sum6 += i+5;
            sum7 += i+6;
            sum8 += i+7;
        }
        return sum1 + sum2 + sum3 + sum4 + sum5 + sum6 + sum7 + sum8;
    }
}
