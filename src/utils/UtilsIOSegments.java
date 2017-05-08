package utils;

import segment.Segment;
import segment.dispatcher.SegmentDispatcher;
import segment.dispatcher.SegmentDispatcherTemporary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static utils.Constants.B;

/**
 * Created by constanzafierro on 07-05-17.
 */
public class UtilsIOSegments {
    /**
     * Transforms the bytes read to list of double
     * Creates segment objects each 4 numbers
     *
     * @param buffer   Bytes from where to get the coordinates
     * @return      An ArrayBytesRead object, with the segments in the run and the number of bytes read
     */
    public static ArrayBytesRead getSegments(byte[] buffer){
        // cache for reading
        int points = 0;
        double[] coordinates = new double[4];
        StringBuilder stringNextNumber = new StringBuilder();
        ArrayList<Segment> segments = new ArrayList<>();
        int bytesRead = 0;
        for (int i = 0; i < buffer.length; i++) {
            byte b = buffer[i];
            // end of bytes read
            if (b==0) break;
            char c = (char) b;
            if(c ==',') {
                coordinates[points] = Double.parseDouble(stringNextNumber.toString());
                stringNextNumber.setLength(0);
                points++;
            }
            else if (c != '\n')
                stringNextNumber.append(c);
            if (points==4){
                Segment s = new Segment(coordinates[0], coordinates[1],
                        coordinates[2], coordinates[3]);
                segments.add(s);
                points = 0;
                bytesRead = i+1;
            }
        }
        return new ArrayBytesRead(segments, bytesRead);
    }

    /***
     * Saves array of segments in a temporary file
     * @param segments  segments to be save
     * @param nameFile
     */
    public static void saveSegmentsTempFile(ArrayList<Segment> segments, String nameFile) {
        SegmentDispatcher dispatcher = new SegmentDispatcherTemporary(nameFile);
        for (Segment segment: segments){
            dispatcher.saveSegment(segment.x1, segment.y1, segment.x2, segment.y2);
        }
        dispatcher.close();
    }

    /***
     * Reads segments from an input file
     * @param input File from where to read
     * @return      An ArrayBytesRead object, with the segments list and the number of bytes read
     */
    public static ArrayBytesRead readPage(RandomAccessFile input, int offset) {
        // B page size
        byte[] buffer = new byte[B];
        try {
            input.seek(offset);
            input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getSegments(buffer);
    }

    /**
     * Represents segments read and bytes count of those segments.
     */
    public static class ArrayBytesRead {
        public final ArrayList<Segment> segments;
        public final int bytesRead;

        ArrayBytesRead(ArrayList<Segment> segments, int bytesRead) {
            this.segments = segments;
            this.bytesRead = bytesRead;
        }
    }
}

