package cz.nitramek.vsb.model.transfer;


public class SignumTranfer implements TransferFunction {
    @Override
    public double transfer(double input, double k) {
        return (input >= 0) ? 1 : -1;
    }
}
