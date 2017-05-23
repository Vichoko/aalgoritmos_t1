package segment.dispatcher;

import segment.Segment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;

import static java.lang.System.exit;
import static utils.Constants.*;

public class SegmentDispatcher extends FileWriter{

    public SegmentDispatcher(String pathname){
        super(pathname);
        // 8bytes*4 + 5bytes = 37 bytes
        // B size page, B/37 = #segments that fit in a page
        maxElements = B/37;
        try {
            file = new File(pathname+".txt");
            pw = new PrintWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setMaxBytesRAM(int maxBytesRAM) {
        this.maxElements = maxBytesRAM/37;
    }

    public void saveSegment(Segment segment){
        saveSegment(segment.x1, segment.y1, segment.x2, segment.y2);
    }

    public void saveSegment(double x1, double y1, double x2, double y2){
        // format: "x1,x2,y1,y2,\n"
        String sx1 = Double.toString(truncateTo3dec(x1));
        String sy1= Double.toString(truncateTo3dec(y1));
        String sx2 = Double.toString(truncateTo3dec(x2));
        String sy2= Double.toString(truncateTo3dec(y2));
        checkCapacity();
        sb.append(sx1).append(",").append(sy1).append(",").append(sx2).append(",").append(sy2).append(",\n");
        elementsCount++;
    }
}

