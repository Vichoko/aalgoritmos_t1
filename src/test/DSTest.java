package test;

import algorithm.DistributionSweep;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.File;
import java.io.IOException;

import static utils.Constants.TOTAL_SEGMENTS;

/**
 * primera prueba
 */
public class DSTest {
    public static int testno = 1;
    public static void main(String[] args) throws IOException {
        // Variables
        // TODO: Parsing?

    }


    public static void tester(int inputSize, Constants.EDistribution distr, double classBalance){
        int m_segmentQuantity = inputSize;
        Constants.EDistribution m_segmentDistribution = distr;
        double m_segmentClassBalance = classBalance;

        System.err.println("Starting test " + ++testno);
        System.err.println("Segment Quantity: " + m_segmentQuantity);
        System.err.println("Distr: " + m_segmentDistribution);
        System.err.println("Balance: " + m_segmentClassBalance);

        System.err.println("Generating segments...");
        SegmentGenerator sg = new SegmentGenerator(m_segmentQuantity,
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

