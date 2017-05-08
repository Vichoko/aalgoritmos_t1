package algorithm;

import segment.Segment;
import segment.dispatcher.PointFileWriter;
import segment.dispatcher.SegmentDispatcher;
import segment.dispatcher.SegmentDispatcherTemporary;
import algorithm.sort.MergeSort;
import utils.*;


import static utils.Constants.*;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;

import static java.lang.System.exit;


public class DistributionSweep {
    private RandomAccessFile accessFile;
    private File inputFile;
    private PointFileWriter answerFile;
    private final int MAX_SIZE_ACTIVE_LIST = 2 * (B - 26) / 3;
    private final int MAX_SIZE_DISPATCHER = (B - 26) / 3;

    public DistributionSweep(File inputFile) {
        this.inputFile = inputFile;
    }

    public void getIntersections(String filename) {
        answerFile = new PointFileWriter(filename);
        MergeSort xSort = new MergeSort(EAxis.X, inputFile);
        String xSortFilename = xSort.sort();
        String ySortFilename = new MergeSort(EAxis.Y, inputFile).sort();
        int verticalSegmentsNo = xSort.getVerticalSegmentsNo();

        try {
            recursiveDistributionSweep(xSortFilename, 0, (int) inputFile.length(), ySortFilename, verticalSegmentsNo);
        } catch (IOException e) {
            System.err.println("algorithm.DistributionSweep:: Error reading file");
            System.err.println(e.toString());
            exit(-1);
        }
        answerFile.close();
    }

    private void recursiveDistributionSweep(String xSortedFilename, int beginOffset, int endOffset,
                                            String ySortedFilename, int verticalSegmentsNumber) throws IOException {
        // it fits in RAM
        //noinspection StatementWithEmptyBody
        if (endOffset - beginOffset < M) {
            // TODO
        } else {
            // Obs. max bytes offset 2^21 * 10*4 (each point is a double, 8 bytes + comas and points = 10 bytes)
            // => 8*10^7, it fit in an integer
            Slab[] slabs = generateSlabs(xSortedFilename, verticalSegmentsNumber, beginOffset, endOffset);
            // def slab dependant objects
            ArrayDeque<Segment>[] activeVerticals = new ArrayDeque[slabs.length];
            RandomAccessFile[] activeVerticalFile = new RandomAccessFile[slabs.length];
            SegmentDispatcher[] yHorizontalFiles = new SegmentDispatcher[slabs.length];
            boolean[] horizontalNotComplete = new boolean[slabs.length];
            for (int i = 0; i < slabs.length; i++) { // init slab dependant objects
                activeVerticals[i] = new ArrayDeque<>();
                activeVerticalFile[i] = new RandomAccessFile(new File("activeVertical_" + i + ".txt"), "rw");
                yHorizontalFiles[i] = new SegmentDispatcherTemporary("yHorizontal_" + i);

                yHorizontalFiles[i].setMaxBytesRAM(MAX_SIZE_DISPATCHER);
                horizontalNotComplete[i] = false;
            }
            RandomAccessFile ySegmentsSorted = new RandomAccessFile(ySortedFilename, "r");
            int offsetY = 0;
            // sweep line
            while (true) {
                UtilsIOSegments.ArrayBytesRead page = UtilsIOSegments.readPage(ySegmentsSorted, offsetY);
                ArrayList<Segment> segments = page.segments;
                offsetY += page.bytesRead;
                if (segments.size() == 0) break;
                for (Segment segment : segments) {
                    if (segment.isVertical())
                    {// vertical segment
                        assert (segment.x1 == segment.x2);
                        int index = getIndexSlab(segment.x1, slabs);
                        addToActiveVerticals(activeVerticals[index], activeVerticalFile[index], segment);
                    }
                    else
                    {// horizontal segment
                        assert (segment.y1 == segment.y2);
                        // search first and last slab where the segment is complete
                        int[] index = getIndexSegment(segment, slabs, yHorizontalFiles, horizontalNotComplete);
                        if (index != null)
                            writeIntersections(activeVerticals, activeVerticalFile, index[0], index[1], segment.y1);
                    }
                }
            }
            // recursive call
            for (int i = 0; i < slabs.length; i++)
                yHorizontalFiles[i].close();
            for (int i = 0; i < slabs.length; i++) {
                if (horizontalNotComplete[i])
                    recursiveDistributionSweep(xSortedFilename, slabs[i].initialOffset, slabs[i].finalOffset,
                            yHorizontalFiles[i].getPathname(), slabs[i].verticalSegmentsNumber);
            }
        }
    }

