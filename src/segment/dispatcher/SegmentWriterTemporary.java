package segment.dispatcher;

/**
 * Writes in a temporary file (it's deleted when program ends)
 */
public class SegmentWriterTemporary extends SegmentWriter {

    public SegmentWriterTemporary(String nameFile) {
        super(nameFile);
        file.deleteOnExit();
    }
}
