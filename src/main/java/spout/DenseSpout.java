package spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_videoio.*;
import utils.SerializableMat;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.bytedeco.javacpp.opencv_videoio.CAP_PROP_POS_FRAMES;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_COUNT;

public class DenseSpout extends BaseRichSpout implements IRichSpout {

    private SpoutOutputCollector spoutOutputCollector;
    private String[] words = {"Hortonworks", "MapR", "Cloudera", "Hadoop", "Kafka", "Spark"};
    private VideoCapture videoCapture;
    private Random rand = null;
    private boolean err = false;
    private long totalFrameNumber = 0;
    private static long currentFramePos = 1;
    private static long prevsMatID = 0;
    private static long nextMatID = 0;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        this.rand = new Random();
        this.videoCapture = new VideoCapture("/home/john/idea/stormCaffe/dense.avi");
        if (!videoCapture.isOpened()) {
            System.out.println("Failed to open video!");
            System.exit(-1);
        }
        else {
            System.out.println("Succeeded to open video!");
        }
        totalFrameNumber = (long)(videoCapture.get(CV_CAP_PROP_FRAME_COUNT));
        System.out.println("Total number of frames: " + totalFrameNumber);
    }

    @Override
    public void nextTuple() {
        String word = words[rand.nextInt(words.length)];

        // take two consecutive frames from the video
        Mat prevs_Mat = new Mat();
        Mat next_Mat = new Mat();

        // read two consecutive frames from the video
        err = videoCapture.read(prevs_Mat);
        if (!err) {
            System.out.println("Failed to read the prevs_frame, exiting!");
            System.exit(-1);
        }
        else {
            prevsMatID = currentFramePos - 1;

            err = videoCapture.read(next_Mat);
            if (!err) {
                System.out.println("Failed to read the next_frame, exiting!");
                System.exit(-1);
            }
            else {
                nextMatID = currentFramePos;
//                currentFramePos++;

                if ((currentFramePos + 1) == totalFrameNumber) {
                    videoCapture.set(CAP_PROP_POS_FRAMES, 0);
                    currentFramePos = 1;
                }
                else {
                    videoCapture.set(CAP_PROP_POS_FRAMES, currentFramePos);
                    currentFramePos++;
                }
            }
        }
        SerializableMat prevs_sMat = new SerializableMat(prevs_Mat);
        SerializableMat next_sMat = new SerializableMat(next_Mat);

        String msgId = UUID.randomUUID().toString();
        spoutOutputCollector.emit(new Values(word, prevsMatID, prevs_sMat, nextMatID, next_sMat, currentFramePos), msgId);
    }

    @Override
    public void ack(Object msgId) {
//        System.out.println("Message #" + msgId + ": acked!");
    }

    @Override
    public void fail(Object msgId) {
        System.out.println("Failed to ack message tuple: " + msgId);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("OriginalWord", "PrevsFrameID", "PrevsFrame", "NextFrameID", "NextFrame", "ActualFramePos"));
    }

    @Override
    public void close() {
        videoCapture.close();
    }
}