package bolt.cv;

import config.StormConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import utils.ExtractResources;
import utils.GetRunningJarPath;
import utils.SerializableMat;

import java.util.Map;

/**
 * Created by john on 6/7/17.
 */
public class FrameDisplayBolt extends BaseRichBolt implements IRichBolt {
    private OutputCollector outputCollector;

    private String mode;
    private String model;
    private String weights;
    private String meanValue;
    private String labels;
    private String labelledFramesPath;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
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

        this.outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String word = tuple.getStringByField("AppendedWord");
        System.out.println("Received word: " + word);

        long frameID = tuple.getLongByField("FrameID");
        SerializableMat sMat = (SerializableMat) tuple.getValueByField("LabelledFrame");
        Mat mat = sMat.toJavaCVMat();
        imshow("Object Detection", mat);
        waitKey(1);

        System.out.println("\n-----Frame #" + frameID + " is being displayed!-----\n");

        outputCollector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
