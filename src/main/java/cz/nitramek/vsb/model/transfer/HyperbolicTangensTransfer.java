package cz.nitramek.vsb.model.transfer;

import static java.lang.Math.exp;

/**
 * Created by Martin on 18.2.2017.
 */
public class HyperbolicTangensTransfer implements TransferFunction {
    @Override
    public double transfer(double input) {
        double ex = exp(input);
        double eMinusx = exp(-input);
        return (ex - eMinusx) / (ex + eMinusx);
    }
}
