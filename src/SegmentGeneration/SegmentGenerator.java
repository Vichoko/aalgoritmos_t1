package SegmentGeneration;

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
        Random uniRand = new Random();
        NormalRandom normalRand = new NormalRandom(XMAX/2, XMAX/7);
        double x1, y1, x2, y2;

        if (distr == EDistribution.UNIFORM){
            // Generar N segmentos con distribucion de coordenadas uniformes. Balance dependiendo de a.
            //Genero los verticales
            for (int i = 0; i < a*n ; i++){
                x1 = uniRand.nextDouble()*XMAX;
                y1 = uniRand.nextDouble()*YMAX;

                x2 = x1;
                y2 = uniRand.nextDouble()*YMAX;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
            // Generar Horizontales
            for (int i = 0; i < (1-a)*n; i++){
                x1 = uniRand.nextDouble()*XMAX;
                y1 = uniRand.nextDouble()*YMAX;

                x2 = uniRand.nextDouble()*YMAX;
                y2 = y1;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }

        } else {
            // Generar N segmentos con distribucion de coordenadas uniformes. Excepto la coordenada x de los verticales debe tener normal. Balance dependiendo de a.
            //Genero los verticales
            for (int i = 0; i < a*n ; i++){
                x1 = normalRand.getGaussian();
                y1 = uniRand.nextDouble()*YMAX;

                x2 = x1;
                y2 = uniRand.nextDouble()*YMAX;
                dispatcher.saveSegment(x1, y1, x2, y2);
            }
            // Generar Horizontales
            for (int i = 0; i < (1-a)*n; i++){
                x1 = uniRand.nextDouble()*XMAX;
                y1 = uniRand.nextDouble()*YMAX;

                x2 = uniRand.nextDouble()*YMAX;
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
