package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;

import java.io.*;

public class PingRandomNode {
    public long countSent;
    public long countRelayed;
    public long countReceived;
    public long sentSum;
    public long sum;
    public int nodeId;

    public byte[] packBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int type = 21;

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);//always 21

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public byte[] nodePackBytes(MessagingNode node) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int type = 21;

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);//always 21
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
    public void unpackBytesFromNode(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        //get to and eat message header
        while(din.readByte() != -1);

        int type = din.readInt();//should be 21
        nodeId = din.readInt();
        countSent = din.readLong();
        countRelayed = din.readLong();
        countReceived = din.readLong();
        sentSum = din.readLong();
        sum = din.readLong();

        baInputStream.close();
        din.close();
    }
}
