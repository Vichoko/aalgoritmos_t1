package test;

import algorithm.*;
import segment.Segment;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.*;
import java.util.ArrayList;

import static utils.Constants.TOTAL_SEGMENTS;

/**
 * Created by vicen on 22-05-2017.
 */
public class acurracyTest {
    public static void main(String[] args) throws IOException {
        // Variables
        // TODO: Parsing?
        System.out.println("Starting acurracyTest");

        int m_segmentQuantity = TOTAL_SEGMENTS;
        Constants.EDistribution m_segmentDistribution = Constants.EDistribution.NORMAL;
        double m_segmentClassBalance = 0.50;

        System.err.println("Starting first test.");
        System.err.println("Generating segments...");
        SegmentGenerator sg = new SegmentGenerator(m_segmentQuantity,
                m_segmentDistribution, m_segmentClassBalance);
        String originalSegmentsFileName = sg.generateSegments();
        File originalSegmentsFile = new File(originalSegmentsFileName);
        System.err.println("    done");

        ArrayList<Segment> originalSegments = DistributionSweep.SegmentFileToArray(originalSegmentsFileName,0,-1,0,Integer.MAX_VALUE);
        BruteforceDetection brutus = new BruteforceDetection(originalSegments, "acurracyTestOutfile");

        System.err.println("Lauching DistributionSweep...");
        DistributionSweep ds = new DistributionSweep(originalSegmentsFile, m_segmentQuantity );
        String outFileName = "t_first#" +   System.currentTimeMillis() + ".txt";
        ds.getIntersections(outFileName);
        System.err.println("    Done [DistributionSweep]");


        System.out.println("Quantity of intersections found:");
        System.out.println("    BruteForce found " + brutus.getIntersectionCounter());
        System.out.println("    DistributionSweep found " + countLinesOfFile(outFileName));






    }

    public static int countLinesOfFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }
}
