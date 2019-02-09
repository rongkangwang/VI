import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.pcap4j.packet.Packet;

import java.util.List;
import java.util.Map;

/**
 * Created by kang on 2019/1/30.
 */
public class FilterBolt extends BaseRichBolt {
    private OutputCollector outputCollector;
    //several rules, benchmark values
    private int minLength = 50;
    private int maxLength = 256;
    private int minMeanLength = 50;
    private int maxMeanLength = 256;
    private double threshold = 0.25;
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }

    public void execute(Tuple tuple) {
//        System.out.println("FilterBolt -> "+tuple.getSourceStreamId());
//        if(tuple.getSourceStreamId().equals("10")) {
            String key = tuple.getStringByField("key");
            List<Packet> pendingflowsList = (List<Packet>) tuple.getValueByField("flow");
            System.out.println(key+" -> 10 -> "+pendingflowsList.size());
            int sumLength = 0;
            for (Packet pkt : pendingflowsList) {
                sumLength += pkt.length();
            }
            int meanLength = sumLength / pendingflowsList.size();
            int countLowerMin = 0;
            for (Packet pkt : pendingflowsList) {
                if (pkt.length() < minLength) {
                    countLowerMin++;
                }
            }
            int countLowerMax = 0;
            for (Packet pkt : pendingflowsList) {
                if (pkt.length() > maxLength) {
                    countLowerMax++;
                }
            }
            if (meanLength < maxMeanLength && meanLength > minMeanLength && countLowerMax / pendingflowsList.size() < threshold && countLowerMin / pendingflowsList.size() < threshold) {
                this.outputCollector.emit(new Values(key, pendingflowsList));
            } else {
                //System.out.println("Flow " + key + " is filtered out (FilterBolt).");
            }
//        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("key", "flow"));
    }
}
