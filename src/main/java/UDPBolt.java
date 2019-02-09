import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by kang on 2019/1/28.
 */
public class UDPBolt extends BaseRichBolt {
    private OutputCollector outputCollector;
    private Jedis jedis;
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        this.jedis = new Jedis("127.0.0.1", 6379);
    }

    public void execute(Tuple tuple) {
        Packet pkt = (Packet)tuple.getValueByField("packet");
//        System.out.println("wang");
//        System.out.println(pkt);
        if(pkt.length()>50&&pkt.length()<300) {
            IpV4Packet ipV4Packet = pkt.get(IpV4Packet.class);
            if (ipV4Packet != null) {
                UdpPacket udpPacket = ipV4Packet.get(UdpPacket.class);
                if (udpPacket != null) {
                    String srcAddr = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
                    String dstAddr = ipV4Packet.getHeader().getDstAddr().getHostAddress();
                    String srcPort = udpPacket.getHeader().getSrcPort().valueAsString();
                    String dstPort = udpPacket.getHeader().getDstPort().valueAsString();
                    StringBuilder sb = new StringBuilder(srcAddr).append("-").append(dstAddr).append("-").append(srcPort).append("-").append(dstPort);
                    String key = sb.toString();
                    System.out.println("start");
                    System.out.println(udpPacket.getHeader().getSrcPort().value());
                    System.out.println(udpPacket.getHeader().getSrcPort().valueAsInt());
                    System.out.println(udpPacket.getHeader().getSrcPort().valueAsString());
                    if(jedis.get(key+"-10")!=null){
                        // redis db has result, how to process packets next step
                        //System.out.println("Packet " + key + " is filtered out (UDPBolt).");
                    } else {
                        this.outputCollector.emit(new Values(key, udpPacket.getPayload()));
                    }
                }
            }
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("key", "payload"));
    }
}
