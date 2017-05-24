package segment.writer;

import segment.Segment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class SegmentWriter extends FileWriter{

    public SegmentWriter(String pathname){
        super(pathname);
        // 8bytes*4 + 5bytes = 37 bytes
        // B size page, B/37 = #segments that fit in a page
        try {
            file = new File(pathname+".txt");
            pw = new PrintWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean saveSegment(Segment segment){
        return saveSegment(segment.x1, segment.y1, segment.x2, segment.y2);
    }

    public boolean saveSegment(double x1, double y1, double x2, double y2){
        // format: "x1,x2,y1,y2,\n"
        String sx1 = Double.toString(truncateTo3dec(x1));
        String sy1= Double.toString(truncateTo3dec(y1));
        String sx2 = Double.toString(truncateTo3dec(x2));
        String sy2= Double.toString(truncateTo3dec(y2));
        int lenString = sx1.length()+sy1.length()+sx2.length()+sy2.length()+5;
        boolean b = checkCapacity(lenString);
        sb.append(sx1).append(",").append(sy1).append(",").append(sx2).append(",").append(sy2).append(",\n");
        bytesCount += lenString;
        return b;
    }
}

