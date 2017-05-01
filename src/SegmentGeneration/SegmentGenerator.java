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
        if (distr == EDistribution.UNIFORM){
            Random rand = new Random();
            // Generar N segmentos con distribucion de coordenadas uniformes. Balance dependiendo de a.

            //Genero los verticales
            for (int i = 0; i < a*n ; i++){

            }
            // Generar Horizontales
            for (int i = 0; i < (a-1)*n; i++){}

        } else {

        }
    }

    public static void main(String[] args){

    }
}
