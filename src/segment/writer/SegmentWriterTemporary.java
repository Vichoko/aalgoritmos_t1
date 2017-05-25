package segment.writer;

import java.io.IOException;

/**
 * Writes in a temporary file (it's deleted when program ends)
 */
public class SegmentWriterTemporary extends SegmentWriter {

    public SegmentWriterTemporary(String nameFile) throws IOException {
        super(nameFile);
        file.deleteOnExit();
    }
}
