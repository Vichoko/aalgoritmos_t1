package segment.segment_dispatcher;

import segment.Segment;

import java.io.PrintWriter;
import java.util.ArrayList;

import static utils.Constants.*;

/**
 * Recibe un segmento (x1,y1,x2,y2) y lo envía al outfile para que quede almacenado.
 */
public abstract class SegmentDispatcher{
    PrintWriter pw;
    private StringBuilder sb;
    private int segmentsCount;

    SegmentDispatcher(String nameFile){
        sb = new StringBuilder();
        segmentsCount = 0;
    }
    public void saveSegment(double x1, double y1, double x2, double y2){
        // format: "x1,x2,y1,y2,\n"
        String sx1 = Double.toString(truncateTo3dec(x1));
        String sy1= Double.toString(truncateTo3dec(y1));
        String sx2 = Double.toString(truncateTo3dec(x2));
        String sy2= Double.toString(truncateTo3dec(y2));
        // 8bytes*4 + 5bytes = 37 bytes
        // B size page, B/37 = #segments that fit in a page
        if (segmentsCount+1 >= B/37){
            writePage();
            segmentsCount = 0;
            sb.setLength(0);
        }
        sb.append(sx1).append(",").append(sy1).append(",").append(sx2).append(",").append(sy2).append(",\n");
        segmentsCount++;
    }
    public void close(){
        if (sb.length()==0)
            writePage();
        pw.close();
    }
    private void writePage(){
        pw.write(sb.toString());
    }
    private double truncateTo3dec(double src){
        return Math.floor(src*1000)/1000;
    }
}

