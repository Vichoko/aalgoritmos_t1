package segment.dispatcher;

import java.io.File;
import java.io.PrintWriter;

public abstract class FileWriter{
    PrintWriter pw;
    File file;
    StringBuilder sb;
    private String pathname;
    int maxElements;
    int elementsCount;

    FileWriter(String pathname) {
        this.pathname = pathname;
        sb = new StringBuilder();
        elementsCount = 0;
    }

    public String getPathname() {
        return pathname;
    }

    public void setDeleteOnExit(){
        file.deleteOnExit();
    }

    abstract public void setMaxBytesRAM(int maxBytesRAM);

    public boolean close(){
        if (sb.length()!=0) {
            writePage();
            pw.close();
            return true;
        }
        pw.close();
        return false;
    }

    boolean checkCapacity(){
        if (elementsCount+1 >= maxElements){
            writePage();
            elementsCount = 0;
            sb.setLength(0);
            return true;
        }
        return false;
    }

    private void writePage(){
        pw.write(sb.toString());
    }
    double truncateTo3dec(double src){
        return Math.floor(src*1000)/1000;
    }
}
