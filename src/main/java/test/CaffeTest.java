package test;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bytedeco.javacpp.*;

import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by john on 7/6/17.
 */
public class CaffeTest {

    private Integer gpu;
    private String modelPath;
    private String weightsPath;
    private Integer iterations;
    private org.bytedeco.javacpp.caffe.FloatNet caffe_net;
    private org.bytedeco.javacpp.caffe.FloatBlobVector bottom_vec;
    private ArrayList<Integer> test_score_output_id;
    private ArrayList<Float> test_score;

    // Test: score a model.
    private void caffeTest() {

        gpu = new Integer("0");
//        modelPath = "/home/john/idea/stormCaffe/examples/mnist/lenet_train_test.prototxt";
        modelPath = "/home/john/idea/stormCaffe/examples/mnist/lenet.prototxt";
        weightsPath = "/home/john/idea/stormCaffe/examples/mnist/lenet_iter_10000.caffemodel";
        iterations = new Integer("100");

        // Set device id and mode
        if (gpu >= 0) {
            System.out.println("Use GPU with device ID " + gpu);
            Caffe.SetDevice(gpu);
            Caffe.set_mode(Caffe.GPU);
        } else {
            System.out.println("Use CPU.");
            Caffe.set_mode(Caffe.CPU);
        }


        // Instantiate the test.caffe net.
        caffe_net = new FloatNet(modelPath, TEST);
        caffe_net.CopyTrainedLayersFrom(weightsPath);
        System.out.println("Running for " + iterations + " iterations.");

        // simulate looping of execute method in Storm
        while(true) {
            bottom_vec = new FloatBlobVector();
            test_score = new ArrayList<>();
            test_score_output_id = new ArrayList<>();

            float loss = 0;
            for (int i = 0; i < iterations; i++) {
                float[] iter_loss = new float[1];
                FloatBlobVector result = caffe_net.Forward(bottom_vec, iter_loss);
                loss += iter_loss[0];
                int idx = 0;
                for (int j = 0; j < result.size(); j++) {
                    FloatPointer result_vec = result.get(j).cpu_data();
                    for (int k = 0; k < result.get(j).count(); k++, idx++) {
                        float score = result_vec.get(k);
                        if (i == 0) {
                            test_score.add(score);
                            test_score_output_id.add(j);
                        } else {
                            test_score.set(idx, test_score.get(idx) + score);
                        }
                        String output_name = caffe_net.blob_names().get(caffe_net.output_blob_indices().get(j)).getString();
                        System.out.println("Batch " + i + ", " + output_name + " = " + score);
                    }
                }
            }
            loss /= iterations;
            System.out.println("Loss: " + loss);

            for (int i = 0; i < test_score.size(); i++) {
//                String output_name = caffe_net.blob_names().get(caffe_net.output_blob_indices().get(test_score_output_id.get(i))).getString();
//                float loss_weight = caffe_net.blob_loss_weights().get(caffe_net.output_blob_indices().get(i));
//                String loss_msg_stream = "";
//                float mean_score = test_score.get(i) / iterations;
//                if (loss_weight != 0) {
//                    loss_msg_stream = " (* " + loss_weight + " = " + (loss_weight * mean_score) + " loss)";
//                }
//                System.out.println(output_name + " = " + mean_score + loss_msg_stream);
                System.out.println("Test score: " + test_score.get(i));
            }

            bottom_vec.close();
            test_score.clear();
            test_score_output_id.clear();
        }
    }

    public static void main(String[] args) {

        CaffeTest caffeTest = new CaffeTest();
        caffeTest.caffeTest();

        System.out.println("Caffe test finished.");
    }
}