import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Created by kang on 2019/1/14.
 */
public class TrafficSpout extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private TrafficCollector tc;
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.collector = spoutOutputCollector;
        this.tc = new TrafficCollector();
    }

    public void nextTuple() {
        try {
            this.collector.emit(new Values(tc.getPacket()));
        }catch (Exception e){

        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("packet"));
    }
}
