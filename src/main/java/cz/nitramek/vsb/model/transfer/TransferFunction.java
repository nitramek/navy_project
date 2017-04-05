package cz.nitramek.vsb.model.transfer;

import java.util.function.DoubleBinaryOperator;

public interface TransferFunction extends DoubleBinaryOperator {

    TransferFunction BINARY = new BinaryTransfer();
    TransferFunction LOGISTIC = new LogisticTransfer();
    TransferFunction PERCEPTRON = new PerceptronTransfer();
    TransferFunction HYPERBOLIC = new HyperbolicTangensTransfer();
    TransferFunction SIGNUM = new SignumTranfer();

    @Override
    default double applyAsDouble(double left, double right) {
        return transfer(left, right);
    }


    double transfer(double input, double k);

}
