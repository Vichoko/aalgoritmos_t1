package sortalgorithm.comparators;

import segment.Segment;

/**
 * Created by constanzafierro on 04-05-17.
 */
public class SegmentComparatorY extends SegmentComparator{

    @Override
    public int compare(Segment o1, Segment o2) {
        int compare = Double.compare(Math.max(o1.y1, o1.y2), Math.max(o2.y1, o2.y2));
        // the vertical segments have precedence in equality
        // vertical > horizontal
        if (compare == 0){
            // both vertical
            if (o1.x1 == o1.x2 && o2.x1 == o2.x2) return 0;
            if (o1.x1 == o1.x2) return 1;
            return -1;
        }
        return compare;
    }
}
