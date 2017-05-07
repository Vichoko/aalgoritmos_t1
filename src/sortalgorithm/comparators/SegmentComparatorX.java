package sortalgorithm.comparators;

import segment.Segment;

public class SegmentComparatorX extends SegmentComparator{

    @Override
    public int compare(Segment o1, Segment o2) {
        return Double.compare(Math.min(o1.x1, o1.x2), Math.min(o2.x1, o2.x2));
    }
}
