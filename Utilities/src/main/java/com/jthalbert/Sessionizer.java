package com.jthalbert;

import org.krakenapps.pcap.PcapInputStream;
import org.krakenapps.pcap.decoder.ethernet.*;
import org.krakenapps.pcap.decoder.ip.InternetProtocol;
import org.krakenapps.pcap.decoder.ip.IpDecoder;
import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.decoder.ip.IpProcessor;
import org.krakenapps.pcap.decoder.tcp.TcpDecoder;
import org.krakenapps.pcap.decoder.tcp.TcpPortProtocolMapper;
import org.krakenapps.pcap.decoder.udp.UdpDecoder;
import org.krakenapps.pcap.decoder.udp.UdpPortProtocolMapper;
import org.krakenapps.pcap.file.PcapFileInputStream;
import org.krakenapps.pcap.packet.PcapPacket;

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
        File f = new File("/Users/jthalbert/Documents/code/data/outside.tcpdump");

        EthernetDecoder ethernetDecoder = new EthernetDecoder();
        IpDecoder ip = new IpDecoder();
        //TcpDecoder tcp = new TcpDecoder(new TcpPortProtocolMapper());
        //UdpDecoder udp = new UdpDecoder(new UdpPortProtocolMapper());

        ethernetDecoder.register(EthernetType.IPV4,ip);
        //ethernetDecoder.register(EthernetType.IPV4, new MyIpProcessor());
        ip.register(InternetProtocol.TCP,new MyTCPProcessor());
        //ip.register(InternetProtocol.UDP,udp);

        PcapInputStream inputStream = new PcapFileInputStream(f);
        while (true) {
            PcapPacket packet = inputStream.getPacket();
            ethernetDecoder.decode(packet);

            //System.out.println(packet.getPacketHeader().toString());
        }

    }

    private static class MyIpProcessor implements EthernetProcessor {
        public void process(EthernetFrame frame) {
            MacAddress sourceMac = frame.getSource();
            MacAddress destMac = frame.getDestination();
            System.out.println(sourceMac.toString());
        }
    }

    private static class MyTCPProcessor implements IpProcessor {
        public void process(IpPacket packet) {
            InetAddress source = packet.getSourceAddress();
            InetAddress dest = packet.getDestinationAddress();
            System.out.println(source.toString()+"->"+dest.toString());
        }
    }
}
