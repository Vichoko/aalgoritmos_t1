package utils.random;

import java.util.Random;

/**
 * Created by vicen on 01-05-2017.
 */
public class UniformRandom implements IRandom {
    private Random rand = new Random();
    private double max;

    public UniformRandom(double max){
        this.max = max;
    }

    public double getRandom(){
        return rand.nextDouble()*max;
    }

}
