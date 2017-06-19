package bolt;

import config.StormConfig;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import utils.ExtractResources;
import utils.GetRunningJarPath;

import java.io.IOException;
import java.util.Map;

public class Bolt2 extends BaseRichBolt {

    OutputCollector outputCollector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;

        try {
            ExtractResources.extractResources(GetRunningJarPath.getRunningJarPath(), StormConfig.LOCAL_DATA_DIR, "example");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {

        // prepare and emit other information to default stream
        String appendedWord = tuple.getString(0) + "!!!";
        outputCollector.emit(tuple, new Values(appendedWord));

//        try {
//            CaffeForward.caffeTest();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        outputCollector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("AppendedWord"));
    }
}