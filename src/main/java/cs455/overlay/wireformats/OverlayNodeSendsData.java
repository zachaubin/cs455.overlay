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

        System.out.println(">PACK:type:"+type);
        System.out.println(">PACK:destinationId:"+destinationId);
        System.out.println(">PACK:sourceId:"+sourceId);
        System.out.println(">PACK:payload:"+payload);
        System.out.println(">PACK:path:"+path);

        pathLength = path.length;

        dout.writeInt(0);
        dout.writeByte(-1);
        //size is len plus 5 ints * 4 to bytes
        dout.writeInt((pathLength + 5 ) * 4);
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

        int fourcount = 0;
        for (byte b : messageBytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("<PACK> RRTI --------");
                fourcount = 0;
            }
        }

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
        System.out.println("ONSD this is sending event thread");
        TCPSender out = new TCPSender(this.socket,messageBytes);
        System.out.println("ONSD this is sending event thread|1");
        Thread sendThread = new Thread(out);
        System.out.println("ONSD this is sending event thread|2");
        sendThread.start();
        System.out.println("ONSD this is sending event thread|3");
    }
}


