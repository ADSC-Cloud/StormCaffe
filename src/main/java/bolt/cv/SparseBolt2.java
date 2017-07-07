package bolt.cv;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import config.StormConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.opencv_core.*;
import utils.ExtractResources;
import utils.GetRunningJarPath;

import caffe.CaffeClass;
import utils.OpticalFlow;
import utils.SerializableMat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;

public class SparseBolt2 extends BaseRichBolt implements IRichBolt {

    private OutputCollector outputCollector;
    private CaffeClass.FloatNet floatNet;
    private String fileName;
    private Kryo kryo;

    private String mode;
    private String frameFilePath;
    private String videoFilePath;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        mode = map.get("mode").toString();
        frameFilePath = map.get("frameFilePath").toString();
        videoFilePath = map.get("videoFilePath").toString();

        // extract resources in cluster mode
        if (mode.equals("cluster")) {
            ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "example");
            ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "opticalflow");
        }

        this.outputCollector = outputCollector;
        kryo = new Kryo();
    }

    @Override
    public void execute(Tuple tuple) {

        // prepare and emit other information to default stream
        String appendedWord = tuple.getStringByField("OriginalWord") + "!!!";

        long prevsFrameID = (long) tuple.getValueByField("PrevsFrameID");
        SerializableMat prevs_sMat = (SerializableMat) tuple.getValueByField("PrevsFrame");
        long nextFrameID = (long) tuple.getValueByField("NextFrameID");
        SerializableMat next_sMat = (SerializableMat) tuple.getValueByField("NextFrame");
        long actualFramePos = (long) tuple.getValueByField("ActualFramePos");

//        System.out.println("Frame #" + prevsFrameID + " and #" + nextFrameID + " received, size (rows, cols): (" + prevs_sMat.getRows() + ", " + prevs_sMat.getCols() + "), current frame pos: " + actualFramePos);

        fileName = String.valueOf(prevsFrameID) + "--" + String.valueOf(nextFrameID);

        // calculate the optical flow between two consecutive frames
        Mat prevs_mat = prevs_sMat.toJavaCVMat();
        Mat next_mat = next_sMat.toJavaCVMat();
        Mat output_Mat = new Mat();
        OpticalFlow.calcSparseOpticalFlow(prevs_mat, next_mat, output_Mat, true);
        SerializableMat output_sMat = new SerializableMat(output_Mat);

        // output the optical-flow-added Mat
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(frameFilePath + fileName + ".bin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Output output = new Output(fos);
        kryo.writeClassAndObject(output, output_sMat);
        output.close();
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        imshow("frame", output_Mat);
//        int k = waitKey(1) & 0xff;
//        if (k==27) {
//            System.exit(-1);
//        }

        outputCollector.emit(tuple, new Values(appendedWord));
        outputCollector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("AppendedWord"));
    }
}