package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;

public class RegistrySendsNodeManifest extends Event {

    //sends routing table to node x
    public Socket socket;
    public RoutingTable table;
    public MessagingNode node;
    public byte[] messageBytes;

    int type;
    public RoutingTable receivedTable;


    RegistrySendsNodeManifest(){};

    public RegistrySendsNodeManifest(Socket socket, RoutingTable table, MessagingNode node){
        this.socket = socket;
        this.table = table;
        this.node = node;
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
    public byte[] packBytes( int type, RoutingTable table ) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int entries = table.getNumberOfNodes();

        dout.writeInt(-1);
        dout.writeInt(type);

        //for entry in table,
        // //send: int=-1,str=ent1,int=-1,str=ent2,int=-1,str=ent3
        if(!table.table.isEmpty()) {
            for (RoutingEntry e : table.table) {
                dout.writeInt(e.nodeHost.getBytes().length);
                dout.write(e.nodeHost.getBytes());
                dout.write(e.nodePort);
                dout.writeInt(e.nodeId);
            }
            dout.writeInt(-1);
        }


        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

        printBytes(messageBytes);

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }
    //unpacks primitives from byte[]
    public RoutingTable unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        System.out.println("==unpackbytes==");
        printBytes(pack);

        //get to and eat message header
        while(din.readByte() != -1);
        din.readByte();din.readByte();din.readByte();

        type = din.readInt();
        System.out.println("UNPACK:type:"+type);

        int hostLength;
        byte[] hostbytes;
        String host;
        int port;
        int id;

        byte dinTrack = 0;

        //first entry
        while(din.available() > 3) {

            hostLength = din.readInt();
            hostbytes = new byte[hostLength];
            din.readFully(hostbytes, 0, hostLength);
            host = new String(hostbytes);
            port = din.readInt();
            id = din.readInt();
            receivedTable.buildEntry(host, port, id);

        }
        //tail, might not need
        while(din.readByte() != -1);
        din.readByte();din.readByte();din.readByte();


        baInputStream.close();
        din.close();
    }

    public byte[] packEntry(RoutingEntry e) throws IOException {
        byte[] packed = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(e.nodeHost.getBytes().length);
        dout.write(e.nodeHost.getBytes());
        dout.write(e.nodePort);
        dout.writeInt(e.nodeId);
    }

    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now
        System.out.println("RegistrySendsNodeManifest: before look for entries");

        for(RoutingEntry e : table.table){
            System.out.println("RegistrySendsNodeManifest: starting entry send");

            //should get socket from cache
            Socket tempSock = null;
            TCPSender tcpOut = null;
            try {
                System.out.println("RegistrySendsNodeManifest: establish socket");
                tempSock = new Socket(e.nodeHost,e.nodePort);
            } catch (IOException ex) {
                System.err.println("error making socket connection to node in routing table");
                ex.printStackTrace();
            }
            System.out.println("RegistrySendsNodeManifest: establish tcp sender");

            tcpOut = new TCPSender(tempSock,messageBytes);//SENDS WHOLE TABLE TO NODE IN TABLE
            Thread sendThread = new Thread(tcpOut);
            System.out.println("RegistrySendsNodeManifest: start sender thread");

            sendThread.start();
        }
        System.out.println("RegistrySendsNodeManifest: sent table to all entries");
        
    }

}
