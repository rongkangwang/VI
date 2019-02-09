import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.pcap4j.core.PcapNetworkInterface;

/**
 * Created by kang on 2019/1/28.
 */
public class VoIPTopology {
    public static void main(String[] args) throws Exception{
        TopologyBuilder tb = new TopologyBuilder();
        tb.setSpout("spout", new TrafficSpout(), 3);  // executor num
        //tb.setSpout("spout", new TrafficSpout(), 3).setNumTasks(3);  // task num
        tb.setBolt("udpbolt", new UDPBolt()).shuffleGrouping("spout");
        //tb.setBolt("pendingflowsbolt", new PendingFlowsBolt()).globalGrouping("udpbolt");
        tb.setBolt("pendingflowsbolt", new PendingFlowsBolt()).fieldsGrouping("udpbolt", new Fields("key"));
        //5
        tb.setBolt("classifierbolt5", new ClassifierBolt5()).shuffleGrouping("pendingflowsbolt", "5");
        //10
        tb.setBolt("filterbolt", new FilterBolt()).shuffleGrouping("pendingflowsbolt", "10");
        tb.setBolt("classifierbolt", new ClassifierBolt()).shuffleGrouping("filterbolt");
        //merge
        tb.setBolt("rediswriterbolt", new RedisWriterBolt()).shuffleGrouping("classifierbolt5").shuffleGrouping("classifierbolt");

        Config config = new Config();
        // config.setNumWorkers(3); //worker num

        if(args!=null&&args.length>0){
            StormSubmitter.submitTopology(args[0], config, tb.createTopology());
        }else {
            LocalCluster lc = new LocalCluster();
            lc.submitTopology("localmode", config, tb.createTopology());
        }
    }
}
