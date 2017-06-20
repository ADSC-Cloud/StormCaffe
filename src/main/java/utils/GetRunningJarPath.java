package utils;

import java.net.URISyntaxException;

/**
 * Created by john on 19/6/17.
 */
public class GetRunningJarPath {
    public static String getRunningJarPath() {
        String runningJarPath = null;
        try {
            runningJarPath = GetRunningJarPath.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return runningJarPath;
    }
}
