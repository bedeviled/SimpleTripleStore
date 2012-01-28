package com.jthalbert;

import org.apache.accumulo.core.client.*;
import org.krakenapps.pcap.decoder.tcp.TcpSegment;
import org.krakenapps.pcap.decoder.tcp.TcpSegmentCallback;
import org.krakenapps.pcap.decoder.tcp.TcpSession;

import java.net.InetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: jthalbert
 * Date: 1/27/12
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 * In general terms, a session is a persistent logical linking of two software application processes,
 * to allow them to exchange data over a prolonged period of time. In some discussions, these sessions
 * are called dialogs; they are roughly analogous to a telephone call made between two people.
 *
 */
public class AccumuloSessionizer  {

    private BatchWriter writer;

    private static class AccumuloTCPCallback implements TcpSegmentCallback {

        public void onReceive(TcpSession session, TcpSegment segment) {
            try {
                InetAddress clientIP = session.getKey().getClientIp();
                InetAddress serverIP = session.getKey().getServerIp();
                int clientPort = session.getKey().getClientPort();
                int serverPort = session.getKey().getServerPort();
            } catch (NullPointerException e) {
                System.out.println("something null");
            }
        }
    }

    public void setWriter(BatchWriter writer) {
        this.writer = writer;
    }

    public AccumuloSessionizer(String tableName) throws AccumuloException,
            AccumuloSecurityException, TableExistsException, TableNotFoundException {
        String instanceName = "accumulo";
        String zookeepers = "localhost";
        String accumuloUser = "root";
        String accumuloPassword = "secret";
        Instance instance= new ZooKeeperInstance(instanceName,zookeepers);
        Connector connector = instance.getConnector(accumuloUser, accumuloPassword.getBytes());
        long memBuf = 1000000l; //bytes to store before sending a batch
        long timeout = 1000l; //milliseconds to wait before sending
        int numThreads = 10;
        if (connector.tableOperations().exists(tableName)) {
            this.setWriter(connector.createBatchWriter(tableName,memBuf,timeout,numThreads));
        } else {
            connector.tableOperations().create(tableName);
            this.setWriter(connector.createBatchWriter(tableName,memBuf,timeout,numThreads));
        }

    }




}
