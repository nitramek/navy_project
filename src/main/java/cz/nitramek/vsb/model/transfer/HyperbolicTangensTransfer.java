package cz.nitramek.vsb.model.transfer;

import static java.lang.Math.exp;


public class HyperbolicTangensTransfer implements TransferFunction {
    @Override
    public double transfer(double input, double k) {
        double ex = exp(k * input);
        double eMinusx = exp(k * -input);
        return (ex - eMinusx) / (ex + eMinusx);
    }
}
