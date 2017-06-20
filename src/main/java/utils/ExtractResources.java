package utils;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by john on 19/6/17.
 */
public class ExtractResources {

    /**
     * Extract resources (a directory or a single file) of a jar file to target folder.
     * @param jarFileAbsolutePath Absolute path to jar file that is to be decompressed.
     * @param extractTo The destination folder to which the jar file will be decompressed to.
     * @param name Specify the name of the uppermost folder in resource package.
     */
    public static void extractResources (String jarFileAbsolutePath, String extractTo, String name) {

        String currentDirectory = extractTo;
        boolean is_decompressed = false;

        File currentDirectoryFile = new File(extractTo);
        if (currentDirectoryFile.list().length > 0)
            is_decompressed = true;

        final int BUFFER_SIZE = 2048;

        // process only when the file is a jar file and it has not been decompressed
        if (jarFileAbsolutePath.endsWith(".jar") && !is_decompressed) {
            File file = new File(jarFileAbsolutePath);
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Enumeration<JarEntry> jarFileEntries = jarFile.entries();

            while (jarFileEntries.hasMoreElements()) {
                JarEntry jarEntry = jarFileEntries.nextElement();
                String currentEntryName = jarEntry.getName();

                if (currentEntryName.startsWith(name)) {
                    File destinationFile = new File(extractTo, currentEntryName);

                    File destinationParent = destinationFile.getParentFile();
                    destinationParent.mkdirs();

                    // process jarEntry which is a file type to the new destination
                    if (!jarEntry.isDirectory()) {
                        BufferedInputStream bis = null;
                        try {
                            bis = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        int currentByte;
                        byte data[] = new byte[BUFFER_SIZE];

                        // write current file to disk
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(destinationFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);

                        try {
                            while ((currentByte = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                                bos.write(data, 0, currentByte);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // flush output stream and close streams once finished
                        try {
                            bos.flush();
                            bis.close();
                            fos.close();
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // recursively call decompression functions to all jar files within the parent jar file
                    if (currentEntryName.endsWith("jar")) {
                        // create a new folder in the name of jar
                        String currentJarAbsolutePath = currentDirectory + File.separator + currentEntryName;
                        currentDirectory = currentJarAbsolutePath.substring(0, currentJarAbsolutePath.length() - 4);

                        File subJarFilePath = new File(currentDirectory);
                        subJarFilePath.mkdir();

                        extractResources(currentJarAbsolutePath, currentDirectory, name);
                    }
                }
            }
            try {
                jarFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Decompression succeeded!");
    }
}