    /**
     * Reads all the active verticals (list and file) between these slabs,
     * updates the active list and writes the intersections in the answer file
     *
     * @param activeVerticals    List with active verticals
     * @param activeVerticalFile File with active verticals
     * @param slab_i             first slab where to check intersections
     * @param slab_j             last slab where to check intersections
     * @param sweepLineHeight    Height of sweep line
     */
    private void writeIntersections(ArrayDeque<Segment>[] activeVerticals, RandomAccessFile[] activeVerticalFile,
                                    int slab_i, int slab_j, double sweepLineHeight) throws FileNotFoundException {
        for (int slab = slab_i; slab <= slab_j; slab++) {
            // new activeVerticalFile with updated content (After deletes)
            ArrayDeque<Segment> newActiveVertical = new ArrayDeque<>();
            RandomAccessFile newActiveVerticalFile = new RandomAccessFile(
                    new File("activeVertical_" + Integer.toString(slab) + Long.toString(System.currentTimeMillis())),
                    "rw");

            // check verticals in RAM
            for (Segment s : activeVerticals[slab]){
                if (s.y1 < sweepLineHeight) { // remove from active segments
                    activeVerticals[slab].remove(s);
                } else {
                    answerFile.savePoint(s.x1, sweepLineHeight);
                    addToActiveVerticals(newActiveVertical, newActiveVerticalFile, s); // add to updated newActiveVertical list
                }
            }
            // check verticals in disk
            int offset = 0;
            while (true) {
                UtilsIOSegments.ArrayBytesRead read = UtilsIOSegments.readPage(activeVerticalFile[slab], offset);
                ArrayList<Segment> segments = read.segments;
                if (segments.size() == 0) break;
                offset += read.bytesRead;
                for (Segment s : segments) {
                    if (s.y1 < sweepLineHeight) {// remove from active segments
                        segments.remove(s);
                    } else {
                        answerFile.savePoint(s.x1, sweepLineHeight);
                        addToActiveVerticals(newActiveVertical, newActiveVerticalFile, s); // add to updated newActiveVertical list
                    }
                }

                // idea traer buffer a RAM, despues devolverlo a disco, actualizado
            }
            // update references
            activeVerticalFile[slab] = newActiveVerticalFile;
            activeVerticals[slab] = newActiveVertical;
        }
    }

    /**
     * Load the deque with refreshed data from disk.
     */
    private void reloadLoadedActiveVerticals(ArrayDeque<Segment> activeVerticals, RandomAccessFile randomAccessFile) {
        activeVerticals.clear();
        int offset = 0;
        while (activeVerticals.size() < MAX_SIZE_ACTIVE_LIST) {
            UtilsIOSegments.ArrayBytesRead read = UtilsIOSegments.readPage(randomAccessFile, offset);
            ArrayList<Segment> segments = read.segments;
            if (segments.size() == 0) break;
            offset += read.bytesRead;
            for (Segment s : segments) {
                activeVerticals.addLast(s);
            }
        }
    }

    /**
     * Adds the segment to the active vertical list, if its full it writes in the file
     *
     * @param activeVertical   Where to add the segment
     * @param randomAccessFile Where to write the list if it's full
     * @param segment          The segment to be added
     */
    private void addToActiveVerticals(ArrayDeque<Segment> activeVertical, RandomAccessFile randomAccessFile,
                                      Segment segment) {
        StringBuilder toFile = new StringBuilder();
        if (activeVertical.size() + 1 >= MAX_SIZE_ACTIVE_LIST) {
            while (!activeVertical.isEmpty()) {
                Segment s = activeVertical.pollLast();
                toFile.append(s.x1).append(",").append(s.y1).append(",");
                toFile.append(s.x2).append(",").append(s.y2).append(",\n");
            }
            try {
                randomAccessFile.writeUTF(toFile.toString());
            } catch (IOException e) {
                System.err.println("algorithm.DistributionSweep:: Error writing in file");
                System.err.println(e.toString());
                exit(-2);
            }
        }
        activeVertical.add(segment);
    }

