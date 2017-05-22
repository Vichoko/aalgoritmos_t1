package algorithm;

import algorithm.sort.comparators.SegmentComparatorX;
import segment.Segment;
import segment.dispatcher.PointFileWriter;
import segment.dispatcher.SegmentDispatcher;
import segment.dispatcher.SegmentDispatcherTemporary;
import algorithm.sort.MergeSort;
import algorithm.sort.comparators.*;
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

    public void getIntersections(String filename) throws IOException {
        if (DEBUG) System.err.println("Creating answerFile " + filename);
        answerFile = new PointFileWriter(filename);
        if (DEBUG) System.err.println("Sorting file by X...");
        MergeSort xSort = new MergeSort(EAxis.X, inputFile, "_x_");
        String xSortFilename = xSort.sort();
        if (DEBUG) System.err.println("Sorting file by Y...");
        String ySortFilename = new MergeSort(EAxis.Y, inputFile, "_y_").sort();
        int verticalSegmentsNo = xSort.getVerticalSegmentsNo();
        if (DEBUG) System.err.println("Starting DS Secondary-Memory Recursion...");
        recursiveDistributionSweep(xSortFilename + ".tmp", 0, (int) inputFile.length(),0,Integer.MAX_VALUE,ySortFilename + ".tmp", verticalSegmentsNo);
        if (DEBUG) System.err.println(" Done Priamry-Memory recursion.");
        answerFile.close();
        if (DEBUG) System.err.println("Closed answerFile " + answerFile.getPathname());
    }
