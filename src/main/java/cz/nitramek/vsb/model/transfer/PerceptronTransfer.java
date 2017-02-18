package cz.nitramek.vsb.model.transfer;


public class PerceptronTransfer implements TransferFunction {
    @Override
    public double transfer(double input) {
        return input > 0 ? input : 0;
    }
}
