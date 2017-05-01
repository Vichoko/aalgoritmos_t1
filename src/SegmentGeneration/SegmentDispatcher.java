package SegmentGeneration;

import java.io.File;

/**
 * Recibe un segmento (x1,y1,x2,y2) y la envía al outfile para quedar almacenado.
 */
public class SegmentDispatcher {
    private String m_outFile;

    public SegmentDispatcher(String outFile){
        m_outFile = outFile;
    }

    public void sendSegment(double x1, double y1, double x2, double y2){
        //TODO: Guarda la tupla en el archivo outfile
    }


}
