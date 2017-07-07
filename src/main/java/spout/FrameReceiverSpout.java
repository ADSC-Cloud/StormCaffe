package spout;

import config.StormConfig;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_videoio.*;
import utils.ExtractResources;
import utils.GetRunningJarPath;
import utils.SerializableMat;

import java.util.Map;
import java.util.Random;
import java.util.UUID;



/**
 * Created by john on 6/7/17.
 */
public class FrameReceiverSpout extends BaseRichSpout implements IRichSpout {

    private SpoutOutputCollector spoutOutputCollector;
    private String[] words = {"Hortonworks", "MapR", "Cloudera", "Hadoop", "Kafka", "Spark"};
    private VideoCapture videoCapture;
    private Random rand = null;
    private boolean err = false;
    private static long frameID = 0;

    private String mode;
    private String model;
    private String weights;
    private String meanValue;
    private String labels;
    private String labelledFramesPath;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        mode = map.get("mode").toString();
        model = map.get("model").toString();
        weights = map.get("weights").toString();
        meanValue = map.get("meanValue").toString();
        labels = map.get("labels").toString();
        labelledFramesPath = map.get("labelledFrames").toString();

        // extract resources in cluster mode
        if (mode.equals("cluster")) {
            ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "example");
            ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "opticalflow");
            ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "VGG");
        }

        this.spoutOutputCollector = spoutOutputCollector;
        this.rand = new Random();

        // choose computer's default camera for video capturing
        this.videoCapture = new VideoCapture(0);
        if (!videoCapture.isOpened()) {
            System.out.println("Failed to open camera!");
            System.exit(-1);
        }
        else {
            System.out.println("Succeeded to open camera!");
        }
    }

    @Override
    public void nextTuple() {
        String word = words[rand.nextInt(words.length)];

        // capture one frame from camera each time
        Mat frame = new Mat();
        err = videoCapture.read(frame);
        if (!err) {
            System.out.println("Failed to capture a frame from camera, exiting!");
            System.exit(-1);
        }
        else {
            frameID++;
        }

        SerializableMat sMat = new SerializableMat(frame);

        String msgId = UUID.randomUUID().toString();
        spoutOutputCollector.emit(new Values(word, frameID, sMat), msgId);
    }

    @Override
    public void ack(Object msgId) {

    }

    @Override
    public void fail(Object msgId) {
        System.out.println("Failed to ack message tuple: " + msgId);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("OriginalWord", "FrameID", "Frame"));
    }

    @Override
    public void close() {
        videoCapture.close();
    }
}
