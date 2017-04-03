package cz.nitramek.vsb.evolution;

import java.util.Random;

import lombok.Data;

import static java.lang.Math.abs;

@Data
public class Boundary {
    public final double min;
    public final double max;

    public final double range;

    public Boundary(double min, double max) {
        this.min = min;
        this.max = max;
        this.range = abs(max - min);
    }

    /**
     * @return Random in interval {@literal <}min,max>
     */
    public double randomDouble(Random random) {
        return random.nextDouble() * this.getRange() + this.min;
    }

    /**
     * Creates new Random so in a cycle is not very good to use
     *
     * @return Random in interval {@literal <}min,max>
     */
    public double randomDouble() {
        return randomDouble(new Random());
    }

    /**
     * Creation method is casting random double to int
     *
     * @return Random in interval {@literal <}min,max>
     */
    public int randomInt(Random random) {
        return (int) this.randomDouble(random);
    }

    public int randomInt() {
        return (int) this.randomDouble(new Random());
    }

    public boolean isInBoundary(double x) {
        return x >= this.min && x <= this.max;
    }


    public double getInRange(double x) {
        double tmp = x;
        while (!isInBoundary(tmp)) {
            if (tmp <= this.min) {
                tmp += this.range;
            } else if (tmp >= this.max) {
                tmp -= this.range;
            }
        }
        return tmp;
    }
}
