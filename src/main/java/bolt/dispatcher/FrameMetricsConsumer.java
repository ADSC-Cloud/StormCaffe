package bolt.dispatcher;

import org.apache.storm.metric.LoggingMetricsConsumer;
import org.apache.storm.metric.api.IMetricsConsumer;
import org.apache.storm.task.IErrorReporter;
import org.apache.storm.task.TopologyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 * Created by john on 7/7/17.
 */
public class FrameMetricsConsumer implements IMetricsConsumer {

    public static final Logger LOG = LoggerFactory.getLogger(LoggingMetricsConsumer.class);
    static private String padding = "                       ";

    @Override
    public void prepare(Map map, Object registrationArgument, TopologyContext topologyContext, IErrorReporter iErrorReporter) {
        String padding = "                       ";
    }

    @Override
    public void handleDataPoints(TaskInfo taskInfo, Collection<DataPoint> collection) {
        StringBuilder sb = new StringBuilder();
        String header = String.format("%d\t%15s:%-4d\t%3d:%-11s\t",
                taskInfo.timestamp,
                taskInfo.srcWorkerHost, taskInfo.srcWorkerPort,
                taskInfo.srcTaskId,
                taskInfo.srcComponentId);
        sb.append(header);
        for (DataPoint p : collection) {
            sb.delete(header.length(), sb.length());
            sb.append(p.name)
                    .append(padding).delete(header.length()+23,sb.length()).append("\t")
                    .append(p.value);
            LOG.info(sb.toString());
        }


        // return the required running information
    }

    @Override
    public void cleanup() {

    }
}
