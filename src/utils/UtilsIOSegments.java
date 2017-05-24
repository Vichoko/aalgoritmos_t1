package utils;

import segment.Segment;
import segment.writer.SegmentWriter;
import segment.writer.SegmentWriterTemporary;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import static utils.Constants.B;

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
        ArrayList<Segment> segments = new ArrayList<Segment>();
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
     * @param nameFile file name
     */
    public static void saveSegmentsTempFile(ArrayList<Segment> segments, String nameFile) {
        SegmentWriter dispatcher = new SegmentWriterTemporary(nameFile);
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
        return readPage(input, offset, Integer.MAX_VALUE);
    }
    /***
     * Reads segments from an input file
     * @param input File from where to read
     * @param startOffset Start mark where to start reading in bytes.
     * @param endOffset Start mark where to end reading in bytes.
     *
     * @return      An ArrayBytesRead object, with the segments list and the number of bytes read
     */
    public static ArrayBytesRead readPage(RandomAccessFile input, int startOffset, int endOffset) {
        byte[] buffer;
        // B page size
        if (endOffset-startOffset >= B){ //Read a page
            buffer = new byte[B];
        }
        else if(startOffset >= endOffset){ // dont read anything
            return getSegments(new byte[0]);
        }
        else { //read until less than B, until offset
            buffer = new byte[endOffset-startOffset];
        }

        try {
            input.seek(startOffset);
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

