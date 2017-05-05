package sortalgorithms;

import java.io.*;
import java.util.ArrayList;

import segment.segment_dispatcher.SegmentDispatcher;
import segment.Segment;
import segment.segment_dispatcher.SegmentDispatcherTemporary;
import sortalgorithms.comparators.*;

import static utils.Constants.*;

import static java.lang.System.exit;


public class MergeSort {
    private FileInputStream m_inStream;
    private SegmentComparator segmentsComparator;
    private String outputFilename;

    public static void main(String[] args){
        MergeSort mergeSort = new MergeSort(EAxis.X);
        mergeSort.setInputFile(new File("1493943784947.txt"));
        String filename = mergeSort.sort();
        System.out.println("Segments sorted in "+filename);
    }


    /***
     * Sorts the segments in the file by the specified axis
     *
     * @param axis      Sorts by this coordinate
     */
    public MergeSort(EAxis axis){
        segmentsComparator = (axis==EAxis.X) ? new SegmentComparatorX() : new SegmentComparatorY();
    }

    public String sort(){
        // first stage
        long segmentsRead = 0;
        int runCount = 1; // id run
        while (segmentsRead < TOTAL_SEGMENTS){
            segmentsRead += readSortRun(runCount);
            runCount++;
        }
        // second stage: merge
        outputFilename = mergeRuns(runCount-1);
        return outputFilename;
    }

    public void setInputFile(File inFile){
        try{
            m_inStream = new FileInputStream(inFile);
        } catch (FileNotFoundException e){
            System.err.println("Mergesort:: inFile no se puede abrir");
            System.err.println(e.toString());
            exit(-1);
        }
    }

    public String getOutputFilename(){
        return outputFilename;
    }

    /***
     * Reads M bytes from the input file
     * Sorts them and saves them in a temporary file
     *
     * @return  Number of segments read
     */
    private int readSortRun(int runName){
        // read max RAM size
        byte[] run = new byte[M];
        try{
            m_inStream.read(run); // Se carga en RAM
        } catch (IOException e  ){
            System.err.println("Mergesort:: inFile no se pudo leer :/ ");
            System.err.println(e.toString());
            exit(-2);
        }
        // get Segment objects from byte data
        ArrayList<Segment> segments = getSegments(run);
        segments.sort(segmentsComparator);
        // Save temporary file
        saveSegmentsTempFile(segments, "Run_"+runName);
        return segments.size();
    }

    /**
     * Transforms the bytes read to float
     * Creates segment objects each 4 numbers
     *
     * @param run   Bytes from where to get the coordinates
     * @return      Segments in the run
     */
    private ArrayList<Segment> getSegments(byte[] run){
        StringBuilder sb = new StringBuilder();
        int points = 0;
        double[] coordinates = new double[4];
        ArrayList<Segment> segments = new ArrayList<>();
        for(byte b: run) {
            // end of bytes read
            if (b==0) break;
            char c = (char) b;
            if(c ==',') {
                coordinates[points] = Double.parseDouble(sb.toString());
                sb.setLength(0);
                points++;
            }
            else if (c != '\n')
                sb.append(c);
            if (points==4){
                Segment s = new Segment(coordinates[0], coordinates[1],
                        coordinates[2], coordinates[3]);
                segments.add(s);
                points = 0;
            }
        }
        return segments;
    }

    /***
     * Saves array of segments in a temporary file
     * @param segments  segments to be save
     * @param nameFile
     */
    private void saveSegmentsTempFile(ArrayList<Segment> segments, String nameFile) {
        SegmentDispatcher dispatcher = new SegmentDispatcherTemporary(nameFile);
        for (Segment segment: segments){
            dispatcher.saveSegment(segment.x1, segment.y1, segment.x2, segment.y2);
        }
        dispatcher.close();
    }

    /**
     * Merges files Run_i where i=1...totalRuns
     *
     * @param totalRuns Count of runs to be merged
     * @return          filename of temporary file the merged
     */
    private String mergeRuns(int totalRuns){
        int filesMerged = 0; // id run to be read next
        int m = M/B; // pages that fit in RAM
        int numberLastFile = totalRuns; // id for file name
        // break: when we've read all the runs and there's just one file left
        while(filesMerged < totalRuns && filesMerged+1 < numberLastFile) {
            FileInputStream[] inputs = new FileInputStream[m - 1];
            // read next m-1 while there is more runs to read
            int i;
            for (i= 0; i < m-1 && i+filesMerged < numberLastFile; i++) {
                try {
                    inputs[i] = new FileInputStream("Run_" + (i+1+filesMerged));
                } catch (FileNotFoundException e) {
                    System.err.println("Mergesort:: archivo temporal " + "Run_" + (i + filesMerged) + " no se pudo leer");
                    System.err.println(e.toString());
                    exit(-2);
                }
            }
            // Merge
            mergeMRuns(inputs, "Run_"+(++numberLastFile));
            filesMerged += i+1;
        }
        return "Run_"+numberLastFile;
    }

    /**
     * Creates a temporary file with the segments in the input files merged
     *
     * @param inputs    Files with segments sorted
     * @param nameFile  Name for the temporary file
     */
    private void mergeMRuns(FileInputStream[] inputs, String nameFile) {
        int m = M / B; // pages that fit in RAM
        int max_segments = B/32; // number of segments that fit in a page
        ArrayList<Segment> out = new ArrayList<>(max_segments);
        int out_index = 0;
        SegmentDispatcher fileOut = new SegmentDispatcherTemporary(nameFile);
        int[] index = new int[m - 1];
        // read a page from each run
        ArrayList<Segment>[] runs_page = new ArrayList[m - 1];
        while (true) {
            // find the minimum among all min elements of each run
            Segment min = null;
            int min_index = -1;
            for (int i = 0; i < m - 1; i++) {
                // there's no more elements of run_i
                if (index[i] == runs_page[i].size()) {
                    ArrayList<Segment> s = readPage(inputs[i]);
                    index[i] = (s == null) ? -1 : 0;
                }
                // there's still elements of run_i
                if (index[i] != -1) {
                    if (min == null) {
                        min = runs_page[i].get(index[i]);
                        min_index = i;
                    } else {
                        int result = segmentsComparator.compare(runs_page[i].get(index[i]), min);
                        if (result < 0) {
                            min = result < 0 ? runs_page[i].get(index[i]) : min;
                            min_index = i;
                        }
                    }
                }
            }
            // there're no more elements in none of the runs
            if (min == null) break;
            // add the minimum to out, "delete" it from the run
            index[min_index]++;
            // if no more space in out
            if (out_index == max_segments) {
                // save segments
                for (Segment segment: out)
                    fileOut.saveSegment(segment.x1, segment.y1, segment.x2, segment.y2);
                out_index = 0;
            }
            out.add(out_index, min);
            out_index++;
        }
        fileOut.close();
    }

    /***
     * Reads segments from an input file
     * @param input File from where to read
     * @return      List of segments read, null if no more to read
     */
    private ArrayList<Segment> readPage(FileInputStream input) {
        // B page size
        byte[] buffer = new byte[B];
        try {
            int bytesRead = input.read(buffer);
            if (bytesRead == -1) return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getSegments(buffer);
    }

}
