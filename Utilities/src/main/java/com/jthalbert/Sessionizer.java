package com.jthalbert;

import org.krakenapps.pcap.PcapInputStream;
import org.krakenapps.pcap.decoder.ethernet.EthernetDecoder;
import org.krakenapps.pcap.decoder.ethernet.EthernetFrame;
import org.krakenapps.pcap.decoder.ethernet.EthernetProcessor;
import org.krakenapps.pcap.decoder.ethernet.EthernetType;
import org.krakenapps.pcap.decoder.ip.InternetProtocol;
import org.krakenapps.pcap.decoder.ip.IpDecoder;
import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.decoder.ip.IpProcessor;
import org.krakenapps.pcap.decoder.tcp.*;
import org.krakenapps.pcap.decoder.udp.UdpDecoder;
import org.krakenapps.pcap.decoder.udp.UdpPortProtocolMapper;
import org.krakenapps.pcap.file.PcapFileInputStream;
import org.krakenapps.pcap.packet.PcapPacket;
import org.krakenapps.pcap.util.Buffer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: jthalbert
 * Date: 1/24/12
 * Time: 8:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class Sessionizer {
    public static void main(String[] args) throws IOException {
        File f = new File("/Users/jthalbert/Documents/code/Utilities/src/main/resources/test.pcap");

        EthernetDecoder ethernetDecoder = new EthernetDecoder();
        IpDecoder ip = new IpDecoder();
        TcpDecoder tcp = new TcpDecoder(new TcpPortProtocolMapper());
        tcp.registerSegmentCallback(new myTcpSegmentCallbak());
        UdpDecoder udp = new UdpDecoder(new UdpPortProtocolMapper());

        ethernetDecoder.register(EthernetType.IPV4,ip);
        ip.register(InternetProtocol.TCP,new MyTCPProcessor());
        //ip.register(InternetProtocol.UDP,new MyUDPProcessor());
        //ip.register(InternetProtocol.TCP,tcp);
        //ethernetDecoder.register(EthernetType.IPV4, new MyIpProcessor());
        //ip.register(InternetProtocol.TCP,new MyTCPProcessor());


        PcapInputStream inputStream = new PcapFileInputStream(f);
        while (true) {
            PcapPacket packet = inputStream.getPacket();
            ethernetDecoder.decode(packet);

            //System.out.println(packet.getPacketHeader().toString());
        }

    }

    private static class MyEthernetProcessor implements EthernetProcessor {

        public void process(EthernetFrame frame) {

        }
    }

    private static class MyTCPProcessor implements IpProcessor {
        public void process(IpPacket packet) {
            InetAddress sourceIP = packet.getSourceAddress();
            InetAddress destIP = packet.getDestinationAddress();

            Buffer data = packet.getData();
            int tcpLength = packet.getTotalLength() - packet.getIhl();
            TcpPacket tcpPacket = TcpPacket.parse(sourceIP,destIP,tcpLength,data);
            int sourcePort = tcpPacket.getSourcePort();
            int destPort = tcpPacket.getDestinationPort();
            Buffer tcpdata = tcpPacket.getData();
            if (tcpdata != null ){
                System.out.println(tcpdata.getString(tcpdata.readableBytes()));
            }
            //System.out.println(sourceIP.getHostAddress()+":"+sourcePort+"->"+destIP.getHostAddress()+":"+destPort);
            //System.out.println(tcpPacket.toString());
        }
    }

    private static class myTcpSegmentCallbak implements TcpSegmentCallback {
        public void onReceive(TcpSession session, TcpSegment segment) {
            try {
                InetAddress clientIP = session.getKey().getClientIp();
                InetAddress serverIP = session.getKey().getServerIp();
                int clientPort = session.getKey().getClientPort();
                int serverPort = session.getKey().getServerPort();
                System.out.println(clientIP.getHostAddress()+":"+clientPort);
            } catch (Exception e) {

            }
        }
    }


    private static class MyUDPProcessor implements IpProcessor {
        public void process(IpPacket packet) {
            InetAddress sourceIP = packet.getSourceAddress();
            InetAddress destIP = packet.getDestinationAddress();
            Buffer data = packet.getData();
            int sourcePort = (int) data.getShort() & 0xffff;
            int destPort = (int) data.getShort() & 0xffff;
            System.out.println(sourceIP.getHostAddress()+":"+sourcePort+"->"+destIP.getHostAddress()+":"+destPort);
        }
    }
}
