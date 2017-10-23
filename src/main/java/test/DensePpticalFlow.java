package test;

import org.bytedeco.javacpp.indexer.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_video.calcOpticalFlowFarneback;


/**
 * Dense optical flow function implemented with Gunnar Farneback’s algorithm.
 */
public class DensePpticalFlow {
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
        System.out.println("Number of frames: " + totalFrameNumber);

        Mat prevsFrame = new Mat(); //CV_8UC3
        Mat prevsGray = new Mat(); //CV_8UC1

        boolean err;
        err = videoCapture.read(prevsFrame);
        if (!err) {
            System.out.println("Failed to read the 1st frame, exiting!");
            System.exit(-1);
        }

        int rows = prevsFrame.rows();
        int cols = prevsFrame.cols();

        cvtColor(prevsFrame, prevsGray, COLOR_BGR2GRAY);

        Scalar sc = new Scalar(0, 255, 0, 0);
        Mat hsv = new Mat(rows, cols, CV_8UC3, sc);

        err = true;
        int count = 1;
        while (err) {
            Mat nextFrame = new Mat(); //CV_8UC3
            Mat nextGray = new Mat(); //CV_8UC1
            Mat flow = new Mat(); //CV_32FC2

            err = videoCapture.read(nextFrame);

            if (err) {
                cvtColor(nextFrame, nextGray, COLOR_BGR2GRAY);
                calcOpticalFlowFarneback(prevsGray,nextGray,flow,0.5, 3, 15, 3, 5, 1.2, 0);

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

                Mat rgb = new Mat();
                cvtColor(hsv, rgb, COLOR_HSV2BGR);

                imshow("frame2", rgb);
                int k = waitKey(30) & 0xff;

                if (k == 27) // is press ESC, exit
                    System.exit(-1);
                else if (k == (int)'s') { // elseif press "s", then save two images
                    imwrite("before.png", nextFrame);
                    imwrite("after.png", rgb);
                }
                nextGray.copyTo(prevsGray);
                count++;
                System.out.println("Succeeded in processing " + count + "th frame");
            }
        }

        videoCapture.release();
        destroyAllWindows();
    }
}
