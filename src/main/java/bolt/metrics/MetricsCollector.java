package bolt.metrics;

import org.apache.storm.metric.api.CountMetric;
import org.apache.storm.metric.api.IMetric;
import org.apache.storm.metric.api.MeanReducer;

/**
 * Created by john on 7/7/17.
 */
public class MetricsCollector {
    class count implements IMetric {

        @Override
        public Object getValueAndReset() {
            return null;
        }
    }

    CountMetric countMetric;
    MeanReducer s;
}
