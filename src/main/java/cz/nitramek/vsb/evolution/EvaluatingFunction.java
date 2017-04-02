package cz.nitramek.vsb.evolution;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public interface EvaluatingFunction {
    static double calculateSumForTwo(double[] params, DoubleBinaryOperator function) {
        return IntStream.rangeClosed(1, params.length - 1)
                .mapToDouble(i -> function.applyAsDouble(params[i - 1], params[i]))
                .sum();
    }

    static DoubleStream calculateForTwo(double[] params, DoubleBinaryOperator function) {
        return IntStream.rangeClosed(1, params.length - 1)
                .mapToDouble(i -> function.applyAsDouble(params[i - 1], params[i]));
    }

    static DoubleStream calculateForOne(double[] params, DoubleUnaryOperator function) {
        return Arrays.stream(params).map(function);
    }

    static double calculateSumForOne(double[] params, DoubleUnaryOperator function) {
        return Arrays.stream(params).map(function).sum();
    }

    double getValue(double... params);

    default String getName() {
        return this.getClass().getSimpleName();
    }

}
