import java.io.IOException;
import java.util.List;
import static org.bytedeco.javacpp.caffe.Caffe;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacpp.opencv_videoio.*;
import caffe.CaffeClassifier;

import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class CaffeExampleJava {

    public static void main(String[] args) throws IOException {

        String model   = "/home/john/idea/stormCaffe/src/main/resources/VGG/VGG_ILSVRC_19_layers_deploy.prototxt";
        String weights = "/home/john/idea/stormCaffe/src/main/resources/VGG/VGG_ILSVRC_19_layers.caffemodel";
        String meanValue = "/home/john/idea/stormCaffe/src/main/resources/VGG/mean.binaryproto";
        String labels   = "/home/john/idea/stormCaffe/src/main/resources/VGG/labels.txt";

        Caffe.set_mode(Caffe.CPU);
        caffe.CaffeClassifier classifier = caffe.CaffeClassifier.create(model, weights, meanValue, labels);

        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Failed to open camera!");
            System.exit(-1);
        }
        else {
            System.out.println("Succeeded to open camera!");
        }
        boolean err = true;
        long frameCount = 1;
        while (err) {
            String resultsStr;
            CvFont font = new CvFont();
            cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 1.5, 1.5, 0, 2, 8);

            Mat frame = new Mat();
            err = capture.read(frame);

            System.out.format("\n-----Frame %d is being analyzed!-----\n", frameCount);

            List<caffe.CaffeClassifier.Prediction> results = classifier.classify(frame, 5);
            for (int i = 0; i < results.size(); i++) {

                resultsStr = results.get(i).label + ": " + String.format("%.02f", results.get(i).confidence);

                System.out.println(resultsStr);

                if (i == 0) {
                    putText(frame, resultsStr, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0, 255, 0), 2, 2, false);
                }
                else if (i == 1) {
                    putText(frame, resultsStr, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0, 255, 0), 2, 2, false);
                }
                else {
                    putText(frame, resultsStr, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 0, 0), 2, 2, false);
                }
            }

            imshow("Object Detection", frame);
            int k = waitKey(1) & 0xff;
            if (k == 27) {
                System.exit(-1);
            }
            frameCount++;
        }
    }
}