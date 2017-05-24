package algorithm;

import segment.Segment;
import segment.dispatcher.PointFileWriter;

import java.util.ArrayList;

import static utils.Constants.DEBUG;

/**
 * Created by vicen on 22-05-2017.
 */
public class BruteforceDetection {
    ArrayList<Segment> m_array;
    PointFileWriter answerFile;

    private int interCounter;



    public BruteforceDetection(ArrayList<Segment> array,
                               String outfilename)
    {
        if (DEBUG) {System.err.println("Starting Bruteforce Detection");}
        interCounter = 0;
        m_array = array;
        answerFile = new PointFileWriter(outfilename);
        ArrayList<Segment> verticals = new ArrayList<Segment>();
        ArrayList<Segment> horizotals = new ArrayList<Segment>();
        if (DEBUG) {System.err.println("Separating segments...");}
        for (Segment s : array){
            if (s.isVertical()){
                verticals.add(s);
            } else {
                horizotals.add(s);
            }
        }
        if (DEBUG) {System.err.println("    Done.");}

        if (DEBUG) {System.err.println("Counting intersections...");}

        for (Segment v : verticals){
            assert (v.x1 == v.x2);
            double yi = Math.min(v.y1, v.y2);
            double yj = Math.max(v.y1, v.y2);
            double x = v.x1;
            for (Segment h : horizotals){
                assert (h.y1 == h.y2);
                double y = h.y1;
                double xi = Math.min(h.x1, h.x2);
                double xj = Math.max(h.x1, h.x2);
                if (xi <= x && x <= xj && yi <= y && y <= yj){
                    answerFile.savePoint(v.x1, h.y1);
                    interCounter++;
                }
            }
        }
        if (DEBUG) {System.err.println("    Done BruteForce.");}
    }

    public void getInsight(){
        System.out.print("Number of intersections: "+interCounter);
    }

    public int getIntersectionCounter(){
        return interCounter;
    }
}
