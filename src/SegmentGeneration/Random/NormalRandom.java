package SegmentGeneration.Random;

import java.util.Random;

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
        double res = mean + rand.nextGaussian()*variance;

        while (res > mean*2){ // arreglo para evitar que entregue coordenada > XMAX
            res = mean + rand.nextGaussian()*variance;
        }
        return res;
    }

}
