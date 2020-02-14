package cs455.overlay.wireformats;

import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.ServerSocket;
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

        System.out.println(">PACK:type:"+type);
        System.out.println(">PACK:numMsgs:"+numMsgs);

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);

        dout.writeInt(numMsgs);

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

        System.out.println("RegistryRequestsTaskInitiate:: ==unpackbytes==");
        int fourcount = 0;
        for (byte b : pack) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("<UNPACK> RRTI --------");
                fourcount = 0;
            }
        }

        type = din.readInt();
        System.out.println("UNPACK:type:"+type);

        numMsgs = din.readInt();
        System.out.println("UNPACK:numMsgs:"+numMsgs);

        baInputStream.close();
        din.close();
    }
    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now

        System.out.println("RRTI this is sending event thread");
        TCPSender out = new TCPSender(this.socket,messageBytes);
        System.out.println("this is sending event thread|1");
        Thread sendThread = new Thread(out);
        System.out.println("this is sending event thread|2");

        sendThread.start();
        System.out.println("this is sending event thread|3");
//        try {
//            this.socket.close();
//        } catch (IOException e) {
//            System.out.println("socket close error in ONSR run()");
//            e.printStackTrace();
//        }


    }
}