/************************************************************************************************************************************************** *
 *          SHARED METHODS
 ************************************************************************************************************************************************** *
 */
    /**
     * Should return memory usage of the software, based on the attributes recieved.
     *
     * @param beginOffset In bytes, offset in segment file where to start reading.
     * @param endOffset   In bytes, offset in segment file where to stop reading.
     * @return Size in bytes used by the software.
     */
    private int getMemorySize(int beginOffset, int endOffset) {
        // si es muy poco, duplicarlo o triplicarlo.
        // n.a. Viendo el codigo calculo que si se acaba la ram, hay que triplicar este valor.
        // n.a.2. me inclino que el tamaño podria ser el cuadrado de esto
        if (DEBUG && DEBUG_PM_TRIGGER) {
            DEBUG_PM_TRIGGER = false;
            System.err.println(" Done Secondary-Memory recursion.");
            System.err.println("Starting DS Primary-Memory Recursion...");
        }
        return endOffset - beginOffset;
    }

    /**
     * Search the slab where the x coordinate is placed
     * This method should be called for vertical segments
     *
     * @param i     x coordinate to be searched
     * @param slabs List of Slab objects
     * @return The index or -1 if it's not between these slabs
     */
    private int getIndexSlab(double i, ArrayList<Slab> slabs) {
        for (int j = 0; j < slabs.size(); j++) {
            if (i >= slabs.get(j).initX && i < slabs.get(j).finalX) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Search the slab where the x coordinate is placed.
     * This method should be called for vertical segments
     *
     * @param segment segment it x coordinate to be searched
     * @param slabs   List of Slab objects
     * @return The index or -1 if it's not between these slabs
     */
    private int getIndexSlab(Segment segment, ArrayList<Slab> slabs) {
        assert (segment.x1 == segment.x2); // vertical check
        for (int j = 0; j < slabs.size(); j++) {
            if (segment.x1 >= slabs.get(j).initX && segment.x1 < slabs.get(j).finalX) {
                return j;
            }
        }
        return -1;
    }


/************************************************************************************************************************************************** *
 *          SECONDARY-MEMORY ALGORITHMS
 ************************************************************************************************************************************************** *
 */

    /**
     * @param xSortedFilename        Archivo donde estan los segmentos ordenados por X.
     * @param beginOffset            Offset en bytes, desde donde se tiene que leer el archivo xSortedFilename
     * @param endOffset              Offset en bytes, hasta donde se tiene que leer el archvio xSortedFilename
     * @param ySortedFilename        Archivo dodne estan los segmentos ordenados por Y. Estos estan limitados al rango de offset por construccion.
     * @param verticalSegmentsNumber Cantidad de segmentos verticales dentro del rango de offset
     * @throws IOException
     */
    private void recursiveDistributionSweep(String xSortedFilename, int beginOffset, int endOffset, int beginIndex, int endIndex,
                                            String ySortedFilename, int verticalSegmentsNumber) throws IOException {

        // if fits in RAM
        if (getMemorySize(beginOffset, endOffset) < M) {
            // prepares the data for primary memory usage
            //ArrayList<Segment> xSortedSegments = extractXSortedArray(xSortedFilename, beginOffset, endOffset, beginIndex, endIndex);
            ArrayList<Segment> ySortedSegments = extractYSortedArray(ySortedFilename);
            ArrayList<Segment> xSortedSegments = ySortedSegments;
            xSortedSegments.sort(new SegmentComparatorX()); // cheap, few segments in most cases
            localRecursiveDistributionSweep(xSortedSegments, 0, xSortedSegments.size() - 1, ySortedSegments, verticalSegmentsNumber);

        } else {
            // Obs. max bytes offset 2^21 * 10*4 (each point is a double, 8 bytes + comas and points = 10 bytes)
            // => 8*10^7, it fit in an integer

            ArrayList<Slab> slabs = generateSlabs(xSortedFilename, verticalSegmentsNumber, beginOffset, endOffset, beginIndex, endIndex);
            // def slab dependant objects
            ArrayDeque<Segment>[] activeVerticals = new ArrayDeque[slabs.size()];
            RandomAccessFile[] activeVerticalFile = new RandomAccessFile[slabs.size()];
            // need auxiliary activeVertical for deletions
            ArrayDeque<Segment>[] activeVerticalsAux = new ArrayDeque[slabs.size()];
            RandomAccessFile[] activeVerticalFilesAux = new RandomAccessFile[slabs.size()];
            SegmentDispatcher[] yRecursiveSlabFiles = new SegmentDispatcher[slabs.size()];
            boolean[] horizontalNotComplete = new boolean[slabs.size()];
            for (int i = 0; i < slabs.size(); i++) { // init slab dependant objects
                activeVerticals[i] = new ArrayDeque<>();
                activeVerticalFile[i] = new RandomAccessFile(new File("activeVertical_" + i + ".txt"), "rw");

                activeVerticalsAux[i] = new ArrayDeque<>();
                activeVerticalFilesAux[i] = new RandomAccessFile(new File("activeVertical_" + (slabs.size() + i) + ".txt"), "rw");


                yRecursiveSlabFiles[i] = new SegmentDispatcherTemporary("yRecursiveSlabFiles" + i);

                yRecursiveSlabFiles[i].setMaxBytesRAM(MAX_SIZE_DISPATCHER);
                horizontalNotComplete[i] = false;
            }
            RandomAccessFile ySegmentsSorted = new RandomAccessFile(ySortedFilename, "r");
            int offsetY = 0;
            // sweep line
            if (DEBUG) System.err.println("Starting Sweep...");
            while (true) {
                UtilsIOSegments.ArrayBytesRead page = UtilsIOSegments.readPage(ySegmentsSorted, offsetY);
                ArrayList<Segment> segments = page.segments;
                offsetY += page.bytesRead;
                if (segments.size() == 0) break;
                for (Segment segment : segments) {
                    if (segment.isVertical()) {// vertical segment
                        assert (segment.x1 == segment.x2);
                        //int index = getIndexSlab(segment.x1, slabs);
                        int index = getIndexSlab(segment, slabs);
                        yRecursiveSlabFiles[index].saveSegment(segment); // save the segment for recursive call
                        addToActiveVerticals(activeVerticals[index], activeVerticalFile[index], segment);
                    } else {// horizontal segment
                        assert (segment.y1 == segment.y2);
                        // search first and last slab where the segment is complete
                        int[] index = getIndexSegment(segment, slabs, yRecursiveSlabFiles, horizontalNotComplete);
                        if (index != null)
                            writeIntersections(activeVerticals, activeVerticalFile, activeVerticalsAux, activeVerticalFilesAux, index[0], index[1], segment.y1);
                    }
                }
            }
            if (DEBUG) System.err.println("    Done.");
            if (DEBUG) System.err.println("Recursive call ->");
            // recursive call
            for (int i = 0; i < slabs.size(); i++)
                yRecursiveSlabFiles[i].close();
            for (int i = 0; i < slabs.size(); i++) {
                if (horizontalNotComplete[i])
                    recursiveDistributionSweep(xSortedFilename, slabs.get(i).initialOffset, slabs.get(i).finalOffset,
                            slabs.get(i).initialIndex, slabs.get(i).finalIndex,
                            yRecursiveSlabFiles[i].getPathname()+".tmp", slabs.get(i).verticalSegmentsNumber);
            }
        }
    }

    /**
     * Read a File of Segments to an ArrayList. Supossing it fits in RAM!
     * Used at RAM recursion step.
     *
     * @param filename    name of the file
     * @param beginOffset in bytes, where to start reading
     * @param endOffset   in bytes, where to stop reading
     * @return arrayList with the content of the file loaded in RAM.
     * @throws FileNotFoundException
     */
    private ArrayList<Segment> SegmentFileToArray(String filename, int beginOffset, int endOffset, int beginIndex, int endIndex) throws FileNotFoundException {
        ArrayList<Segment> array = new ArrayList<>();

        int offset = beginOffset;
        RandomAccessFile raf = new RandomAccessFile(filename, "r");
        while (true) {
            UtilsIOSegments.ArrayBytesRead page = (endOffset < 0 || endOffset == beginOffset) ? UtilsIOSegments.readPage(raf, offset) : UtilsIOSegments.readPage(raf, offset, endOffset);
            ArrayList<Segment> segments = page.segments;
            offset += page.bytesRead;
            if (segments.size() == 0) break;
            if (offset == page.bytesRead){
                // first page read
                for (int i = beginIndex; i < segments.size(); i++){
                    array.add(segments.get(i));
                }
            }
            else if (offset >= endOffset){
                //last page read
                for (int i = 0; i < endIndex && i < segments.size(); i++){
                    array.add(segments.get(i));
                }
            }
            else{
                array.addAll(segments);
            }
        }
        return array;
    }

    /**
     * Wrapper for converting the X coordinate sorted file to array in RAM.
     * Used at RAM recursion step.
     *
     * @param xSortedFilename
     * @param beginOffset
     * @param endOffset
     * @return
     * @throws FileNotFoundException
     */
    private ArrayList<Segment> extractXSortedArray(String xSortedFilename, int beginOffset, int endOffset, int beginIndex, int endIndex) throws FileNotFoundException {
        return SegmentFileToArray(xSortedFilename, beginOffset, endOffset, beginIndex, endIndex);
    }

    /**
     * Wrapper for converting the Y coordinate sorted file to array in RAM.
     * Used at RAM recursion step.
     *
     * @param ySortedFilename
     * @return
     * @throws FileNotFoundException
     */
    private ArrayList<Segment> extractYSortedArray(String ySortedFilename) throws FileNotFoundException {
        return SegmentFileToArray(ySortedFilename, 0, -1, 0, Integer.MAX_VALUE);
    }


    /**
     * Reads all the active verticals (list and file) between these slabs,
     * updates the active list and writes the intersections in the answer file
     *
     * @param activeVerticals    List with active verticals
     * @param activeVerticalFiles File with active verticals
     * @param slab_i             first slab where to check intersections
     * @param slab_j             last slab where to check intersections
     * @param sweepLineHeight    Height of sweep line
     */
    private void writeIntersections(ArrayDeque<Segment>[] activeVerticals,
                                    RandomAccessFile[] activeVerticalFiles,
                                    ArrayDeque<Segment>[] activeVerticalsAux,
                                    RandomAccessFile[] activeVerticalFilesAux,
                                    int slab_i, int slab_j, double sweepLineHeight) throws IOException {
        for (int slab = slab_i; slab <= slab_j; slab++) {
            // activeVerticalFileAux with updated content (After deletes)

            // check verticals in RAM
            for (Segment s : activeVerticals[slab]) {
                double y = Math.max(s.y1, s.y2);
                if (y >= sweepLineHeight) { // save intersection & mantain in active segments
                    answerFile.savePoint(s.x1, sweepLineHeight);
                    addToActiveVerticals(activeVerticalsAux[slab],
                            activeVerticalFilesAux[slab], s); // add to updated newActiveVertical list
                }
            }
            // check verticals in disk
            int offset = 0;
            while (true) {
                UtilsIOSegments.ArrayBytesRead read = UtilsIOSegments.readPage(activeVerticalFiles[slab], offset);
                ArrayList<Segment> segments = read.segments;
                if (segments.size() == 0) break;
                offset += read.bytesRead;
                for (Segment s : segments) {
                    double y = Math.max(s.y1, s.y2);
                    if (y >= sweepLineHeight) {
                        answerFile.savePoint(s.x1, sweepLineHeight);
                        addToActiveVerticals(activeVerticalsAux[slab],
                                activeVerticalFilesAux[slab], s); // add to updated newActiveVertical list
                    }
                }

                // idea traer buffer a RAM, despues devolverlo a disco, actualizado
            }
            // save reference
            RandomAccessFile swapAuxFile = activeVerticalFiles[slab];
            // update references with updated information
            activeVerticalFiles[slab] = activeVerticalFilesAux[slab];
            activeVerticals[slab] = activeVerticalsAux[slab];
            // update aux references & blank old data
            activeVerticalFilesAux[slab] = swapAuxFile;
            activeVerticalFilesAux[slab].setLength(0);
            activeVerticalsAux[slab] = new ArrayDeque<>();
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
     * @param yHorizontalFiles      Where save the horizontal segments that are not complete
     * @param horizontalNotComplete Where set to true the slabs that have segments not complete
     * @return first and last index where the segment is complete, if none then null
     */
    private int[] getIndexSegment(Segment segment, ArrayList<Slab> slabs, SegmentDispatcher[] yHorizontalFiles,
                                  boolean[] horizontalNotComplete) {
        double i = Math.min(segment.x1, segment.x2);
        double j = Math.max(segment.x1, segment.x2);
        int slab_i = getIndexSlab(i, slabs);
        int slab_j = getIndexSlab(j, slabs);
        if (slab_i == slab_j && !(i == slabs.get(slab_i).initX && j == slabs.get(slab_j).finalX)) {// case segment inside 1 slab not touching limits
            yHorizontalFiles[slab_i].saveSegment(segment);
            horizontalNotComplete[slab_i] = true;
            return null;
        }
        if (i > slabs.get(slab_i).initX && i < slabs.get(slab_i).finalX) { // not completely inside left slab
            yHorizontalFiles[slab_i].saveSegment(segment);
            horizontalNotComplete[slab_i] = true;
            slab_i++;
        }
        if (j > slabs.get(slab_j).initX && j < slabs.get(slab_j).finalX) {// not completely inside left slab
            yHorizontalFiles[slab_j].saveSegment(segment);
            horizontalNotComplete[slab_j] = true;
            slab_j--;
        }
        return new int[]{slab_i, slab_j};
    }

    /**
     * Calculates offset (initial and final) for each slab in the file
     * Generates slabs with equal amount of vertical segments
     * <p>
     * NECESITA ACTUALIZARSE
     *
     * @param xSortedFilename File with segments
     * @param beginOffset     Place in the file to start slabs
     * @param endOffset       Place in the file to end slabs
     * @return Array with slabs, each one with initial offset in 0 and final offset in 1
     * @throws IOException Error reading the file
     */
    private ArrayList<Slab> generateSlabs(String xSortedFilename, int verticalSegmentsNo, int beginOffset, int endOffset, int beginIndex, int endIndex) throws IOException {
        if (DEBUG) System.err.println("Generating slabs...");
        int k = M / B - 2; // have to be <= m
        if (k > verticalSegmentsNo) {
            k = verticalSegmentsNo;
        }
        int len = verticalSegmentsNo / k;

        ArrayList<Slab> slabs = new ArrayList<Slab>();

        RandomAccessFile xSegmentsSorted = new RandomAccessFile(xSortedFilename, "r");
        int offset = beginOffset;

        // slab creation vars
        int verticalCounter = 0;
        int offset_init = offset;
        double startX = 0;
        Segment lastVerticalSegmentSeen = null;
        double max_x = 0;
        int ind = Integer.MAX_VALUE;


        while (true) {
            UtilsIOSegments.ArrayBytesRead page = (beginOffset == endOffset) ? UtilsIOSegments.readPage(xSegmentsSorted, offset) : UtilsIOSegments.readPage(xSegmentsSorted, offset, endOffset);
            ArrayList<Segment> segments = page.segments;
            offset += page.bytesRead;
            int index_offset = 0;
            if (segments.size() == 0) break;
            int i, j;

            if (offset <= beginOffset){
                //first page read
                i = beginIndex;
                index_offset = i;
                j = segments.size();
            }
            else if (offset >= endOffset){ // no se si se cumpla
                //final page read
                i = 0;
                j = Math.min(endIndex, segments.size());            }
            else{
                i = 0;
                j = segments.size();
            }
            for (ind = i; ind < j; ind++) {
                Segment s = segments.get(ind);

                if (s.isVertical()) {
                    assert (s.x1 == s.x2); // if its vertical should pass
                    lastVerticalSegmentSeen = s;
                    verticalCounter++;

                    if (verticalCounter >= len) { //last vertical segment in slab
                        slabs.add(new Slab(offset_init, offset, index_offset, ind, verticalCounter, s, startX, s.x1));
                        //restart
                        verticalCounter = 0;
                        offset_init = offset;
                        startX = s.x1;
                    }
                }
                // find max_x to finish last slab
                double curr_maxx = Math.max(s.x1, s.x2);
                if (curr_maxx > max_x) {
                    max_x = curr_maxx;
                }
            }
        }



        slabs.set(slabs.size()-1, new Slab(
                slabs.get(slabs.size()-1).initialOffset, offset,
                slabs.get(slabs.size()-1).initialIndex, ind,
                slabs.get(slabs.size()-1).verticalSegmentsNumber + verticalCounter, lastVerticalSegmentSeen, slabs.get(slabs.size()-1).initX, Double.MAX_VALUE));


        if (DEBUG) System.err.println("    Done.");
        return slabs;
    }

    /************************************************************************************************************************************************** *
     *          PRIMARY-MEMORY ALGORITHMS
     ************************************************************************************************************************************************** *
     */


    /**
     * Ejecuta algoritmo 'Distribution Sweep' cuando los segmentos caben en memoria primaria.
     *
     * @param xSortedSegments        Lista de segmentos ordenados por X
     * @param beginIndex             Indice de xSortedSegments desde el cual se debe a leer.
     * @param endIndex               Indice de xSortedSegments hasta el cual se debe leer.
     * @param ySortedSegments        Lista de segmentos ordenados por Y
     * @param verticalSegmentsNumber Numero de segmentos verticales
     *                               <p>
     *                               <p>
     *                               N.A.: Es necesario dar los beginIndex y endIndex? Se podría cortar xSortedSegments.
     */
    private void localRecursiveDistributionSweep(ArrayList<Segment> xSortedSegments, int beginIndex, int endIndex,
                                                 ArrayList<Segment> ySortedSegments,
                                                 int verticalSegmentsNumber) {
        ArrayList<Slab> slabs = generateSlabsLocal(xSortedSegments, verticalSegmentsNumber, beginIndex, endIndex);

        ArrayDeque<Segment>[] activeVerticals = new ArrayDeque[slabs.size()];
        ArrayList<ArrayList<Segment>> slabRecursiveSegments = new ArrayList<>(slabs.size());
        boolean[] horizontalNotComplete = new boolean[slabs.size()];

        for (int i = 0; i < slabs.size(); i++) { // init slab dependant objects
            activeVerticals[i] = new ArrayDeque<>();
            slabRecursiveSegments.add(new ArrayList<>());
            horizontalNotComplete[i] = false;
        }
        int offsetY = 0;
        // sweep line

        for (Segment segment : ySortedSegments) {
            if (segment.isVertical()) {
                int index = getIndexSlab(segment, slabs);
                slabRecursiveSegments.get(index).add(segment); // Guarda segmento vertical en slabRecursiveSegments para la llamada recursiva. Se mantiene orden por Y.
                activeVerticals[index].add(segment); // Optimizacion: No es necesario que se guarde dos veces, solo en active verticals

            } else {
                assert (segment.y1 == segment.y2); // horizontal assert
                int[] index = getIndexIntervalLocal(segment, slabs, slabRecursiveSegments, horizontalNotComplete);
                if (index != null) {
                    writeIntersectionsLocal(activeVerticals, index[0], index[1], segment.y1);
                }
            }
        }
        // recursive call


            for (int i = 0; i < slabs.size(); i++) {

                if (horizontalNotComplete[i]){
                    if (slabs.get(i).verticalSegmentsNumber <= 1){
                        fastReduct(slabs.get(i), slabRecursiveSegments.get(i));
                    } else {
                    localRecursiveDistributionSweep(xSortedSegments, slabs.get(i).initialIndex, slabs.get(i).finalIndex,
                            slabRecursiveSegments.get(i), slabs.get(i).verticalSegmentsNumber);
                    }
                }
            }
        }



    /**
     * Finishes recursion calculating intersection by bruteforce.
     *
     * @param slab     Slab that contains, by construction, one segment.
     * @param segments Segments to compare for intersections.
     */
    private void fastReduct(Slab slab, ArrayList<Segment> segments) {
        assert (slab.verticalSegmentsNumber <= 1);
        Segment fSegment = slab.getFinalSegment();
        assert (fSegment.isVertical());
        double yi = Math.min(fSegment.y1, fSegment.y2);
        double yj = Math.max(fSegment.y1, fSegment.y2);
        for (Segment s : segments) {
            if (s.isHorizontal()) {
                double xi = Math.min(s.x1, s.x2);
                double xj = Math.max(s.x1, s.x2);

                if (fSegment.x1 >= xi && fSegment.x1 <= xj && s.y1 >= yi && s.y1 <= yj) {
                    // hay interseccion
                    answerFile.savePoint(fSegment.x1, s.y1);
                }
            }
        }
    }

    /**
     * For the slabs between slab_i & slab_j, it writes in the output the intersections detected in its activeVerticals.
     * SideEffect: delete inactive verticals (Out of sweepLine) from activeVerticals list.
     *
     * @param activeVerticals Active vertical list for every slab.
     * @param slab_i          Fist slab to check intersection
     * @param slab_j          Last slab to check intersection
     * @param sweepLineHeight Y coord. of the actual sweep lane.
     */
    private void writeIntersectionsLocal(ArrayDeque<Segment>[] activeVerticals, int slab_i, int slab_j, double sweepLineHeight) {
        for (int slab = slab_i; slab <= slab_j; slab++) {
            for (Segment s : activeVerticals[slab]) { // for this slab, report intersections
                if (s.y1 < sweepLineHeight) { // remove from active segments
                    activeVerticals[slab].remove(s);
                } else {
                    answerFile.savePoint(s.x1, sweepLineHeight); // TODO: Considerar guardarlo directamente en buffer en RAM
                }
            }
        }
    }

    /**
     * Obtiene indices (i,j) de los slabs en que el segmento esta contenido completamente.
     * <p>
     * Side-effect: En los slab que el segmento no cubre completamente, delega la responsabilidad de detectarlo a la recursion.
     * i.e. guarda el segmento en slabRecursiveSegments y setea horizontalNotComplete a 1.
     * <p>
     * Obs: El segmento que recibe es horizontal, por eso puede abarcar 1 o mas slabs.
     *
     * @param segment
     * @param slabs
     * @param slabRecursiveSegments
     * @param horizontalNotComplete
     * @return
     */
    private int[] getIndexIntervalLocal(Segment segment, ArrayList<Slab> slabs, ArrayList<ArrayList<Segment>> slabRecursiveSegments, boolean[] horizontalNotComplete) {
        double i = Math.min(segment.x1, segment.x2);
        double j = Math.max(segment.x1, segment.x2);
        int slab_i = getIndexSlab(i, slabs);
        int slab_j = getIndexSlab(j, slabs);

        if (slab_i == slab_j && !(i == slabs.get(slab_i).initX && j == slabs.get(slab_j).finalX)) {// case segment inside 1 slab not touching limits
            slabRecursiveSegments.get(slab_i).add(segment);
            horizontalNotComplete[slab_i] = true;
            return null;
        }
        if (i > slabs.get(slab_i).initX && i < slabs.get(slab_i).finalX) { // not completely inside left slab
            slabRecursiveSegments.get(slab_i).add(segment);
            horizontalNotComplete[slab_i] = true;
            slab_i++;
        }
        if (j > slabs.get(slab_j).initX && j < slabs.get(slab_j).finalX) {// not completely inside left slab
            slabRecursiveSegments.get(slab_j).add(segment);
            horizontalNotComplete[slab_j] = true;
            slab_j--;
        }
        return new int[]{slab_i, slab_j};
    }


    private ArrayList<Slab> generateSlabsLocal(ArrayList<Segment> xSortedSegments, int verticalSegmentsNumber, int beginIndex, int endIndex) {
        if (DEBUG) System.err.println("Generating slabs...");
        int k = M / (3*B); // if k > len k = len. rest: debe haber al menos 1 vertical por slab
        if (k > verticalSegmentsNumber) {
            k = verticalSegmentsNumber;
        }
        int len = (int) Math.ceil(verticalSegmentsNumber / k);
        ArrayList<Slab> slabs = new ArrayList<>();

        if (endIndex <= 0){
            //return slabs;
            int asd = 1;
            slabs = new ArrayList<>();

        }
        int verticalCounter = 0;

        int index_init = beginIndex;

        double startX = 0;
        double max_x = 0;
        Segment lastVerticalSegmentSeen = null;

        for (int index_current = beginIndex; index_current <= endIndex; index_current++) { // itera sobre el arreglo x, creando los slabs
            // considerar eliminar el sistema de indices
            Segment segment = xSortedSegments.get(index_current);
            if (segment.isVertical()) {
                lastVerticalSegmentSeen = segment;
                verticalCounter++;
                if (verticalCounter >= len) {
                    slabs.add(new Slab(0,0,index_init, index_current, verticalCounter, segment, startX, segment.x1));
                    // restart
                    verticalCounter = 0;
                    index_init = index_current;
                    startX = segment.x1;
                }
            }
            // find max_x to finish last slab
            double curr_maxx = Math.max(segment.x1, segment.x2);
            if (curr_maxx > max_x) {
                max_x = curr_maxx;
            }
        }

        slabs.set(slabs.size()-1, new Slab(0,0,
                slabs.get(slabs.size()-1).initialIndex,
                endIndex,
                slabs.get(slabs.size()-1).verticalSegmentsNumber + verticalCounter,
                lastVerticalSegmentSeen,
                slabs.get(slabs.size()-1).initX,
                Double.MAX_VALUE));

        if (DEBUG) System.err.println("    Done.");
        return slabs;
    }


/************************************************************************************************************************************************** *
 *          UNUSED METHODS
 ************************************************************************************************************************************************** *
 */

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

}