package sortalgorithm;

import java.io.*;
import java.util.ArrayList;

import segment.segment_dispatcher.SegmentDispatcher;
import segment.Segment;
import segment.segment_dispatcher.SegmentDispatcherTemporary;
import sortalgorithm.comparators.*;
import utils.UtilsIOSegments;

import static utils.Constants.*;

import static java.lang.System.exit;


public class MergeSort {
    private RandomAccessFile accessFile;
    private SegmentComparator segmentsComparator;
    private String outputFilename;

    public static void main(String[] args){
        MergeSort mergeSort = new MergeSort(EAxis.X, new File("1493999714093.txt"));
        String filename = mergeSort.sort();
        System.out.println("Segments sorted in "+filename);
    }


    /***
     * Sorts the segments in the file by the specified axis
     * Raises an error if no input file has been set
     *
     * @param axis      Sorts by this coordinate
     */
    public MergeSort(EAxis axis, File inFile){
        segmentsComparator = (axis==EAxis.X) ? new SegmentComparatorX() : new SegmentComparatorY();
        try{
            accessFile = new RandomAccessFile(inFile, "r");
        } catch (FileNotFoundException e){
            System.err.println("Mergesort:: inFile no se puede abrir");
            System.err.println(e.toString());
            exit(-1);
        }
    }

    public String sort(){
        // first stage
        long segmentsRead = 0;
        int runCount = 0; // id run
        long bytesRead = 0;
        while (segmentsRead < TOTAL_SEGMENTS){
            runCount++;
            int[] read = readSortRun(runCount, bytesRead);
            bytesRead += read[0];
            segmentsRead += read[1];
        }
        // second stage: merge
        outputFilename = mergeRuns(runCount);
        return outputFilename;
    }

    public String getOutputFilename(){
        return outputFilename;
    }

    /***
     * Reads M (size of RAM) bytes from the input file
     * Sorts them and saves them in a temporary file
     *
     * @return  Number of bytes read, number of segments read
     */
    private int[] readSortRun(int runName, long offset){
        // read max RAM size
        byte[] run = new byte[M];
        try{
            accessFile.seek(offset);
            accessFile.read(run); // Se carga en RAM
        } catch (IOException e  ){
            System.err.println("Mergesort:: inFile no se pudo leer :/ ");
            System.err.println(e.toString());
            exit(-2);
        }
        // get Segment objects from byte data
        UtilsIOSegments.ArrayBytesRead answer = UtilsIOSegments.getSegments(run);
        ArrayList<Segment> segments = answer.segments;
        int bytesRead = answer.bytesRead;
        segments.sort(segmentsComparator);
        // Save temporary file
        UtilsIOSegments.saveSegmentsTempFile(segments, "Run_"+runName);
        int[] myAnswer = {bytesRead, segments.size()};
        return myAnswer;
    }

    /**
     * Merges files Run_i where i=1...totalRuns, saves it in a temporary file
     *
     * @param totalRuns Count of runs to be merged
     * @return          filename of merge
     */
    private String mergeRuns(int totalRuns){
        int filesMerged = 0; // id run to be read next
        int m = M/B; // pages that fit in RAM
        int numberLastFile = totalRuns; // id for file name
        // break: when we've read all the runs and there's just one file left
        while(filesMerged < totalRuns && filesMerged+1 < numberLastFile) {
            ArrayList<RandomAccessFile> inputs = new ArrayList<>();
            // read next m-1 while there is more runs to read
            int i;
            for (i= 0; i < m-1 && i+filesMerged < numberLastFile; i++) {
                try {
                    inputs.add(i, new RandomAccessFile("Run_" + (i+1+filesMerged)+".tmp", "r"));
                } catch (FileNotFoundException e) {
                    System.err.println("Mergesort:: archivo temporal " + "Run_" + (i+1+filesMerged) + " no se pudo leer");
                    System.err.println(e.toString());
                    exit(-2);
                }
            }
            // Merge
            mergeMRuns(inputs, "Run_"+(++numberLastFile));
            filesMerged += i;
        }
        return "Run_"+numberLastFile;
    }

    /**
     * Creates a temporary file with the segments in the input files merged
     *
     * @param inputs    Files with segments sorted
     * @param nameFile  Name for the temporary file
     */
    private void mergeMRuns(ArrayList<RandomAccessFile> inputs, String nameFile) {
        int totalInputs = inputs.size();
        int max_segments = B/32; // number of segments that fit in a page
        ArrayList<Segment> out = new ArrayList<>(max_segments);
        int out_index = 0;
        SegmentDispatcher fileOut = new SegmentDispatcherTemporary(nameFile);
        int[] offset = new int[totalInputs];
        ArrayList<Segment>[] runs_page = new ArrayList[totalInputs];
        int[] indexArray = new int[totalInputs];
        for (int i = 0; i < totalInputs; i++) {
            runs_page[i] = new ArrayList<>();
        }
        while (true) {
            // find the minimum among all min elements of each run
            Segment min = null;
            int min_index = -1;
            for (int i = 0; i < totalInputs; i++) {
                // there's no more elements of run_i
                if (indexArray[i] == runs_page[i].size()) {
                    UtilsIOSegments.ArrayBytesRead answer = UtilsIOSegments.readPage(inputs.get(i), offset[i]);
                    runs_page[i] = answer.segments;
                    offset[i] += answer.bytesRead;
                    indexArray[i] = (runs_page[i].size()==0) ? -1 : 0;
                }
                // there's still elements of run_i
                if (indexArray[i] != -1) {
                    if (min == null) {
                        min = runs_page[i].get(indexArray[i]);
                        min_index = i;
                    } else {
                        int result = segmentsComparator.compare(runs_page[i].get(indexArray[i]), min);
                        if (result < 0) {
                            min = result < 0 ? runs_page[i].get(indexArray[i]) : min;
                            min_index = i;
                        }
                    }
                }
            }
            // there're no more elements in none of the runs
            if (min == null) break;
            // add the minimum to out, "delete" it from the run
            indexArray[min_index]++;
            // if no more space in out
            if (out_index == max_segments) {
                // save segments
                for (Segment segment: out)
                    fileOut.saveSegment(segment);
                out.clear();
                out_index = 0;
            }
            out.add(out_index, min);
            out_index++;
        }
        if (out.size() > 0){
            for (Segment segment: out)
                fileOut.saveSegment(segment);
        }
        fileOut.close();
    }

}
