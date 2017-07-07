/**
 * Created by john on 6/7/17.
 */
import bolt.caffe.InferenceBolt;
import bolt.cv.FrameDisplayBolt;
import bolt.dispatcher.DispatcherBolt;
import config.StormConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import spout.FrameReceiverSpout;
import utils.SerializableMat;

public class ObjectDetectionTest {
    public static void main(String[] args) throws Exception {

        final String TOPOLOGY_NAME = "ObjectDetectionTest";
        String model = null;
        String weights = null;
        String meanValue = null;
        String labels = null;
        String labelledFramesPath = null;

        if (args.length == 1) {
            if (args[0].equals("local")) {
                model = "/home/john/idea/stormCaffe/src/main/resources/VGG/VGG_ILSVRC_19_layers_deploy.prototxt";
                weights = "/home/john/idea/stormCaffe/src/main/resources/VGG/VGG_ILSVRC_19_layers.caffemodel";
                meanValue = "/home/john/idea/stormCaffe/src/main/resources/VGG/mean.binaryproto";
                labels   = "/home/john/idea/stormCaffe/src/main/resources/VGG/labels.txt";
                labelledFramesPath = "/home/john/idea/stormCaffe/src/main/resources/VGG/frames/";
            }
            else if (args[0].equals("cluster")) {
                model = StormConfig.LOCAL_DATA_DIR + "/VGG/VGG_ILSVRC_19_layers_deploy.prototxt";
                weights = StormConfig.LOCAL_DATA_DIR + "/VGG/VGG_ILSVRC_19_layers.caffemodel";
                meanValue = StormConfig.LOCAL_DATA_DIR + "/VGG/mean.binaryproto";
                labels   = StormConfig.LOCAL_DATA_DIR + "/VGG/labels.txt";
                labelledFramesPath = StormConfig.LOCAL_DATA_DIR + "/VGG/frames/";
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
        config.put("model", model);
        config.put("weights", weights);
        config.put("meanValue", meanValue);
        config.put("labels", labels);
        config.put("labelledFrames", labelledFramesPath);

        // metrics registration
        config.registerMetricsConsumer(org.apache.storm.metric.LoggingMetricsConsumer.class, 1);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("framereceiver", new FrameReceiverSpout(), 1);
//        builder.setBolt("dispatcher", new DispatcherBolt(), 1).shuffleGrouping("framereceiver");
        builder.setBolt("inference", new InferenceBolt(), 1).shuffleGrouping("framereceiver");
        builder.setBolt("framedisplay", new FrameDisplayBolt(), 1).shuffleGrouping("inference");

        if (args[0].equals("local")) { // Run in local model
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