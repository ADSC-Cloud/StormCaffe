package utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.*;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by john on 30/6/17.
 */
public class SerializableMat implements KryoSerializable, Serializable {

    /**
     * Kryo Serializable Mat class.
     * Essential fields are image data itself, rows and columns count and type of the data.
     */
    private byte[] data;
    private int rows, cols, type;

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getType() {
        return type;
    }

    public SerializableMat(){

    }

    /**
     * Creates new serializable Mat given its format and data.
     *
     * @param rows Number of rows in the Mat object
     * @param cols Number of columns in the Mat object
     * @param type OpenCV type of the data in the Mat object
     * @param data Byte data containing image.
     */
    public SerializableMat(int rows, int cols, int type, byte[] data) {
        this.rows = rows;
        this.cols = cols;
        this.type = type;
        this.data = data;
    }

    /**
     * Creates new serializable Mat from opencv_core.Mat
     *
     * @param mat The opencv_core.Mat
     */
    public SerializableMat(Mat mat) {
        if (!mat.isContinuous())
            mat = mat.clone();

        this.rows = mat.rows();
        this.cols = mat.cols();
        this.type = mat.type();
        int size = mat.arraySize();
        this.data = new byte[size];

        mat.getByteBuffer().get(this.data);
//        ByteBuffer matBuffer = mat.createBuffer();
//        matBuffer.get(this.data);
    }

    /**
     * Creates new serializable Mat given its format and data.
     * @param input Byte data containing image.
     */
    public SerializableMat(byte[] input) {
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            this.rows = in.readInt();
            this.cols = in.readInt();
            this.type = in.readInt();
            int size = in.readInt();
            this.data = new byte[size];
            int readSize = 0;
            while (readSize < size) {
                readSize += in.read(data, readSize, size - readSize);
            }
//            System.out.println("in: " + this.rows + "-" + this.cols + "-" + this.type + "-" + size + "-" + readSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] toByteArray(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeInt(this.rows);
            out.writeInt(this.cols);
            out.writeInt(this.type);
            out.writeInt(this.data.length);
            out.write(this.data);
            out.close();
            byte[] byteSize = bos.toByteArray();
            bos.close();

//            System.out.println("out: " + this.rows + "-" + this.cols + "-" + this.type + "-" + this.data.length + "-" + byteSize.length);
            return byteSize;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] toByteArray(SerializableMat rawFrame, SerializableMat optFlow) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);

            out.writeInt(rawFrame.rows);
            out.writeInt(rawFrame.cols);
            out.writeInt(rawFrame.type);
            out.writeInt(rawFrame.data.length);
            out.write(rawFrame.data);

            out.writeInt(optFlow.rows);
            out.writeInt(optFlow.cols);
            out.writeInt(optFlow.type);
            out.writeInt(optFlow.data.length);
            out.write(optFlow.data);

            out.close();
            byte[] int_bytes = bos.toByteArray();
            bos.close();

            //System.out.println("out: " + this.rows + "-" + this.cols + "-" + this.type + "-" + this.data.length + "-" + int_bytes.length);
            return int_bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SerializableMat[] toSMat(byte[] input) {
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        ObjectInput in = null;
        SerializableMat rawFrame = new SerializableMat();
        SerializableMat optFlow = new SerializableMat();

        try {
            in = new ObjectInputStream(bis);
            rawFrame.rows = in.readInt();
            rawFrame.cols = in.readInt();
            rawFrame.type = in.readInt();
            int size = in.readInt();
            rawFrame.data = new byte[size];
            int readed = 0;
            while (readed < size) {
                readed += in.read(rawFrame.data, readed, size - readed);
            }
            optFlow.rows = in.readInt();
            optFlow.cols = in.readInt();
            optFlow.type = in.readInt();
            size = in.readInt();
            optFlow.data = new byte[size];
            readed = 0;
            while (readed < size) {
                readed += in.read(optFlow.data, readed, size - readed);
            }

            return new SerializableMat[]{rawFrame, optFlow};
            //System.out.println("in: " + this.rows + "-" + this.cols + "-" + this.type + "-" + size + "-" + readed);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return Converts this Serializable Mat into JavaCV's Mat
     */
    public Mat toJavaCVMat() {
        return new Mat(rows, cols, type, new BytePointer(data));
    }

    /**
     * Override Kryo's default Srialization method
     * @param kryo
     * @param output
     */
    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(this.rows);
        output.writeInt(this.cols);
        output.writeInt(this.type);
        output.writeInt(this.data.length);
        output.writeBytes(this.data);
    }

    /**
     * Override Kryo's default deserialization method
     * @param kryo
     * @param input
     */
    @Override
    public void read(Kryo kryo, Input input) {
        this.rows = input.readInt();
        this.cols = input.readInt();
        this.type = input.readInt();
        int size = input.readInt();
        this.data = input.readBytes(size);
    }
}
