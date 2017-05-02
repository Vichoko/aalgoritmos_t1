package Algorithms.Sort;

import java.io.File;
import static Static.Constants.*;

/**
 * MergeSort adaptado para ordenar segmentos formateados como: (x1,y1,x2,y2)
 * 1° Fase: Se ordena todo lo que puede en memoria RAM.
 *      Se crean N/M=n/m runs ordenados en memoria RAM dejandolos en archivo temporal.
 *      Costo esperado: O(n) llevar todos los bloques a RAM ida y vuelta.
 *
 * 2° Fase: Merge
 *
 */



public class MergeSort {
    protected EAxis m_axis;
    protected FileInputStream m_inStream;
    protected FileOutputStream m_outStream;


    private File m_tempFile;

    public MergeSort(EAxis axis, File inFile, File outFile){
        m_axis = axis; // axis puede ser x o y, para cachar por que coordenada ordenar.
        m_inStream = new FileInputStream(inFile);
        m_outStream = new FileOutputStream(outFile);
    }

    public void sort(){
        if (m_axis == EAxis.X){ // ordenar por x

        } else { // ordenar por y

        }

    }
    private void runGeneration(){
        byte[] run = new byte[M]; // Tamano del run es de tamano de RAM
        m_inStream.read(run); // Se carga en RAM
        // TODO: ordenar en memoria interna.
        // TODO: Almacenar run en outfile.


    }
    private void runsMerge(){

    }

}
