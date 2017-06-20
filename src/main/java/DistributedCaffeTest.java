import bolt.Bolt1;
import bolt.Bolt2;
import bolt.Bolt3;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import spout.Spout;

public class DistributedCaffeTest {

    public static void main(String[] args) throws Exception {

        final String TOPOLOGY_NAME = "DistributedCaffeTest";
        String whereFlag = "local";

        if (args.length == 1)
            whereFlag = args[0];
        else {
            System.out.println("Incorrect mode argument. You should declare mode of running topology (local/cluster).");
            System.exit(-1);
        }

        Config config = new Config();
//        config.registerMetricsConsumer(org.apache.storm.metric.LoggingMetricsConsumer.class, 1);
        config.setNumWorkers(4);
        config.setDebug(true);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new Spout(), 1);
        builder.setBolt("bolt1", new Bolt1(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt2", new Bolt2(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt3", new Bolt3(), 1).shuffleGrouping("bolt1").shuffleGrouping("bolt2");

        if (whereFlag.equals("local")) { // Run in local model

            System.out.println("Running in local mode!");
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
//            Utils.sleep(10000);
//            localCluster.killTopology(TOPOLOGY_NAME);
//            localCluster.shutdown();
        }
        else {
            System.out.println("Running in cluster mode!");
            StormSubmitter.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
        }
    }
}