package segment.dispatcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SegmentDispatcherPermanent extends SegmentDispatcher {

    public SegmentDispatcherPermanent(String nameFile){
        super(nameFile);
        try {
            File file = new File(nameFile);
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
