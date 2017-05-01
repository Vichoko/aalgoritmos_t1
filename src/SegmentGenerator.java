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
    int n;
    EDistribution distr;
    int a;

    public SegmentGenerator(int n, EDistribution distr, int a){
        this.n = n;
        this.distr = distr;
        this.a = a;
    }
    
    public static void main(String[] args){

    }
}
