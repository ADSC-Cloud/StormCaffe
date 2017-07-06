package test;

import java.io.IOException;
import java.util.List;
import static org.bytedeco.javacpp.caffe.Caffe;

import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacpp.opencv_videoio.*;

import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class CaffeExample {

    public static void main(String[] args) throws IOException {

        String modelFile   = "/home/john/CLionProjects/vgg19/VGG_ILSVRC_19_layers_deploy.prototxt";
        String trainedFile = "/home/john/CLionProjects/vgg19/VGG_ILSVRC_19_layers.caffemodel";
        String meanFile    = "/home/john/CLionProjects/vgg19/mean.binaryproto";
        String labelFile   = "/home/john/CLionProjects/vgg19/data/synset_words.txt";

        Caffe.set_mode(Caffe.CPU);
        CaffeClassifier classifier = CaffeClassifier.create(modelFile, trainedFile, meanFile, labelFile);

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
            String str;
            CvFont font = new CvFont();
            cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 1.5, 1.5, 0, 2, 8);

            Mat frame = new Mat();
            err = capture.read(frame);

            System.out.format("---------- Prediction for %d ----------\n", frameCount);

            List<CaffeClassifier.Prediction> results = classifier.classify(frame, 5);
            for (int i = 0; i < results.size(); i++) {
                str = results.get(i).label + ": " + String.format("%.02f", results.get(i).confidence);
                System.out.println(str);
                if (i == 0)
                    putText(frame, str, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0 ,255, 0), 2, 2, false);
                else if (i == 1)
                    putText(frame, str, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 0 ,255, 0), 2, 2, false);
                else
                    putText(frame, str, new Point(10, 30 + i * 30), CV_FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255 ,0, 0), 2, 2, false);
            }

            imshow("Result", frame);
            int k = waitKey(1) & 0xff;
            if (k == 27) {
                System.exit(-1);
            }
            frameCount++;
        }
    }
}