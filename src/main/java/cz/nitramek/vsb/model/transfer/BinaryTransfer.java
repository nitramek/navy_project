package cz.nitramek.vsb.model.transfer;


public class BinaryTransfer implements TransferFunction {
    @Override
    public double transfer(double input) {
        return input > 0 ? input : 0;
    }
}
