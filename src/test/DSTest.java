package test;

import algorithm.DistributionSweep;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.File;
import java.io.IOException;

import static utils.Constants.INTERS_COUNTER;
import static utils.Constants.IO_COUNTER;
import static utils.Constants.TOTAL_SEGMENTS;

/**
 * primera prueba
 */
public class DSTest {
    public static int testno = 1;
    public static void main(String[] args) throws IOException {
        int[] segmentSizePowers = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
        Constants.EDistribution[] distributions = {Constants.EDistribution.UNIFORM, Constants.EDistribution.NORMAL};
        double[] alphas = {0.25, 0.5, 0.75};
        for (int power : segmentSizePowers){
            for (Constants.EDistribution distr : distributions){
                for (double a : alphas){
                    tester((int)Math.pow(2, power), distr, a);
                }
            }
        }
    }


    public static void tester(int inputSize, Constants.EDistribution distr, double classBalance) throws IOException {
        TOTAL_SEGMENTS = inputSize;
        Constants.EDistribution m_segmentDistribution = distr;
        double m_segmentClassBalance = classBalance;
        IO_COUNTER=0;
        INTERS_COUNTER = 0;

        System.err.println("Starting test " + testno++);
        System.err.println("Segment Quantity: " + TOTAL_SEGMENTS);
        System.err.println("Distr: " + m_segmentDistribution);
        System.err.println("Segments Class Balance: " + m_segmentClassBalance);

        System.err.println("Generating segments...");
        SegmentGenerator sg = new SegmentGenerator(TOTAL_SEGMENTS,
                m_segmentDistribution, m_segmentClassBalance);
        String originalSegmentsFileName = sg.generateSegments();
        File originalSegmentsFile = new File(originalSegmentsFileName);
        System.err.println("    done");
        System.err.println("Lauching DistributionSweep...");
        DistributionSweep ds = new DistributionSweep(originalSegmentsFile);
        String outFileName = "DSTEST_#" +   System.currentTimeMillis() + ".txt";
        ds.getIntersections(outFileName);
        System.err.println("    Done [DistributionSweep] test " + testno);
        System.err.println("Intersections found: " + INTERS_COUNTER);
        System.err.println("I/O Accesses: " + IO_COUNTER);
        System.err.println("");
    }
}

