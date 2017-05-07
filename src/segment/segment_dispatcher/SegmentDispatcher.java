package segment.segment_dispatcher;

import segment.Segment;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import static utils.Constants.*;

abstract class FileWriter{
    PrintWriter pw;
    StringBuilder sb;
    private String pathname;
    int maxElements;
    int elementsCount;

    public FileWriter(String pathname) {
        this.pathname = pathname;
        sb = new StringBuilder();
        elementsCount = 0;
    }

    public String getPathname() {
        return pathname;
    }

    abstract public void setMaxBytesRAM(int maxBytesRAM);

    public void close(){
        if (sb.length()!=0)
            writePage();
        pw.close();
    }

    void checkCapacity(){
        if (elementsCount+1 >= maxElements){
            writePage();
            elementsCount = 0;
            sb.setLength(0);
        }
    }

    void writePage(){
        pw.write(sb.toString());
    }
    double truncateTo3dec(double src){
        return Math.floor(src*1000)/1000;
    }
}

public abstract class SegmentDispatcher extends FileWriter{


    public SegmentDispatcher(String pathname){
        super(pathname);
        // 8bytes*4 + 5bytes = 37 bytes
        // B size page, B/37 = #segments that fit in a page
        maxElements = B/37;
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

