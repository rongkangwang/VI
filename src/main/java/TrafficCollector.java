import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.namednumber.UdpPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

import java.io.Serializable;
import java.net.Inet4Address;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by kang on 2019/1/28.
 */
public class TrafficCollector{
    private PcapNetworkInterface device;
    private PcapHandle handler;
    public TrafficCollector(){
        try {
            System.out.println("testdev");
            device = Pcaps.getDevByName("en0");
            int snapLen = 65536;
            int timeout = 10;
            handler = device.openLive(snapLen, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);
        }catch (PcapNativeException e){
            System.out.println(e.getMessage());
        }
    }

    public Packet getPacket() throws Exception{
        return this.handler.getNextPacketEx();
    }

    public void capture() throws Exception{
//        this.handler.loop(-1, new Listener(), new PacketExecutor());
        this.handler.loop(-1, new Listener());
    }

    private class Listener implements PacketListener{

        public void gotPacket(PcapPacket pcapPacket) {
            IpV4Packet ipV4Packet = pcapPacket.get(IpV4Packet.class);
            String srcAddr = ipV4Packet.getHeader().getSrcAddr().getHostAddress();
            UdpPacket udpPacket = ipV4Packet.get(UdpPacket.class);
            int srcPort = udpPacket.getHeader().getSrcPort().value();
            System.out.println(srcAddr+"-"+srcPort);
        }
    }

    private class PacketExecutor implements Executor{

        public void execute(Runnable command) {
            command.run();
        }
    }

    public static void main(String[] args) {
//        try{
//            List<PcapNetworkInterface> devList = Pcaps.findAllDevs();
//            for (int i=0;i<devList.size();i++){
//                System.out.println(devList.get(i).getName());
//            }
//        }catch (PcapNativeException e){
//            System.out.println(e.getMessage());
//        }
//        try {
//            TrafficCollector tc = new TrafficCollector();
//            System.out.println(tc.getPacket());
//        }catch (Exception e){
//            System.out.println(e.getMessage());
//        }
//        try{
//            TrafficCollector tc = new TrafficCollector();
//            tc.capture();
//        }catch (Exception e){
//
//        }
//        Jedis jedis =  new Jedis("127.0.0.1", 6379);
//        String value = jedis.get("test");
//        System.out.println(value);
    }
}
