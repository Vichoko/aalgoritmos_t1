package utils;

public class Slab {
    // parece que no se ocupan los ofsset finalmente
    public int initialOffset, finalOffset;
    public double initX, finalX;

    public int verticalSegmentsNumber;

    public Slab(int initialOffset, int finalOffset, double initX, double finalX, int vsn) {
        this.initialOffset = initialOffset;
        this.finalOffset = finalOffset;
        this.initX = initX;
        this.finalX = finalX;
        this.verticalSegmentsNumber = vsn;
    }
}
