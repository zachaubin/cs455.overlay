package cs455.overlay.wireformats;

import cs455.overlay.node.Registry;
import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.ServerSocket;
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

    /*
        byte: Message type (REGISTRY_REPORTS_REGISTRATION_STATUS) == 3
        int: Success status; Assigned ID if successful, -1 in case of a failure
        byte: Length of following "Information string" field
        byte[^^]: Information string; ASCII charset
     */

    //packs primitives into a byte[]
    public byte[] packBytes( int type, int success, int numberOfNodes, int nodeId ) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

//        System.out.println(">PACKreport:type:"+type);
//        System.out.println(">PACKreport:success:"+success);
//        System.out.println(">PACKreport:numNodes:"+numberOfNodes);

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

//        printBytes(messageBytes);

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }
    //unpacks primitives from byte[]
    public void unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

//        System.out.println("==unpackbytes==");
//        int fourcount = 0;
//        for (byte b : pack) {
//            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
//            fourcount++;
//            if(fourcount == 4) {
//                System.out.println("--------");
//                fourcount = 0;
//            }
//        }

        //get to and eat message header
        while(din.readByte() != -1);

        type = din.readInt();
//        System.out.println("UNPACK:type:"+type);

        int sizeofint = din.readInt();//infolen is just an int with sizeof int
//        System.out.println("UNPACK:infolen:"+sizeofint);
        this.numberOfNodes = din.readInt();
        this.nodeId = din.readInt();

//        System.out.println("UNPACK:number of nodes:"+this.numberOfNodes);
//        System.out.println("UNPACK: my nodeId:"+this.nodeId);

        success = 1;


        baInputStream.close();
        din.close();
    }

    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now

//        System.out.println("RegistryReportsRegistrationStatus: this is sending event thread to report status|0");

        while(this.socket == null){
            //lookup host and port from cache to make new socket
            System.out.println("RegRepRegStatus: socket error");
        }
        TCPSender out = new TCPSender(this.socket,messageBytes);
//        System.out.println("RegistryReportsRegistrationStatus: this is sending event thread to report status|1");

        Thread sendThread = new Thread(out);
//        System.out.println("RegistryReportsRegistrationStatus: this is sending event thread to report status|2");

        sendThread.start();
//        System.out.println("RegistryReportsRegistrationStatus: this is sending event thread to report status|3");


    }
}