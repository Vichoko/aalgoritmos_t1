package segment.dispatcher;

import static utils.Constants.B;

public class PointFileWriter extends FileWriter {

    public PointFileWriter(String pathname) {
        super(pathname);
    }

    public void savePoint(double x, double y){
        String sX = ""+truncateTo3dec(x);
        String sY = ""+truncateTo3dec(y);
        int lenString = sX.length()+sY.length()+3;
        checkCapacity(lenString);
        sb.append(sX).append(",").append(sY).append(",\n");
        bytesCount += lenString;
    }
}
