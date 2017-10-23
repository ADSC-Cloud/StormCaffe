package test;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.opencv_core.*;

import java.util.Random;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.TermCriteria.COUNT;
import static org.bytedeco.javacpp.opencv_core.TermCriteria.EPS;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_video.calcOpticalFlowPyrLK;

/**
 * Optical flow function implemented in Lucas-Kanade method with pyramids.
 */
public class SparseOpticalFlow {
    public static void main(String[] args) {

        final VideoCapture videoCapture = new VideoCapture("/home/john/idea/stormCaffe/src/main/resources/opticalflow/dense.avi");

        if (!videoCapture.isOpened()) {
            System.out.println("Failed to open video!");
            System.exit(-1);
        }
        else {
            System.out.println("Succeeded to open video!");
        }
        int totalFrameNumber = (int)(videoCapture.get(CV_CAP_PROP_FRAME_COUNT));
        int currentFramePos = 0;
        System.out.println("Total number of frames: " + totalFrameNumber);

        // params for ShiTomasi corner detection
        int maxCorners = 100;
        float qualityLevel = 0.3f;
        int minDistance = 7;
        int blockSize = 7;

        // params for lucas kanade optical flow
        Size winSize = new Size(15, 15);
        int maxLevel = 2;
        TermCriteria termCriteria = new TermCriteria(EPS|COUNT, 10, 0.03);

        // create some random colors
        Random rand = new Random();
        int color[][] = new int[100][3];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 3; j++) {
                color[i][j] = rand.nextInt(255);
            }
        }

        // take first frame and find corners in it
        Mat prevsFrame = new Mat();
        Mat prevsGray = new Mat();
        Mat p0 = new Mat();
        boolean err;

        err = videoCapture.read(prevsFrame);
        if (!err) {
            System.out.println("Failed to read the 1st frame, exiting!");
            System.exit(-1);
        }
        currentFramePos++;

        int rows = prevsFrame.rows();
        int cols = prevsFrame.cols();
        System.out.println("Frame size: (rows, cols): (" + rows + ", " + cols + ")");
        cvtColor(prevsFrame, prevsGray, COLOR_BGR2GRAY);

        // select certain features instead of all to track
        goodFeaturesToTrack(prevsGray, p0, maxCorners, qualityLevel, minDistance, null, blockSize, false, 0.04);

        // create a mask image for drawing purpose, all pixels are initialized as "0"
        Scalar maskZero = new Scalar(0, 0, 0, 0);
        Mat mask = new Mat(rows, cols, CV_8UC3, maskZero);

        err = true;
        int countOfProcessedFrames = 1;
        while (err)
        {
            Mat nextFrame = new Mat();
            Mat nextGray = new Mat();

            err = videoCapture.read(nextFrame);
            if (err) {
                currentFramePos++;
                if (currentFramePos == totalFrameNumber) {
                    videoCapture.set(CAP_PROP_POS_FRAMES, 1);
                    currentFramePos = 1;
                }
                cvtColor(nextFrame, nextGray, COLOR_BGR2GRAY);

                // calculate optical flow
                Mat p1 = new Mat();
                Mat matStatus = new Mat();
                Mat matErr = new Mat();

                calcOpticalFlowPyrLK(prevsGray, nextGray, p0, p1, matStatus, matErr, winSize, maxLevel, termCriteria, 0, 1e-4);
                System.out.println("Processing frame #" + countOfProcessedFrames +  ": (" + p1.rows() + ", " + p1.cols() + ", " + p1.channels() + ")");
                countOfProcessedFrames++;

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

                Mat img = new Mat();
                add(nextFrame, mask, img);

                imshow("frame", img);
                int k = waitKey(30) & 0xff;
                if (k==27) {
                    System.exit(-1);
                }

                // now update the previous frame and previous points
                nextGray.copyTo(prevsGray);

                // reshape from 2D to 3D
                Mat p0Temp = new Mat(good_new.rows(), 1, CV_32FC2);
                FloatIndexer fip0t = p0Temp.createIndexer();

                for (int i = 0; i < good_new.rows(); i++) {
                    float d0 = fign.get(i, 0);
                    float d1 = fign.get(i, 1);

                    fip0t.put(i, 0, 0, d0);
                    fip0t.put(i, 0, 1, d1);
                }

                if (countOfProcessedFrames%100 == 0) {
                    // select certain features instead of all to track
                    goodFeaturesToTrack(prevsGray, p0, maxCorners, qualityLevel, minDistance, null, blockSize, false, 0.04);

                    // create a mask image for drawing purpose, all pixels are initialized as "0"
                    mask = new Mat(rows, cols, CV_8UC3, maskZero);
                }
                else {
                    p0 = p0Temp;
                }
            }
        }

        videoCapture.release();
        destroyAllWindows();
    }
}