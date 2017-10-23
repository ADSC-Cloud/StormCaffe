/**
 * This program adopts Lucas-Kanade method with pyramids algorithm to achieve sparse optical flow tracking
 * functionality. A selection for pixels of interest will be performed before tracking so that only those
 * pixels will be tracked.
 */

import bolt.cv.SparseBolt1;
import bolt.cv.SparseBolt2;
import bolt.cv.SparseBolt3;

import config.StormConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.shade.org.apache.commons.io.FileUtils;
import org.apache.storm.topology.TopologyBuilder;
import spout.SparseSpout;
import utils.FrameDisplay;
import utils.SerializableMat;

import java.io.File;

import static org.bytedeco.javacpp.opencv_videoio.*;

public class SparseOpticalFlowTest {

    public static void main(String[] args) throws Exception {

        final String TOPOLOGY_NAME = "SparseOpticalFlowTest";
        String frameFilePath = null;
        String videoFilePath = null;
        long totalFrameNumber = 0;

        if (args.length == 1) {
            if (args[0].equals("local")) {
                frameFilePath = "/home/john/idea-data/opticalflow/SparseOpticalFlowTest/";
                videoFilePath = "/home/john/idea-data/opticalflow/traffic.avi";

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
                frameFilePath = StormConfig.LOCAL_DATA_DIR + "/opticalflow/SparseOpticalFlowTest/";
                videoFilePath = StormConfig.LOCAL_DATA_DIR + "/opticalflow/pyrlk.avi";
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
        config.setDebug(false);
        config.setMessageTimeoutSecs(100);
        config.setMaxSpoutPending(100);

        // custom arguments
        config.put("mode", args[0]);
        config.put("frameFilePath", frameFilePath);
        config.put("videoFilePath", videoFilePath);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new SparseSpout(), 1);
        builder.setBolt("bolt1", new SparseBolt1(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt2", new SparseBolt2(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt3", new SparseBolt3(), 1).shuffleGrouping("bolt1").shuffleGrouping("bolt2");

        if (args[0].equals("local")) { // Run in local model
            Thread displayFrameThread = new Thread(new FrameDisplay(frameFilePath, totalFrameNumber));
            displayFrameThread.start();

            System.out.println("Running in local mode!");
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
//            Utils.sleep(10000);
//            localCluster.killTopology(TOPOLOGY_NAME);
//            localCluster.shutdown();
        }
        else {
            System.out.println("Running in cluster mode!");
            StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
        }
    }
}