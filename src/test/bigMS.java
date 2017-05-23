package test;

import algorithm.DistributionSweep;
import algorithm.sort.MergeSort;
import segment.SegmentGenerator;
import utils.Constants;

import java.io.File;
import java.io.IOException;

import static utils.Constants.TOTAL_SEGMENTS;

/**
 * primera prueba
 */
public class bigMS {
    public static void main(String[] args) throws IOException {
        // Variables
        // TODO: Parsing?
        Constants.EDistribution m_segmentDistribution = Constants.EDistribution.NORMAL;
        double m_segmentClassBalance = 0.50;

        SegmentGenerator sg = new SegmentGenerator(TOTAL_SEGMENTS,
                m_segmentDistribution, m_segmentClassBalance);
        String originalSegmentsFileName = sg.generateSegments();
        File originalSegmentsFile = new File(originalSegmentsFileName);

        MergeSort xSort = new MergeSort(Constants.EAxis.X, originalSegmentsFile, "_x_");

        System.out.print(xSort.sort());
        // check outFileName for intersections
    }

}
