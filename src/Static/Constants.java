package Static;

/**
 * Created by vicen on 01-05-2017.
 */
public final class Constants {
    public enum EDistribution{
        UNIFORM, NORMAL
    }

    // Segment Generator
    public static final int YMAX = 100;
    public static final int XMAX = 100;
    public static final double normalMean = XMAX/2;
    public static final double normalDesv = XMAX/7;

    // Algorithms
    public enum EAxis{
        X,Y}

	// en Bytes
    public static final int M = 1024*1024*1024; // Tamano de RAM: 1024 MB para probar
    public static final int B = 512; // Tamano de bloque/sector: 512 bytes en mi pc
	

}
