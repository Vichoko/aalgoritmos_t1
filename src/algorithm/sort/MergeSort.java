package algorithm.sort;

import java.io.*;
import java.util.ArrayList;

import segment.dispatcher.SegmentDispatcher;
import segment.Segment;
import algorithm.sort.comparators.*;
import utils.UtilsIOSegments;

import static utils.Constants.*;

import static java.lang.System.exit;


public class MergeSort {
    private RandomAccessFile accessFile;
    private SegmentComparator segmentsComparator;
    private int verticalSegmentsNo;
    private String id;
    private long memoryAccesCount;


    public static void main(String[] args){
        MergeSort mergeSort = new MergeSort(EAxis.X, new File("1495494457641.txt"));
        String filename = mergeSort.sort();
        System.out.println("Segments sorted in "+filename);
        System.out.println("Number access secondary memory:"+mergeSort.getMemoryAccesCount());
    }

    /**
     *
     * @param axis      Axis to sort
     * @param inFile    File with the segments to be sorted
     * @param id        identifier for the files created
     */
    public MergeSort(EAxis axis, File inFile, String id){
        this.id = id;
        memoryAccesCount = 0;
        verticalSegmentsNo = -1;
        segmentsComparator = (axis==EAxis.X) ? new SegmentComparatorX() : new SegmentComparatorY();
        try{
            accessFile = new RandomAccessFile(inFile, "r");
        } catch (FileNotFoundException e){
            System.err.println("Mergesort:: inFile no se puede abrir");
            System.err.println(e.toString());
            exit(-1);
        }
    }

    public MergeSort(EAxis axis, File inFile){
        this(axis,inFile, Double.toString(System.currentTimeMillis()));
    }

    public int getVerticalSegmentsNo() {
        return verticalSegmentsNo;
    }

    public long getMemoryAccesCount() {
        return memoryAccesCount;
    }

    /***
     * Sorts the segments of the file specified in the constructor
     * @return the filename with the segments sorted
     */
    public String sort(){
        // first stage
        verticalSegmentsNo = 0;
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
        return mergeRuns(runCount);
    }

    /***
     * Reads M (size of RAM) bytes from the input file
     * Sorts them and saves them in a temporary file
     *
     * @return  Number of bytes read, number of segments read
     */
    private int[] readSortRun(int runName, long offset){
        // read max RAM size
        byte[] buffer = new byte[M];
        try{
            accessFile.seek(offset);
            accessFile.read(buffer); // Se carga en RAM
        } catch (IOException e  ){
            System.err.println("Mergesort:: inFile no se pudo leer :/ ");
            System.err.println(e.toString());
            exit(-2);
        }
        // get Segment objects from byte data
        UtilsIOSegments.ArrayBytesRead answer = UtilsIOSegments.getSegments(buffer);
        memoryAccesCount++;
        ArrayList<Segment> segments = answer.segments;
        int bytesRead = answer.bytesRead;
        segments.sort(segmentsComparator);
        // having segments in RAM, calculate number of vertical segments on the run.
        for (Segment s : segments){
            if (s.isVertical()){
                verticalSegmentsNo++;
            }
        }
        // Save temporary file
        UtilsIOSegments.saveSegmentsTempFile(segments, "Run_"+runName+"_"+id);
        memoryAccesCount++;
        return new int[]{bytesRead, segments.size()};
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
        SegmentDispatcher segmentDispatcher = null;
        // break: when we've read all the runs and there's just one file left
        while(filesMerged < totalRuns && filesMerged+1 < numberLastFile) {
            if (segmentDispatcher!=null) segmentDispatcher.setDeleteOnExit();
            ArrayList<RandomAccessFile> inputs = new ArrayList<>();
            // read next m-1 while there is more runs to read
            int i;
            for (i= 0; i < m-1 && i+filesMerged < numberLastFile; i++) {
                try {
                    inputs.add(i, new RandomAccessFile("Run_"+(i+1+filesMerged)+"_"+id+".txt", "r"));
                } catch (FileNotFoundException e) {
                    System.err.println("Mergesort:: archivo temporal " + "Run_"+(i+1+filesMerged)+"_"+id + " no se pudo leer");
                    System.err.println(e.toString());
                    exit(-2);
                }
            }
            // Merge
            segmentDispatcher = mergeMRuns(inputs, "Run_"+(++numberLastFile)+"_"+id);
            totalRuns++;
            filesMerged += i;
        }
        return "Run_"+numberLastFile+"_"+id;
    }

    /**
     * Creates a temporary file with the segments in the input files merged
     *  @param inputs    Files with segments sorted
     *  @param nameFile  Name for the temporary file
     */
    private SegmentDispatcher mergeMRuns(ArrayList<RandomAccessFile> inputs, String nameFile) {
        int totalInputs = inputs.size();
        SegmentDispatcher fileOut = new SegmentDispatcher(nameFile);
        fileOut.setMaxBytesRAM(B);
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
                    memoryAccesCount++;
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
            // "delete" it from the run
            indexArray[min_index]++;
            // add the minimum to out
            boolean savedInMemory = fileOut.saveSegment(min);
            if(savedInMemory) memoryAccesCount++;
        }
        boolean savedInMemory = fileOut.close();
        if(savedInMemory) memoryAccesCount++;
        return fileOut;
    }

}
