import bolt.Bolt1;
import bolt.Bolt2;
import bolt.Bolt3;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;
import spout.DenseSpout;
import spout.Spout;
import utils.SerializableMat;

/**
 * Created by john on 23/10/17.
 */
public class ClusterStormTest {
    public static void main(String[] args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        Config config = new Config();
        config.registerSerialization(SerializableMat.class);
        config.setNumWorkers(4);
        config.setDebug(true);
        config.setMessageTimeoutSecs(100);
        config.setMaxSpoutPending(100);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("spout", new Spout(), 1);
        builder.setBolt("bolt1", new Bolt1(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt2", new Bolt2(), 1).shuffleGrouping("spout");
        builder.setBolt("bolt3", new Bolt3(), 1).shuffleGrouping("bolt1").shuffleGrouping("bolt2");

        if (args[0].equals("local")) { // Run in local model
            System.out.println("Running in local mode!");
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("ClusterStormTest", config, builder.createTopology());
//            Utils.sleep(10000);
//            localCluster.killTopology("ClusterStormTest");
//            localCluster.shutdown();
        } else {
            System.out.println("Running in cluster mode!");
            StormSubmitter.submitTopology("ClusterStormTest", config, builder.createTopology());
        }
    }
}