import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.pcap4j.packet.Packet;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by kang on 2019/1/30.
 */
public class ClassifierBolt extends BaseRichBolt{
    private OutputCollector outputCollector;
    //private Jedis jedis;
    private Random random;
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        //this.jedis = new Jedis("127.0.0.1", 6379);
        this.random = new Random();
    }

    public void execute(Tuple tuple) {
        String key = tuple.getStringByField("key");
        List<Packet> pendingflowsList = (List<Packet>) tuple.getValueByField("flow");
//        System.out.println("10 -> "+pendingflowsList.size());
        try {
            wait(1000);
        }catch (Exception e){

        }
        String value = String.valueOf(this.random.nextInt(10));
        //this.jedis.set(key+"-10", value);
        //System.out.println(key+"-10 -> "+value);
        this.outputCollector.emit(new Values(key, value));
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("key", "application_type"));
    }
}
