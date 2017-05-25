package test;

import algorithm.DistributionSweep;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.*;

import static utils.Constants.TOTAL_SEGMENTS;

/**
 * primera prueba
 */
public class first {
    public static int testno = 0;

    public static void main(String[] args) throws IOException {
        // Variables
        // TODO: Parsing?
        TOTAL_SEGMENTS = (int) Math.pow(2, 21);
        Constants.EDistribution m_segmentDistribution = Constants.EDistribution.NORMAL;
        double m_segmentClassBalance = 0.50;


        System.err.println("Generating segments...");
        SegmentGenerator sg = new SegmentGenerator(TOTAL_SEGMENTS,
                m_segmentDistribution, m_segmentClassBalance);
        String originalSegmentsFileName = sg.generateSegments();
        File originalSegmentsFile = new File(originalSegmentsFileName);
        System.err.println("    done");
        System.err.println("Lauching DistributionSweep...");
        DistributionSweep ds = new DistributionSweep(originalSegmentsFile);
        String outFileName = "t_first#" +   System.currentTimeMillis() + ".txt";
        ds.getIntersections(outFileName);
        System.err.println("    Done [DistributionSweep]");
        // check outFileName for intersections
    }

}
