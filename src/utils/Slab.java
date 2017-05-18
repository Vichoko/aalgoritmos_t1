package utils;

import segment.Segment;

public class Slab {
    public int initialOffset, finalOffset;
    public double initX, finalX;

    public int verticalSegmentsNumber;
    private Segment lastVerticalSegment;

    public Slab(int initialOffset, int finalOffset, double initX, double finalX, int vsn, Segment lastVerticalSegment) {
        this.initialOffset = initialOffset;
        this.finalOffset = finalOffset;
        this.initX = initX;
        this.finalX = finalX;
        this.verticalSegmentsNumber = vsn;
        this.lastVerticalSegment = lastVerticalSegment;
    }

    public Segment getFinalSegment() {
        return lastVerticalSegment;
    }
}
