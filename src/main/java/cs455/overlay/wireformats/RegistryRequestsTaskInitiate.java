package cs455.overlay.wireformats;

import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;

public class RegistryRequestsTaskInitiate extends Event {

    public byte[] messageBytes;
    public int type;
    public int numMsgs;
    public Socket socket;

    public RegistryRequestsTaskInitiate() { }

    //packs primitives into a byte[]
    public byte[] packBytes(int type, int numMsgs) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);

        dout.writeInt(numMsgs);

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

        type = din.readInt();
        numMsgs = din.readInt();

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
