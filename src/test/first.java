package test;

import algorithm.DistributionSweep;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.*;

/**
 * primera prueba
 */
public class first {
    public static void main(String[] args) throws IOException {
        // Variables
        // TODO: Parsing?
        int m_segmentQuantity = (int) Math.pow(2, 9);
        Constants.EDistribution m_segmentDistribution = Constants.EDistribution.NORMAL;
        double m_segmentClassBalance = 0.50;

        SegmentGenerator sg = new SegmentGenerator(m_segmentQuantity,
                m_segmentDistribution, m_segmentClassBalance);
        String originalSegmentsFileName = sg.generateSegments();
        File originalSegmentsFile = new File(originalSegmentsFileName);

        DistributionSweep ds = new DistributionSweep(originalSegmentsFile);
        String outFileName = "t_first#" +   System.currentTimeMillis();
        ds.getIntersections(outFileName);

        // check outFileName for intersections
    }

}
