package cs455.overlay.wireformats;

import cs455.overlay.node.Registry;
import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;

public class RegistryReportsRegistrationStatus extends Event {

    public Socket socket;
    public Registry registry;
    byte[] messageBytes;

    public int type;
    public int success;
    byte infolen;
    public int numberOfNodes;
    public int nodeId;

    public RegistryReportsRegistrationStatus(){}

    public RegistryReportsRegistrationStatus(Socket socket, Registry registry){
        this.socket = socket;
        this.registry = registry;
    }

    public void printBytes(byte[] bytes){
        int fourcount = 0;
        for (byte b : bytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("--------");
                fourcount = 0;
            }
        }
    }

    //packs primitives into a byte[]
    public byte[] packBytes( int type, int success, int numberOfNodes, int nodeId ) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //type, host length, hostname, nodeId
        infolen = 1;//int is 4 bytes always

        dout.write(-1);
        dout.writeInt(type);
        dout.writeInt(infolen);
        dout.writeInt(numberOfNodes);
        dout.writeInt(nodeId);


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

        int sizeofint = din.readInt();//infolen is just an int with sizeof int
        this.numberOfNodes = din.readInt();
        this.nodeId = din.readInt();

        success = 1;

        baInputStream.close();
        din.close();
    }

    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now
        while(this.socket == null){
            //lookup host and port from cache to make new socket
            System.out.println("RegRepRegStatus: socket error");
        }
        TCPSender out = new TCPSender(this.socket,messageBytes);
        Thread sendThread = new Thread(out);
        sendThread.start();
    }
}