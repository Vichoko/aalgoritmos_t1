package Algorithms.Sort;

import java.io.File;
import static Static.Constants.*;

/**
 * MergeSort adaptado para ordenar segmentos formateados como: (x1,y1,x2,y2)
 */



public class MergeSort {
    protected EAxis m_axis;
    protected File m_inFile;
    protected File m_outFile;

    public MergeSort(EAxis axis, File inFile, File outFile){
        m_axis = axis;
        m_inFile = inFile;
        m_outFile = outFile;
    }

    public void sort(){

    }

}
