package Algorithms.Sort;

import java.io.*;

import static Static.Constants.*;
import static java.lang.System.exit;

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
    private EAxis m_axis;

    private FileInputStream m_inStream;
    private FileOutputStream m_outStream;


    private File m_tempFile;

    public MergeSort(EAxis axis, File inFile, File outFile){
        m_axis = axis; // axis puede ser x o y, para cachar por que coordenada ordenar.

        try{ // abrir files
            m_inStream = new FileInputStream(inFile);
            m_outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e){
            System.err.println("Mergesort:: inFile o outFile no se puede abrir");
            System.err.println(e.toString());
            exit(-1);
        }
    }

    public void sort(){
        if (m_axis == EAxis.X){ // ordenar por x

        } else { // ordenar por y

        }

    }
    private void runGeneration(){
        byte[] run = new byte[M]; // Tamano del run es de tamano de RAM
        try{
            m_inStream.read(run); // Se carga en RAM
        } catch (IOException e  ){
            System.err.println("Mergesort:: inFile no se pudo leer :/ ");
            System.err.println(e.toString());
            exit(-2);
        }
        // TODO: ordenar en memoria interna.
        // TODO: Almacenar run en tempFile.


    }
    private void runsMerge(){
        //TODO: Leer de tempFile
        //TODO: Hacer merge como sale en pdf e ir guardandolo en outFile
    }

}
