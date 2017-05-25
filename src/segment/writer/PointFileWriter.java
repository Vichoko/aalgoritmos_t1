package segment.writer;

import java.io.IOException;

import static utils.Constants.INTERS_COUNTER;

public class PointFileWriter extends FileWriter {

    public PointFileWriter(String pathname) throws IOException {
        super(pathname);
    }

    public void savePoint(double x, double y){
        INTERS_COUNTER++;
        String sX = ""+truncateTo3dec(x);
        String sY = ""+truncateTo3dec(y);
        int lenString = sX.length()+sY.length()+3;
        checkCapacity(lenString);
        sb.append(sX).append(",").append(sY).append(",\n");
        bytesCount += lenString;
    }
}
