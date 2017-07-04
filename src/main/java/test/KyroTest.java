package test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.print.DocFlavor;
import java.io.*;

/**
 * Created by john on 2/7/17.
 */
public class KyroTest {
    public static void main(String[] args) {
        outputByJDK();
        outputByKryo();
        inputByJDK();
        intputByKryo();
    }

    private static void outputByKryo() {
        long startTime = System.currentTimeMillis();
        for (int i = 10000; i < 20000; i++) {
            User user = new User();
            user.setId(i);
            user.setName("Nick Huang");


            try {
                FileOutputStream fos = new FileOutputStream("./data/" + i + ".bin");
                Output output = new Output(fos);
                Kryo kryo = new Kryo();
//                kryo.writeObject(output, user);
                kryo.writeClassAndObject(output, user);

                output.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Output by Kryo cost: " + (endTime - startTime));
    }

    private static void intputByKryo() {
        long startTime = System.currentTimeMillis();
        for (int i = 10000; i < 20000; i++) {
            try {
                FileInputStream fis = new FileInputStream("./data/" + i + ".bin");
                Input input = new Input(fis);
                Kryo kryo = new Kryo();
//                User user = kryo.readObject(input, User.class);
                User user = (User) kryo.readClassAndObject(input);

                input.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Input by Kryo cost: " + (endTime - startTime));
    }

    private static void outputByJDK() {
        long startTime = System.currentTimeMillis();
        for (int i = 20000; i < 30000; i++) {
            User user = new User();
            user.setId(i);
            user.setName("Nick Huang");

            try {
                FileOutputStream fos = new FileOutputStream("./data/" + i + ".bin");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(user);

                fos.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Output by JDK cost: " + (endTime - startTime));
    }

    private static void inputByJDK() {
        long startTime = System.currentTimeMillis();
        for (int i = 20000; i < 30000; i++) {
            try {
                FileInputStream fis = new FileInputStream("./data/" + i + ".bin");
                ObjectInputStream ois = new ObjectInputStream(fis);

                User user = (User) ois.readObject();

                fis.close();
                ois.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Input by JDK cost: " + (endTime - startTime));
    }
}

class User implements Serializable {
    private Integer id;
    private String name;

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + "]";
    }
}