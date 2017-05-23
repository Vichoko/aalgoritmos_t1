package utils;

import segment.Segment;

public class Slab {
    public int initialOffset, finalOffset;
    public int initialIndex, finalIndex;
    public double initX, finalX;

    public int verticalSegmentsNumber;
    private Segment lastVerticalSegment;

    /**
     *
     * @param initialOffset Where to star reading in the file
     * @param finalOffset Where to stop reading in the file
     * @param initialIndex Where to start reading in the first segments page
     * @param finalIndex Where to stop reading in the last segments page.
     * @param vsn Number of vertical segments in the slab
     * @param lastVerticalSegment Last vertical segment inside the slab
     * @param initX X coord where this slab starts
     * @param finalX X coord where this slab finish
     */
    public Slab(int initialOffset, int finalOffset, int initialIndex, int finalIndex, int vsn, Segment lastVerticalSegment, double initX, double finalX) {
        this.initialOffset = initialOffset;
        this.initialIndex = initialIndex;
        this.finalOffset = finalOffset;
        this.finalIndex = finalIndex;
        this.initX = initX;
        this.finalX = finalX;
        this.verticalSegmentsNumber = vsn;
        this.lastVerticalSegment = lastVerticalSegment;
    }

    public Segment getFinalSegment() {
        return lastVerticalSegment;
    }
}
