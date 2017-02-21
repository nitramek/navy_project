package cz.nitramek.vsb;


public class Utils {
    public static void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
