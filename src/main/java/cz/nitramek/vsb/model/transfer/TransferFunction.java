package cz.nitramek.vsb.model.transfer;

import java.util.function.DoubleUnaryOperator;

public interface TransferFunction extends DoubleUnaryOperator {

    TransferFunction BINARY = new BinaryTransfer();

    @Override
    default double applyAsDouble(double operand) {
        return transfer(operand);
    }

    double transfer(double input);

}
