import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.pcap4j.packet.Packet;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kang on 2019/1/30.
 */
public class PendingFlowsBolt extends BaseRichBolt {
    private OutputCollector outputCollector;
    private Map<String, List<Packet>> pendingflows;
    private Jedis jedis;

    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        this.pendingflows = new ConcurrentHashMap<String, List<Packet>>();
        this.jedis = new Jedis("127.0.0.1", 6379);
    }

    public void execute(Tuple tuple){
        String key = tuple.getStringByField("key");
        Packet payload = (Packet) tuple.getValueByField("payload");
        List<Packet> pendingflowsList;
        if (jedis.get(key + "-10") != null) {
            if (pendingflows.containsKey(key)) {
                pendingflows.remove(key);
                System.out.println("Flow " + key + " is filtered out (PendingFlowsBolt).");
            }
        } else {
            if ((pendingflowsList = pendingflows.get(key)) != null) {
                pendingflowsList.add(payload);
                if (pendingflowsList.size() == 5) {
                    try {
                        if(!this.jedis.exists(key)) {
                            List<Packet> copy_pendingflowsList = deepCopy(pendingflowsList);
                            this.outputCollector.emit("5", new Values(key, copy_pendingflowsList));
                            this.jedis.set(key, null);
                        }
                    }catch (Exception e){

                    }
                } else if (pendingflowsList.size() == 10) {
                    try {
                        if(!this.jedis.exists(key+"-10")) {
                            List<Packet> copy_pendingflowsList = deepCopy(pendingflowsList);
                            this.outputCollector.emit("10", new Values(key, copy_pendingflowsList));
                            this.jedis.set(key, null);
                            pendingflows.remove(key);
                        }
                    }catch (Exception e){

                    }
                }
            } else {
                List<Packet> pendingflowsl = new ArrayList<Packet>();
                pendingflowsl.add(payload);
                pendingflows.put(key, pendingflowsl);
            }
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream("5", new Fields("key", "flow"));
        outputFieldsDeclarer.declareStream("10", new Fields("key", "flow"));
    }

    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }
}
