package SegmentGeneration;

import java.util.Random;

/**
 * Created by vicen on 01-05-2017.
 */
public class NormalRandom {
    private Random rand = new Random();
    public double mean;
    public double variance;

    public NormalRandom(double mean, double variance){
        this.mean = mean;
        this.variance = variance;
    }

    public double getGaussian(){
        return mean + rand.nextGaussian()*variance;
    }

}
