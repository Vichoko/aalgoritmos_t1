package segment.writer;

import utils.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static utils.Constants.IO_COUNTER;

public abstract class FileWriter{
    PrintWriter pw;
    File file;
    StringBuilder sb;
    private String pathname;
    private int maxBytes;
    int bytesCount;

    FileWriter(String pathname) throws IOException{
        this.pathname = pathname;
        pw = new PrintWriter(new File(pathname));
        sb = new StringBuilder();
        bytesCount = 0;
        this.maxBytes = Constants.B;
    }

    public void setMaxBytes(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    public String getPathname() {
        return pathname;
    }

    public void setDeleteOnExit(){
        file.deleteOnExit();
    }

    public boolean close(){
        if (sb.length()!=0) {
            writePage();
            pw.close();
            return true;
        }
        pw.close();
        return false;
    }

    boolean checkCapacity(int bytesToAdd){
        if (bytesCount+bytesToAdd > maxBytes){
            writePage();
            bytesCount = 0;
            sb.setLength(0);
            return true;
        }
        return false;
    }

    private void writePage(){
        pw.write(sb.toString());
        IO_COUNTER++;
    }
    double truncateTo3dec(double src){
        return Math.floor(src*1000)/1000;
    }
}
