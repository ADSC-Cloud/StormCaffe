package caffe;

import config.StormConfig;

/**
 * Created by john on 19/6/17.
 */
public class Caffe {
    private String LOCAL_DATA_DIR = null;

    public Caffe() {
        LOCAL_DATA_DIR = StormConfig.LOCAL_DATA_DIR;
    }

    public Caffe(String localDataDir) {
        LOCAL_DATA_DIR = localDataDir;
    }
}
