package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeReportsTaskFinished extends Event {

    public int nodeId;

    public byte[] packBytes(int nodeId) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int type = 10;

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);//always 10
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

        int type = din.readInt();//always 10
        nodeId = din.readInt();

        baInputStream.close();
        din.close();
    }



    //need this in end of send_msgs method after for loop


}
