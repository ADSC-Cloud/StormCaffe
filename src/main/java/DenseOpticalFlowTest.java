import bolt.cv.DenseBolt1;
import bolt.cv.DenseBolt2;
import bolt.cv.DenseBolt3;
import config.StormConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.shade.org.apache.commons.io.FileUtils;
import org.apache.storm.topology.TopologyBuilder;
import org.bytedeco.javacpp.opencv_videoio.*;
import spout.DenseSpout;
import utils.FrameDisplay;
import utils.SerializableMat;

import java.io.File;

import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_COUNT;

/**
 * This program adopts Gunnar Farneback’s algorithm to achieve dense optical flow tracking functionality.
 * With dense optical flow tracking, all pixels in frames will be tracked and analysed.
 *
 * Created by john on 3/7/17.
 */
public class DenseOpticalFlowTest {
    public static void main(String[] args) throws Exception {

        final String TOPOLOGY_NAME = "DenseOpticalFlowTest";
        String frameFilePath = null;
        String videoFilePath = null;
        long totalFrameNumber = 0;

        if (args.length == 1) {
            if (args[0].equals("local")) {
                frameFilePath = "/home/john/idea-data/opticalflow/DenseOpticalFlowTest/";
                videoFilePath = "/home/john/idea-data/opticalflow/dense.avi";

                // delete all existing files in frameFilePath folder
                FileUtils.deleteDirectory(new File(frameFilePath));
                FileUtils.forceMkdir(new File(frameFilePath));

                VideoCapture videoCapture = new VideoCapture(videoFilePath);
                if (!videoCapture.isOpened()) {
                    System.out.println("Failed to open video!");
                    System.exit(-1);
                }
                else {
                    System.out.println("Succeeded to open video!");
                }
                totalFrameNumber = (long)(videoCapture.get(CV_CAP_PROP_FRAME_COUNT));
                System.out.println("Total number of frames: " + totalFrameNumber);
                videoCapture.close();
            }
            else if (args[0].equals("cluster")) {
                frameFilePath = StormConfig.LOCAL_DATA_DIR + "/opticalflow/DenseOpticalFlowTest/";
                videoFilePath = StormConfig.LOCAL_DATA_DIR + "/opticalflow/dense.avi";
            }
            else {
                System.out.println("Incorrect mode argument. You should declare mode of running topology (local/cluster).");
                System.exit(-1);
            }
        }
        else {
            System.out.println("Incorrect number of mode arguments.");
            System.exit(-1);
        }

        Config config = new Config();
        config.registerSerialization(SerializableMat.class);
        config.setNumWorkers(4);
        config.setDebug(true);
        config.setMessageTimeoutSecs(100);
        config.setMaxSpoutPending(100);

        // custom arguments
        config.put("mode", args[0]);
        config.put("frameFilePath", frameFilePath);
        config.put("videoFilePath", videoFilePath);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new DenseSpout(), 1);
        builder.setBolt("bolt1", new DenseBolt1(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt2", new DenseBolt2(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt3", new DenseBolt3(), 1).shuffleGrouping("bolt1").shuffleGrouping("bolt2");

        if (args[0].equals("local")) { // Run in local model
            Thread displayFrameThread = new Thread(new FrameDisplay(frameFilePath, totalFrameNumber));
            displayFrameThread.start();

            System.out.println("Running in local mode!");
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
//            Utils.sleep(10000);
//            localCluster.killTopology(TOPOLOGY_NAME);
//            localCluster.shutdown();
        } else {
            System.out.println("Running in cluster mode!");
            StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
        }
    }
}
