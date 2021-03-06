package cz.nitramek.vsb.model.transfer;


import static java.lang.Math.exp;

public class LogisticTransfer implements TransferFunction {
    @Override
    public double transfer(double input, double k) {
        return 1.0 / (1 + exp(k * -input));
    }
}
