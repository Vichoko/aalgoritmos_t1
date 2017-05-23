package segment.dispatcher;

import static utils.Constants.B;
import static utils.Constants.INTERS_COUNTER;

public class PointFileWriter extends FileWriter {

    public PointFileWriter(String pathname) {
        super(pathname);
        // 8*2 + 3 = 16
        maxElements = B/16;
    }

    @Override
    public void setMaxBytesRAM(int maxBytesRAM) {
        this.maxElements = maxBytesRAM/16;
    }

    public void savePoint(double x, double y){
        INTERS_COUNTER++;
        checkCapacity();
        sb.append(truncateTo3dec(x)).append(",").append(truncateTo3dec(y)).append(",\n");
        elementsCount++;
    }
}
