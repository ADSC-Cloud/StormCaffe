package test;

import static org.bytedeco.javacpp.opencv_videoio.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.caffe.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by john on 5/7/17.
 */
public class ObjectDetectionCaffe {

    private static final int IMAGE_SIZE = 224;
    private static final int LABEL_COUNT = 1000;

    private static void bubble_sort(float[] features, int[] sorted_idx) {
        int i, j;
        float tmp;
        int tmp_idx;

        for (i = 0; i < LABEL_COUNT; i++) {
            sorted_idx[i] = i;
        }

        for (i = 0; i < LABEL_COUNT; i++) {
            for (j = 0; j < LABEL_COUNT - 1; j++) {
                if (features[j] < features[j + 1]) {
                    tmp = features[j];
                    features[j] = features[j + 1];
                    features[j + 1] = tmp;

                    tmp_idx = sorted_idx[j];
                    sorted_idx[j] = sorted_idx[j + 1];
                    sorted_idx[j + 1] = tmp_idx;
                }
            }
        }
    }

    private static void get_top5(float[] features, int[] arr) {
        int i;
        int[] sorted_idx = new int[LABEL_COUNT];

        bubble_sort(features, sorted_idx);

        for (i = 0; i < 5; i++) {
            arr[i] = sorted_idx[i];
        }
    }

    private static void get_label(String filename, String[] label) {

        try {
            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            int i;
            for ( i = 0; i < LABEL_COUNT; i++) {
                label[i] = bufferedReader.readLine();
            }
            fileReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void draw_output_ipl(IplImage img, float[] output, int[] idx, String[] label) {
        int i;
        CvFont font = new CvFont();
        String str;
        cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 1.5, 1.5, 0, 2, 8);

        for (i = 0; i < 5; i++) {
            str = label[idx[i]] + ", " + String.format("%.03f", output[i]);
            if (i == 0)
                cvPutText(img, str, cvPoint(10, 30 + i * 30), font, CV_RGB(255, 0 ,0));
            else if (i == 1)
                cvPutText(img, str, cvPoint(10, 30 + i * 30), font, CV_RGB(0, 0, 255));
            else
                cvPutText(img, str, cvPoint(10, 30 + i * 30), font, CV_RGB(0, 255, 0));
        }
    }

    public static void main(String[] args) {

        // mode setting
        Caffe.set_mode(Caffe.GPU);

        // gpu device number
        int device_id = 0;
        Caffe.SetDevice(device_id);

        // net
        String model = "/home/john/CLionProjects/vgg19/VGG_ILSVRC_19_layers_deploy.prototxt";
        String weights = "/home/john/CLionProjects/vgg19/VGG_ILSVRC_19_layers.caffemodel";
        FloatNet caffe_test_net = new FloatNet(model, TEST);
        caffe_test_net.CopyTrainedLayersFrom(weights);

//        float[] iter_loss = new float[1];
//        FloatBlobVector bottom_vec = new FloatBlobVector();
//        FloatBlobVector test_result = caffe_test_net.Forward(bottom_vec, iter_loss);

        // read labels
        String labelPath = "/home/john/CLionProjects/vgg19/data/synset_words.txt";
        String[] label = new String[1000];
        get_label(labelPath, label);

        int i, j, k;
        int[] top5_idx = new int[5];
        float mean_val[] = {103.939f, 116.779f, 123.68f};

        // input
        float[] output = new float[1000];
        FloatBlobVector input_vec = new FloatBlobVector();
        FloatBlob blob = new FloatBlob(1, 3, IMAGE_SIZE, IMAGE_SIZE);

        // open camera
        IplImage frame, crop_image, small_image;
        CvCapture capture = cvCreateCameraCapture(0);

        crop_image = cvCreateImage(cvSize(480, 480), 8, 3);
        small_image = cvCreateImage(cvSize(IMAGE_SIZE, IMAGE_SIZE), 8 , 3);

        cvNamedWindow("Test");

        int key = 0;
        while (key != 27) {
            cvGrabFrame(capture);
            frame = cvRetrieveFrame(capture);

            // crop input image
            int crop_widthStep = crop_image.widthStep();
            int small_widthStep = small_image.widthStep();
            int frame_widthStep = frame.widthStep();
            int crop_nChannels = crop_image.nChannels();
            int small_nChannels = small_image.nChannels();
            int frame_nChannels = frame.nChannels();

            // crop input image
            for (i = 0; i < 480; i++) {
                for (j = 0; j < 480; j++) {
                    byte b0 = frame.imageData().get(i * frame_widthStep + j * frame_nChannels + 0);
                    byte b1 = frame.imageData().get(i * frame_widthStep + j * frame_nChannels + 1);
                    byte b2 = frame.imageData().get(i * frame_widthStep + j * frame_nChannels + 2);
                    crop_image.imageData().put(i * crop_widthStep + j * crop_nChannels + 0, b0);
                    crop_image.imageData().put(i * crop_widthStep + j * crop_nChannels + 1, b1);
                    crop_image.imageData().put(i * crop_widthStep + j * crop_nChannels + 2, b2);
                }
            }

            cvResize(crop_image, small_image);
            for (k = 0; k < 3; k++) {
                for (i = 0; i < IMAGE_SIZE; i++) {
                    for (j = 0; j < IMAGE_SIZE; j++) {
                        float val = (float)(char) small_image.imageData().get(i * small_widthStep + j * small_nChannels + k) - mean_val[k];
                        int index = blob.offset(0, k, i, j);
                        blob.mutable_cpu_data().put(index, val);
                    }
                }
            }

            input_vec.put(blob);

            // forward propagation
            float[] loss = new float[1];
//            FloatBlobVector bottom_vec = new FloatBlobVector();

            caffe_test_net.input_blobs().put(blob);
            FloatBlobVector result = caffe_test_net.Forward(loss);
//            FloatBlobVector result = caffe_test_net.Forward(input_vec, loss);

            // copy output
            for (i = 0; i < LABEL_COUNT; i++) {
                output[i] = result.get(0).cpu_data().get(i);
            }

            get_top5(output, top5_idx);
            draw_output_ipl(crop_image, output, top5_idx, label);
            cvShowImage("Test", crop_image);

            if ((key = cvWaitKey(20) & 0xff) == 27) {
                System.exit(-1);
            }

//            input_vec.close();
        }
        // release memory
        cvReleaseCapture(capture);
        cvReleaseImage(crop_image);
        cvReleaseImage(small_image);
    }
}
