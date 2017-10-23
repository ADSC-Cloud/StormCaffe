package bolt.caffe;

import caffe.CaffeClassifier;
import config.StormConfig;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.caffe.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacpp.opencv_videoio.*;
import utils.ExtractResources;
import utils.GetRunningJarPath;
import utils.SerializableMat;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.bytedeco.javacpp.opencv_imgproc.CV_FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_imgproc.cvInitFont;
import static org.bytedeco.javacpp.opencv_imgproc.putText;

/**
 * Created by john on 20/6/17.
 */
public class InferenceBolt extends BaseRichBolt implements IRichBolt {

    private OutputCollector outputCollector;

    private String mode;
    private String model;
    private String weights;
    private String meanValue;
    private String labels;
    private String labelledFramesPath;

    private CaffeClassifier classifier = null;
    private CvFont font = null;

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

        // set Caffe configurations
        Caffe.set_mode(Caffe.CPU);
        classifier = CaffeClassifier.create(model, weights, meanValue, labels);
        font = new CvFont();
        cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 1.5, 1.5, 0, 2, 8);

        this.outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

        String appendedWord = tuple.getStringByField("OriginalWord") + "!!!";

        long frameID = tuple.getLongByField("FrameID");
        SerializableMat sMat = (SerializableMat) tuple.getValueByField("Frame");
        Mat mat = sMat.toJavaCVMat();

        String resultsStr;
        System.out.format("\n-----Frame %d is being analyzed!-----\n", frameID);

        List<CaffeClassifier.Prediction> results = classifier.classify(mat, 5);
        for (int i = 0; i < results.size(); i++) {

            resultsStr = results.get(i).label + ": " + String.format("%.02f", results.get(i).confidence);

            System.out.println(resultsStr);

            if (i == 0) {
                putText(mat, resultsStr, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0, 255, 0), 2, 2, false);
            }
            else if (i == 1) {
                putText(mat, resultsStr, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0, 255, 0), 2, 2, false);
            }
            else {
                putText(mat, resultsStr, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 0, 0), 2, 2, false);
            }
        }

        SerializableMat sLabelledMat = new SerializableMat(mat);

        outputCollector.emit(tuple, new Values(appendedWord, frameID, sLabelledMat));
        outputCollector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("AppendedWord", "FrameID", "LabelledFrame"));
    }
}
