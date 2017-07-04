package bolt;

import config.StormConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.caffe;
import static org.bytedeco.javacpp.caffe.TEST;
import utils.ExtractResources;
import utils.GetRunningJarPath;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class SparseBolt1 extends BaseRichBolt {

    private OutputCollector outputCollector;

    private Integer gpu;
    private String modelPath;
    private String weightsPath;
    private Integer iterations;
    private caffe.FloatNet caffe_net;
    private caffe.FloatBlobVector bottom_vec;
    private ArrayList<Integer> test_score_output_id;
    private ArrayList<Float> test_score;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;

        System.out.println("Start preparing bolt!");

        // extract resources in local machines
        ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "example");

        // initialize Caffe configurations
        gpu = 0;
        modelPath = StormConfig.LOCAL_DATA_DIR + File.separator + "examples/mnist/lenet_train_test.prototxt";
        weightsPath = StormConfig.LOCAL_DATA_DIR + File.separator + "examples/mnist/lenet_iter_10000.caffemodel";
        iterations = 1;

        // Set device id and mode
        if (gpu >= 0) {
            System.out.println("Use GPU with device ID " + gpu);
            caffe.Caffe.SetDevice(gpu);
            caffe.Caffe.set_mode(caffe.Caffe.GPU);
        } else {
            System.out.println("Use CPU.");
            caffe.Caffe.set_mode(caffe.Caffe.CPU);
        }

        // Instantiate the test.caffe net
        caffe_net = new caffe.FloatNet(modelPath, TEST);
        caffe_net.CopyTrainedLayersFrom(weightsPath);
        System.out.println("Running for " + iterations + " iterations.");

        System.out.println("Finish preparing bolt!");
    }

    @Override
    public void execute(Tuple tuple) {

        // prepare and emit other information to default stream
        String appendedWord = tuple.getString(0) + "&&&";

//        bottom_vec = new caffe.FloatBlobVector();
//        test_score_output_id = new ArrayList<>();
//        test_score = new ArrayList<>();
//        float loss = 0;
//        for (int i = 0; i < iterations; i++) {
//            float[] iter_loss = new float[1];
//            caffe.FloatBlobVector result = caffe_net.Forward(bottom_vec, iter_loss);
//            loss += iter_loss[0];
//            int idx = 0;
//            for (int j = 0; j < result.size(); j++) {
//                FloatPointer result_vec = result.get(j).cpu_data();
//                for (int k = 0; k < result.get(j).count(); k++, idx++) {
//                    float score = result_vec.get(k);
//                    if (i == 0) {
//                        test_score.add(score);
//                        test_score_output_id.add(j);
//                    } else {
//                        test_score.set(idx, test_score.get(idx) + score);
//                    }
//                    String output_name = caffe_net.blob_names().get(
//                            caffe_net.output_blob_indices().get(j)).getString();
//                    System.out.println("Batch " + i + ", " + output_name + " = " + score);
//                }
//            }
//        }
//        loss /= iterations;
//        System.out.println("Loss: " + loss);
//        for (int i = 0; i < test_score.size(); i++) {
//            String output_name = caffe_net.blob_names().get(
//                    caffe_net.output_blob_indices().get(test_score_output_id.get(i))).getString();
//            float loss_weight =
//                    caffe_net.blob_loss_weights().get(caffe_net.output_blob_indices().get(i));
//            String loss_msg_stream = "";
//            float mean_score = test_score.get(i) / iterations;
//            if (loss_weight != 0) {
//                loss_msg_stream = " (* " + loss_weight
//                        + " = " + (loss_weight * mean_score) + " loss)";
//            }
//            System.out.println(output_name + " = " + mean_score + loss_msg_stream);
//        }

        outputCollector.emit(tuple, new Values(appendedWord));
        outputCollector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("AppendedWord"));
    }
}