package spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Spout extends BaseRichSpout implements IRichSpout {

    private SpoutOutputCollector spoutOutputCollector;
    Random rand;
    private String[] words = {"Hortonworks", "MapR", "Cloudera", "Hadoop", "Kafka", "Spark"};

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        rand = new Random();
    }

    @Override
    public void nextTuple() {
        String word = words[rand.nextInt(words.length)];

        String msgId = UUID.randomUUID().toString();
        spoutOutputCollector.emit(new Values(word), msgId);
    }

    @Override
    public void ack(Object msgId) {

    }

    @Override
    public void fail(Object msgId) {
        System.out.println("Failed to ack message tuple: " + msgId);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("OriginalWord"));
    }
}
