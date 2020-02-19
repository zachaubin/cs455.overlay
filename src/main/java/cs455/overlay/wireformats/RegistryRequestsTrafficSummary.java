package cs455.overlay.wireformats;

import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.transport.TCPSender;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class RegistryRequestsTrafficSummary extends Event implements Runnable {

//need this in a thread that waits for all nodes to report task complete
    // sends to all asking for traffic summary
    public Registry registry;

    public RegistryRequestsTrafficSummary(Registry registry){
        this.registry = registry;
    }

    public byte[] packBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int type = 11;

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);//always 11

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    @Override
    public void run() {
        byte[] msg = null;
        try {
            msg = packBytes();
        } catch (IOException e) {
            System.out.println("RRTS: Error: could not pack simple message...");
            e.printStackTrace();
        }
        Socket socket = null;
        for(RoutingEntry e : registry.nodes.table){
            try {
                sleep(69);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            try {
                socket = new Socket(e.nodeHost,e.nodePort);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
//            socket = registry.cache.sockets[e.nodeId];
            TCPSender tcpSender = new TCPSender(socket,msg);
            Thread tcpSenderThread = new Thread(tcpSender);
            tcpSenderThread.start();
            try {
                tcpSenderThread.join();
            } catch (InterruptedException ex) {
                System.out.println("RRTS: Error: wouldn't send message?");
                ex.printStackTrace();
            }
        }

    }
}
