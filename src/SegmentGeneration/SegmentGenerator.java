package SegmentGeneration;

import SegmentGeneration.Random.IRandom;
import SegmentGeneration.Random.NormalRandom;
import SegmentGeneration.Random.UniformRandom;
import com.sun.deploy.xml.XMLAttribute;

import java.util.Random;
/**
 * Segmentos almacenados como tupla (x1,y1, x2,y2)
 *
 * Debe ser capaz de generar segmentos:
 * Cantidades: N = {2^9, ..., 2^21}
 * Distribucion de la coordenada x de los segmentos verticales: Uniforme, normal.
 * Distribucion resto de cooredenadas: Uniforme
 * Balance entre cantidad de segmentos verticales vs horizontales:
 *  aN horizontales, (1-a)N Verticales, a en {0.25, 0.5, 0.75}
 */

enum EDistribution{
    UNIFORM, NORMAL
}

public class SegmentGenerator {
    static final int YMAX = 100;
    static final int XMAX = 100;
	static final double normalMean = XMAX/2;
	static final double normalDesv = XMAX/7;

    int n;
    EDistribution distr;
    double a;



    public SegmentGenerator(int n, EDistribution distr, double a){
        this.n = n;
        this.distr = distr;
        this.a = a;
    }

    protected void GenerateSegments(){
        SegmentDispatcher dispatcher = new SegmentDispatcher(Long.toString(System.currentTimeMillis()) + ".txt");
        IRandom uniformXRand = new UniformRandom(XMAX);
        IRandom uniformYRand = new UniformRandom(YMAX);
        IRandom normalRand = new NormalRandom(normalMean, normalDesv);
        double x1, y1, x2, y2;

        if (distr == EDistribution.UNIFORM){
            // Generar N segmentos con distribucion de coordenadas uniformes. Balance dependiendo de a.
            //Genero los verticales
            for (int i = 0; i < a*n ; i++){
                x1 = uniformXRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = x1;
                y2 = uniformYRand.getRandom();
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
            // Generar Horizontales
            for (int i = 0; i < (1-a)*n; i++){
                x1 = uniformXRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = uniformXRand.getRandom();
                y2 = y1;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }

        } else {
            // Generar N segmentos con distribucion de coordenadas uniformes. Excepto la coordenada x de los verticales debe tener normal. Balance dependiendo de a.
            //Genero los verticales
            for (int i = 0; i < a*n ; i++){
                x1 = normalRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = x1;
                y2 = uniformYRand.getRandom();
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
            // Generar Horizontales
            for (int i = 0; i < (1-a)*n; i++){
                x1 = uniformXRand.getRandom();
                y1 = uniformYRand.getRandom();

                x2 = uniformXRand.getRandom();
                y2 = y1;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
        }
        dispatcher.close();
    }

    public static void main(String[] args){
        SegmentGenerator generator = new SegmentGenerator((int) Math.pow(2,9) , EDistribution.NORMAL, 0.5);

        generator.GenerateSegments();
        System.out.println(" done");
    }
}
