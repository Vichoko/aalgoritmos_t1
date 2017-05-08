package segment.dispatcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.System.exit;

/**
 * Writes in a temporary file (it's deleted when program ends)
 */
public class SegmentDispatcherTemporary extends SegmentDispatcher {

    public SegmentDispatcherTemporary(String nameFile) {
        super(nameFile);
        File tempFile = null;
        try {
            tempFile = new File(nameFile+".tmp");
            pw = new PrintWriter(tempFile);
        } catch (IOException e) {
            System.err.println("Mergesort:: error al crear archivo temporal");
            System.err.println(e.toString());
            exit(-1);
        }
        //tempFile.deleteOnExit();
    }
}
