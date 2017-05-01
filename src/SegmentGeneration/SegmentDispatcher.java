package SegmentGeneration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Recibe un segmento (x1,y1,x2,y2) y la envía al outfile para quedar almacenado.
 */
public class SegmentDispatcher {
    private String m_outFile;
    private PrintWriter pw;

    public SegmentDispatcher(String outFile){
        m_outFile = outFile;

        try {
            File file = new File(outFile);
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double trunkto3dec(double src){
        return Math.floor(src*1000)/1000;
    }

    public void saveSegment(double x1, double y1, double x2, double y2){
        // format: "x1,x2,y1,y2,\n"

        String sx1 = Double.toString(trunkto3dec(x1));
        String sy1= Double.toString(trunkto3dec(y1));
        String sx2 = Double.toString(trunkto3dec(x2));
        String sy2= Double.toString(trunkto3dec(y2));

        pw.println(sx1 + "," + sy1 + "," + sx2 + "," + sy2 + ",");
    }

    public void close(){
        pw.close();
    }


}
