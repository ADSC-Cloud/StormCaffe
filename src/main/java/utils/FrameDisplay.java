package utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import config.StormConfig;
import org.bytedeco.javacpp.opencv_core.*;

import java.io.*;

import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;

/**
 * Created by john on 4/7/17.
 */
public class FrameDisplay implements Runnable {
    private static Kryo kryo;
    private String frameFilePath;
    private long totalFrameNumber;

    public FrameDisplay(String frameFilePath, long totalFrameNumber) {
        this.frameFilePath = frameFilePath;
        this.totalFrameNumber = totalFrameNumber;
        this.kryo = new Kryo();
    }

    @Override
    public void run() {

        System.out.println("Starting displayFrame thread, frameFilePath: " + frameFilePath + ", totalFrameNumber: " + totalFrameNumber);

        long prevsFrameID, nextFrameID, currentFramePos = 1;

        while(true) {
            prevsFrameID = currentFramePos - 1;
            nextFrameID = currentFramePos;

            String fileName = String.valueOf(prevsFrameID) + "--" + String.valueOf(nextFrameID) + ".bin";
            File aFile = new File(frameFilePath + fileName);

            if (aFile.exists()) {

                // wait for a while to avoid any inconsistency while Kryo Output is writing out the frame to file
                waitKey(100);

                try {
                    System.out.println(frameFilePath + fileName + " is being processed!");
                    FileInputStream fis = new FileInputStream(frameFilePath + fileName);
                    Input input = new Input(fis);
                    SerializableMat sMat = (SerializableMat) kryo.readClassAndObject(input);
                    Mat mat = sMat.toJavaCVMat();
                    imshow("frame", mat);
                    waitKey(1);

                    input.close();
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println(frameFilePath + fileName + " doesn't exist!");

                // wait the optical flow bolts generating optical flows
                waitKey(100);
                continue;
            }

            currentFramePos++;
            if (currentFramePos == totalFrameNumber) {
                currentFramePos = 1;
            }
        }
    }
}
