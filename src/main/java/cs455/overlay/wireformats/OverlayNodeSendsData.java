package cs455.overlay.wireformats;

import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;

public class OverlayNodeSendsData extends Event {

    public int destinationId;
    public int sourceId;
    public int payload;
    public int pathLength;
    public int[] path;

    public byte[] messageBytes;
    public int type;
    public Socket socket;

    public OverlayNodeSendsData() { }


    // PASSING A MESSAGE!!!!!!! Originates in MessagingNode, passed here
    //packs primitives into a byte[]
    public byte[] packBytes(int type, int destinationId, int sourceId, int payload, int[] path, int myId) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        pathLength = path.length;

        dout.writeInt(0);
        dout.writeByte(-1);
        //size is len plus 5 ints * 4 to bytes
//        dout.writeInt((pathLength + 5 ) * 4);
        dout.writeInt(type);

        dout.writeInt(destinationId);
        dout.writeInt(sourceId);
        dout.writeInt(payload);
        dout.writeInt(pathLength+1);

        for(int id : path) {
            dout.writeInt(id);
        }

        dout.writeInt(myId);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

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
        int msgSize = din.readInt();
        type = din.readInt();

        destinationId = din.readInt();
        sourceId = din.readInt();
        payload = din.readInt();
        pathLength = din.readInt();

        for(int i = 0; i < pathLength; i++) {
            path[i] = din.readInt();
        }

        baInputStream.close();
        din.close();
    }
    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now
        TCPSender out = new TCPSender(this.socket,messageBytes);
        Thread sendThread = new Thread(out);
        sendThread.start();
    }
}


