package utils;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.opencv_core.*;

import java.util.Random;

import static config.OpticalFlowConfig.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_video.calcOpticalFlowFarneback;
import static org.bytedeco.javacpp.opencv_video.calcOpticalFlowPyrLK;

/**
 * Optical flow extraction algorithms, including sparse and dense optical flow extractions.
 * Created by john on 3/7/17.
 */
public class OpticalFlow {
    private static boolean firstEntry = true;
    private static Scalar maskZero = new Scalar(0, 0, 0, 0);
    private static Scalar mask255 = new Scalar(0, 255, 0, 0);

    private static Mat p0 = new Mat();
    private static Mat mask = null;
    private static Mat hsv = null;
    private static Mat prevsGray = new Mat();

    private static long processedFrames = 0;
    private static Random rand = new Random();
    private static int color[][] = new int[100][3];

    // create some random colors
    static {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 3; j++) {
                color[i][j] = rand.nextInt(255);
            }
        }
    }

    public static void calcSparseOpticalFlow(Mat prevsFrame, Mat nextFrame, Mat outputFrame, boolean change) {

        processedFrames++;

        Mat nextGray = new Mat();
        Mat p1 = new Mat();
        Mat matStatus = new Mat();
        Mat matErr = new Mat();

        int rows = prevsFrame.rows();
        int cols = prevsFrame.cols();

        cvtColor(prevsFrame, prevsGray, COLOR_BGR2GRAY);
        cvtColor(nextFrame, nextGray, COLOR_BGR2GRAY);

        // select certain features instead of all to track
        if (firstEntry) {
            goodFeaturesToTrack(prevsGray, p0, maxCorners, qualityLevel, minDistance, null, blockSize, false, 0.04);

            // create a mask image for drawing purpose, all pixels are initialized as "0"
            mask = new Mat(rows, cols, CV_8UC3, maskZero);

            firstEntry = false;
        }

        calcOpticalFlowPyrLK(prevsGray, nextGray, p0, p1, matStatus, matErr, winSize, maxLevel, termCriteria, 0, 1e-4);

        // select good points
        int p_rows = p0.rows();
        int p_cols = p0.cols();

        FloatIndexer fip0 = p0.createIndexer();
        FloatIndexer fip1 = p1.createIndexer();
        UByteIndexer ubimatstatus = matStatus.createIndexer();

        // count the number of "1"s in mat Status
        int count = 0;
        for (int i = 0; i < p_rows; i++) {
            byte flag = (byte)(ubimatstatus.get(i));
            if (flag == (byte) 1) {
                count++;
            }
        }

        Mat good_old = new Mat(count, 2, CV_32FC1);
        Mat good_new = new Mat(count, 2, CV_32FC1);
        FloatIndexer figo = good_old.createIndexer();
        FloatIndexer fign = good_new.createIndexer();

        int pos = 0;
        for (int i = 0; i < p_rows; i++) {
            for (int j = 0; j < p_cols; j++) {
                byte flag = (byte)(ubimatstatus.get(i));
                if (flag == (byte) 1) {
                    float p0_d0 = fip0.get(i, j, 0);
                    float p0_d1 = fip0.get(i, j, 1);
                    float p1_d0 = fip1.get(i, j, 0);
                    float p1_d1 = fip1.get(i, j, 1);

                    figo.put(pos, 0, p0_d0);
                    figo.put(pos, 1, p0_d1);
                    fign.put(pos, 0, p1_d0);
                    fign.put(pos, 1, p1_d1);
                    pos++;
                }
            }
        }

        // draw the tracks
        int length = good_new.rows();
        for (int i = 0; i < length; i++) {
            int a = (int)(fign.get(i, 0));
            int b = (int)(fign.get(i, 1));
            int c = (int)(figo.get(i, 0));
            int d = (int)(figo.get(i, 1));


            Point point1 = new Point(a, b);
            Point point2 = new Point(c, d);
            Scalar colorPoint = new Scalar(color[i][0], color[i][1], color[i][2], 0);

            line(mask, point1, point2, colorPoint, 2, 8, 0);
            circle(nextFrame, point1, 5, colorPoint, -1, 8, 0);
        }

        add(nextFrame, mask, outputFrame);

        // reshape from 2D to 3D
        Mat p0Temp = new Mat(good_new.rows(), 1, CV_32FC2);
        FloatIndexer fip0t = p0Temp.createIndexer();

        for (int i = 0; i < good_new.rows(); i++) {
            float d0 = fign.get(i, 0);
            float d1 = fign.get(i, 1);

            fip0t.put(i, 0, 0, d0);
            fip0t.put(i, 0, 1, d1);
        }

        if (change && (processedFrames % INTERVAL == 0)) {
            // select certain features instead of all to track
            goodFeaturesToTrack(prevsGray, p0, maxCorners, qualityLevel, minDistance, null, blockSize, false, 0.04);

            // create a mask image for drawing purpose, all pixels are initialized as "0"
            mask = new Mat(rows, cols, CV_8UC3, maskZero);
        }
        else {
            p0 = p0Temp;
        }
    }

    public static void calcDenseOpticalFlow(Mat prevsFrame, Mat nextFrame, Mat outputFrame) {
        Mat nextGray = new Mat();

        int rows = prevsFrame.rows();
        int cols = prevsFrame.cols();

        if (firstEntry) {
            cvtColor(prevsFrame, prevsGray, COLOR_BGR2GRAY);
            hsv = new Mat(rows, cols, CV_8UC3, mask255);
            firstEntry = false;
        }
        cvtColor(nextFrame, nextGray, COLOR_BGR2GRAY);

        Mat flow = new Mat();
        calcOpticalFlowFarneback(prevsGray, nextGray, flow, 0.5, 3, 15, 3, 5, 1.2, 0);

        Mat flow0 = new Mat(rows, cols, CV_32FC1);
        Mat flow1 = new Mat(rows, cols, CV_32FC1);
        Mat mag = new Mat(rows, cols);
        Mat ang = new Mat(rows, cols);

        FloatIndexer fiflow = flow.createIndexer();
        FloatIndexer fiflow0 = flow0.createIndexer();
        FloatIndexer fiflow1 = flow1.createIndexer();
        UByteIndexer bihsv = hsv.createIndexer();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float d0 = fiflow.get(i, j, 0);
                float d1 = fiflow.get(i, j, 1);
                fiflow0.put(i, j, d0);
                fiflow1.put(i, j, d1);
            }
        }

        cartToPolar(flow0, flow1, mag, ang, false);

        Mat hsv2 = new Mat();
        normalize(mag, hsv2, 0, 255, NORM_MINMAX, -1, null);

        FloatIndexer fihsv2 = hsv2.createIndexer();
        FloatIndexer fiang = ang.createIndexer();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                byte d0 = (byte)(fiang.get(i, j) * 180 / Math.PI / 2);
                byte d2 = (byte)(fihsv2.get(i, j));

                bihsv.put(i, j, 0, d0);
                bihsv.put(i, j, 2, d2);
            }
        }

        cvtColor(hsv, outputFrame, COLOR_HSV2BGR);
        nextGray.copyTo(prevsGray);
    }
}
