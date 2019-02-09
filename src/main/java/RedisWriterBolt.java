import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by kang on 2019/1/31.
 */
public class RedisWriterBolt extends BaseRichBolt {
    private OutputCollector outputCollector;
    private Jedis jedis;
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        this.jedis = new Jedis("127.0.0.1", 6379);
    }

    public void execute(Tuple tuple) {
        String streamComponent = tuple.getSourceComponent();
        //System.out.println(streamComponent);
        String key = tuple.getStringByField("key");
        String result = tuple.getStringByField("application_type");
        if(streamComponent.equals("classifierbolt5")){
            this.jedis.set(key, result);
        }else if(streamComponent.equals("classifierbolt")){
            this.jedis.set(key+"-10", result);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
