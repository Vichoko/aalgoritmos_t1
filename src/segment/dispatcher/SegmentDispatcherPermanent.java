package segment.dispatcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SegmentDispatcherPermanent extends SegmentDispatcher {

    public SegmentDispatcherPermanent(String nameFile){
        super(nameFile);
        try {
            File file = new File(nameFile+".txt");
            pw = new PrintWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
