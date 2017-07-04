package config;

import org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_core.TermCriteria.COUNT;
import static org.bytedeco.javacpp.opencv_core.TermCriteria.EPS;

/**
 * Created by john on 3/7/17.
 */
public class OpticalFlowConfig {

    // params for ShiTomasi corner detection
    public static final int maxCorners = 100;
    public static final float qualityLevel = 0.3f;
    public static final int minDistance = 7;
    public static final int blockSize = 7;

    // params for lucas kanade optical flow
    public static final Size winSize = new Size(15, 15);
    public static final int maxLevel = 2;
    public static final TermCriteria termCriteria = new TermCriteria(EPS|COUNT, 10, 0.03);

    // params for refreshing interval
    public static final int INTERVAL = 100;
}
