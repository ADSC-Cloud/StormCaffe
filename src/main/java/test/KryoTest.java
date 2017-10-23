package test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.apache.commons.io.FileUtils;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests the performance of default JAVA serializer and Kryo serializer on both Srialization
 * and de-Srialization. User class objects are to be written to and read from disk. All data are stored
 * in ./data/ directory and will be deleted before exiting the program.
 */
public class KryoTest {

    private static class User implements Serializable {
        private static final long serialVersionUID = 1;
        private String name;
        private int age;
        private Map<String, Integer> map;

        public User() {}
        public User(String name, int age, Map<String, Integer> map) {
            this.name = name;
            this.age = age;
            this.map = map;
        }

        public String getName() {
            return this.name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getAge() {
            return this.age;
        }
        public void setAge(int age) {
            this.age = age;
        }
        public Map<String, Integer> getMap() {
            return map;
        }
        public void setMap(Map<String, Integer> map) {
            this.map = map;
        }
    }

    public static void main(String[] args) throws IOException {

        // create necessary directories including necessary parent directories
        File file = new File("./data/");
        file.mkdirs();

        System.out.println("Start benchmarking!");
        outputByJDK();
        outputByKryo();
        inputByJDK();
        intputByKryo();

        System.out.println("Start deleting temporary files!");
        FileUtils.deleteDirectory(file);
        System.out.println("All steps finished!");
    }

    private static void outputByKryo() {
        long startTime = System.currentTimeMillis();
        Kryo kryo = new Kryo();

        for (int i = 100000; i < 200000; i++) {
            User user = new User();

            Map<String, Integer> map = new HashMap<>(2);
            map.put("zhang0", i);
            map.put("zhang1", i);

            user.setName("zhang");
            user.setAge(i);
            user.setMap(map);

            try {
                FileOutputStream fos = new FileOutputStream("./data/" + i + ".bin");
                Output output = new Output(fos);
                kryo.writeObject(output, user);
//                kryo.writeClassAndObject(output, user);

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
        Kryo kryo = new Kryo();

        for (int i = 100000; i < 200000; i++) {
            try {
                FileInputStream fis = new FileInputStream("./data/" + i + ".bin");
                Input input = new Input(fis);

                User user = kryo.readObject(input, User.class);
//                User user = (User) kryo.readClassAndObject(input);

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

        for (int i = 200000; i < 300000; i++) {
            User user = new User();

            Map<String, Integer> map = new HashMap<>(2);
            map.put("zhang0", i);
            map.put("zhang1", i);

            user.setName("zhang");
            user.setAge(i);
            user.setMap(map);

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
        for (int i = 200000; i < 300000; i++) {
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