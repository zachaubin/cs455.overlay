package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;

public class OverlayNodeReportsTrafficSummary extends Event {



//    need this as instant response to registry requests traffic summary

    //need this in a thread that waits for all nodes to report task complete
    // sends to all asking for traffic summary
    public MessagingNode node;
    public int nodeId;
    public long countSent;
    public long countRelayed;
    public long countReceived;
    public long sentSum;
    public long sum;

    public OverlayNodeReportsTrafficSummary() { }


    public OverlayNodeReportsTrafficSummary(MessagingNode node){
        this.node = node;
    }

    public byte[] packBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int type = 12;

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);//always 12

        dout.writeInt(node.nodeId);
        dout.writeLong(node.countSent.get());
        dout.writeLong(node.countRelayed.get());
        dout.writeLong(node.countReceived.get());
        dout.writeLong(node.sentSum.get());
        dout.writeLong(node.sum.get());


        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    //unpacks primitives from byte[]
    public void unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        //get to and eat message header
        while(din.readByte() != -1);

        int type = din.readInt();//always 10
        nodeId = din.readInt();
        countSent = din.readLong();
        countRelayed = din.readLong();
        countReceived = din.readLong();
        sentSum = din.readLong();
        sum = din.readLong();

        baInputStream.close();
        din.close();
    }



    @Override
    public void run() {
        byte[] msg = null;
        try {
            msg = packBytes();
        } catch (IOException e) {
            System.out.println("ONRTS: Error: could not pack simple message...");
            e.printStackTrace();
        }
        Socket socket = null;
        try {
            socket = new Socket(node.registryHostname,node.portNumber);
        } catch (IOException e) {
            System.out.println("ONRTS: Error: could not open socket to REGISTRY to report traffic summary");
            e.printStackTrace();
        }
        TCPSender tcpSender = new TCPSender(socket,msg);
        Thread tcpSenderThread = new Thread(tcpSender);
        tcpSenderThread.start();
        try {
            tcpSenderThread.join();
        } catch (InterruptedException e) {
            System.out.println("ONRTS: Error: wouldn't send message?");
            e.printStackTrace();
        }
    }
}
