package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeSendsDeregistration extends Event {

    public int nodeId;
    int type;//5

    //packs primitives into a byte[]
    public byte[] packBytes(int nodeId) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //type, host length, hostname, nodeId
        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(4);//type
        dout.writeInt(nodeId);

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

        type = din.readInt();
        nodeId = din.readInt();

        baInputStream.close();
        din.close();
    }



}
