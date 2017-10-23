package test;

import config.StormConfig;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by john on 14/6/17.
 */
public class JarTest {
    private static void extractResources (String jarFileAbsolutePath, String extractTo, String name) throws IOException {

        String currentDirectory = extractTo;

        final int BUFFER_SIZE = 2048;

        // process only when the file is a jar file
        if (jarFileAbsolutePath.endsWith(".jar")) {
            File file = new File(jarFileAbsolutePath);
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> jarFileEntries = jarFile.entries();

            while (jarFileEntries.hasMoreElements()) {
                JarEntry jarEntry = jarFileEntries.nextElement();
                String currentEntryName = jarEntry.getName();

                // only extract files in resource folder
                if (currentEntryName.startsWith(name)) {
                    File destinationFile = new File(extractTo, currentEntryName);
                    File destinationParent = destinationFile.getParentFile();

                    destinationParent.mkdirs();

                    // process jarEntry which is a file type to the new destination
                    if (!jarEntry.isDirectory()) {
                        BufferedInputStream bis = new BufferedInputStream(jarFile.getInputStream(jarEntry));

                        int currentByte;
                        byte data[] = new byte[BUFFER_SIZE];

                        // write current file to disk
                        FileOutputStream fos = new FileOutputStream(destinationFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);

                        while ((currentByte = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                            bos.write(data, 0, currentByte);
                        }

                        // flush output stream and close streams once finished
                        bos.flush();
                        bis.close();
                        fos.close();
                        bos.close();
                    }

                    // recursively call decompression functions to all jar files within the parent jar file
                    if (currentEntryName.endsWith("jar")) {
                        // create a new folder in the name of jar
                        String currentJarAbsolutePath = currentDirectory + File.separator + currentEntryName;
                        currentDirectory = currentJarAbsolutePath.substring(0, currentJarAbsolutePath.length() - 4);

                        File subJarFilePath = new File(currentDirectory);
                        subJarFilePath.mkdirs();

                        extractResources(currentJarAbsolutePath, currentDirectory, name);
                    }
                }
            }
            jarFile.close();
        }
        System.out.println("Decompression succeeded!");
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        String jarFileAbsolutePath = "/home/john/idea/stormCaffe/target/stormCaffe-1.0-SNAPSHOT.jar";
        final String baseLocalDataDir = StormConfig.LOCAL_DATA_DIR;

        extractResources(jarFileAbsolutePath, baseLocalDataDir, "example");
    }
}