package bolt.dispatcher;

import org.apache.storm.metric.api.*;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;


/**
 * Created by john on 6/7/17.
 */
public class DispatcherBolt extends BaseRichBolt {
    private OutputCollector collector;
    private final int COUNT_RECORD_INTERVAL = 10; // default recording mean frame processing time interval time
    private final int MEAN_RECORD_INTERVAL = 10; // default recording mean frame processing time interval time

    // Metrics
    // Note: these must be declared as transient since they are not Serializable
    private transient CountMetric countMetric;
    private transient MultiCountMetric frameCountMetric;
    private transient ReducedMetric meanFrameProcessingTimeMetric;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;

        // Metrics must be initialized and registered in the prepare() method for bolts,
        // or the open() method for spouts.  Otherwise, an Exception will be thrown
        countMetric = new CountMetric();
        frameCountMetric = new MultiCountMetric();
        meanFrameProcessingTimeMetric = new ReducedMetric(new MeanReducer());

        context.registerMetric("execute_count", countMetric, COUNT_RECORD_INTERVAL);
        context.registerMetric("word_count", frameCountMetric, MEAN_RECORD_INTERVAL);
        context.registerMetric("word_length", meanFrameProcessingTimeMetric, MEAN_RECORD_INTERVAL);
    }

    @Override
    public void execute(Tuple tuple) {
        collector.emit(tuple, new Values(tuple.getString(0) + "!!!"));
        collector.ack(tuple);

        updateMetrics(tuple.getString(0));
    }

    void updateMetrics(String word) {
        countMetric.incr();
        frameCountMetric.scope(word).incr();
        meanFrameProcessingTimeMetric.update(word.length());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }
}