    /**
     * Returns slabs index where the segment is complete
     *
     * @param segment               Segment to place
     * @param slabs                 List of Slab objects
     * @param yHorizontalFiles      Where save the segments that are not complete
     * @param horizontalNotComplete Where set to true the slabs that have segments not complete
     * @return first and last index where the segment is complete, if none then null
     */
    private int[] getIndexSegment(Segment segment, Slab[] slabs, SegmentDispatcher[] yHorizontalFiles,
                                  boolean[] horizontalNotComplete) {
        double i = Math.min(segment.x1, segment.x2);
        double j = Math.max(segment.x1, segment.x2);
        int slab_i = getIndexSlab(i, slabs);
        int slab_j = getIndexSlab(j, slabs);
        if (slab_i == slab_j && !(i == slabs[slab_i].initX && j == slabs[slab_j].finalX)) {// case segment inside 1 slab not touching limits
            yHorizontalFiles[slab_i].saveSegment(segment);
            horizontalNotComplete[slab_i] = true;
            return null;
        }
        if (i > slabs[slab_i].initX && i < slabs[slab_i].finalX) { // not completely inside left slab
            yHorizontalFiles[slab_i].saveSegment(segment);
            horizontalNotComplete[slab_i] = true;
            slab_i++;
        }
        if (j > slabs[slab_j].initX && j < slabs[slab_j].finalX) {// not completely inside left slab
            yHorizontalFiles[slab_j].saveSegment(segment);
            horizontalNotComplete[slab_j] = true;
            slab_j--;
        }
        return new int[]{slab_i, slab_j};
    }

    /**
     * Search the slab where the x coordinate is placed
     *
     * @param i     x coordinate to be searched
     * @param slabs List of Slab objects
     * @return The index or -1 if it's not between these slabs
     */
    private int getIndexSlab(double i, Slab[] slabs) {
        for (int j = 0; j < slabs.length; j++) {
            if (i >= slabs[j].initX && i < slabs[j].finalX)
                return j;
        }
        return -1;
    }

    /**
     * Calculates offset (initial and final) for each slab in the file
     * Generates slabs with equal amount of vertical segments
     *
     * @param xSortedFilename File with segments
     * @param beginOffset     Place in the file to start slabs
     * @param endOffset       Place in the file to end slabs
     * @return Array with slabs, each one with initial offset in 0 and final offset in 1
     * @throws IOException Error reading the file
     */
    private Slab[] generateSlabs(String xSortedFilename, int verticalSegmentsNo, int beginOffset, int endOffset) throws IOException {
        int k = M / B - 2; // have to be <= m
        Slab[] slabs = new Slab[k];

        int len = verticalSegmentsNo / k;

        RandomAccessFile xSegmentsSorted = new RandomAccessFile(xSortedFilename, "r");
        int offset = beginOffset;

        // slab creation vars
        int verticalCounter = 0;
        int slabCounter = 0;
        int offset_init = offset;
        double startX = Double.MAX_VALUE;
        double lastXseen = Double.MIN_VALUE;

        while (true) {
            UtilsIOSegments.ArrayBytesRead page = UtilsIOSegments.readPage(xSegmentsSorted, offset, endOffset);
            ArrayList<Segment> segments = page.segments;
            offset += page.bytesRead;
            if (segments.size() == 0) break;
            for (Segment s : segments) {
                if (s.isVertical()) {
                    assert (s.x1 == s.x2); // if its vertical should pass
                    if (verticalCounter == 0) { // first vertical segment in slab
                        startX = s.x1;
                    }
                    lastXseen = s.x1; // short-term memory for outer scope
                    verticalCounter++;

                    if (verticalCounter >= len) { //last vertical segment in slab
                        slabs[slabCounter] = new Slab(offset_init, offset, startX, s.x1, verticalCounter);
                        //restart
                        verticalCounter = 0;
                        slabCounter++;
                        offset_init = offset;
                        startX = Double.MAX_VALUE;
                    }
                }
            }
        }
        if (verticalCounter != 0) { // add remaining to last slab
            assert (slabCounter == k - 1); // if theory is good, should pass
            slabs[slabCounter] = new Slab(
                    slabs[slabCounter].initialOffset,
                    offset,
                    slabs[slabCounter].initX,
                    lastXseen,
                    slabs[slabCounter].verticalSegmentsNumber + verticalCounter);
        }

        return slabs;
    }


    /**
     * Counts the lines in a file
     *
     * @param filename Name of the file
     * @param start    Place in the file where start counting
     * @param end      Place in the file where stop counting
     * @return Total count of lines in the file
     * @throws IOException When reading the file
     */
    private int countLines(String filename, int start, int end) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        file.seek(start);
        int linesCount = 0;
        boolean empty = true;
        int totalRead = 0;
        int toReadBytes = end - start;
        while (totalRead < toReadBytes) {
            byte[] buffer = new byte[M];
            file.read(buffer);
            for (int i = 0; i < M && totalRead < toReadBytes; i++) {
                empty = false;
                byte b = buffer[i];
                if (b == 0) break; // end of file
                totalRead++;
                if ((char) b == '\n') linesCount++;
            }
        }
        file.close();
        return (linesCount == 0 && !empty) ? 1 : linesCount;
    }
}
