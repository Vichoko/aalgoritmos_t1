package utils.random;

import java.util.Random;

import static utils.Constants.*;

/**
 * Created by vicen on 01-05-2017.
 */
public class NormalRandom implements IRandom {
    private Random rand = new Random();
    public double mean;
    public double variance;

    public NormalRandom(double mean, double variance){
        this.mean = mean;
        this.variance = variance;
    }

    public double getRandom(){
        double number = mean + rand.nextGaussian()*variance;
        // avoid coordinate out of range
        while (number > X_MAX || number < 0){
            number = mean + rand.nextGaussian()*variance;
        }
        return number;
    }

}
