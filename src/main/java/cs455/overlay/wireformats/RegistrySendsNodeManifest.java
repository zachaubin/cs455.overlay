package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class RegistrySendsNodeManifest extends Event {

    //sends routing table to node x
    public Socket socket;
    public RoutingTable table;
    public MessagingNode node;
    public byte[] messageBytes;


    int type;
    public RoutingTable receivedTable;
    public ArrayList<Integer> ids;


    public RegistrySendsNodeManifest(){

        this.receivedTable = new RoutingTable();
        this.ids = new ArrayList<>();
    };

    public RegistrySendsNodeManifest(Socket socket, RoutingTable table){
        this.socket = socket;
        this.table = table;
        this.receivedTable = new RoutingTable();
        this.ids = new ArrayList<>();

    }

    public RegistrySendsNodeManifest(RoutingTable table){
        this.table = table;
        this.receivedTable = new RoutingTable();
        this.ids = new ArrayList<>();

    }

    public void printBytes(byte[] bytes){
        int fourcount = 0;
        for (byte b : bytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("--------RSNM" );
                fourcount = 0;
            }
        }
    }

    //packs primitives into a byte[], table.routes each keyId has a RoutingEntry to add
    public byte[] packBytes( int type, RoutingTable table ) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        int entries = table.getNumberOfNodes();

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);

        //for entry in table,
        // //send: int=-1,str=ent1,int=-1,str=ent2,int=-1,str=ent3
        if(!table.table.isEmpty()) {
            for (RoutingEntry e : table.table) {
                dout.writeInt(e.nodeHost.getBytes().length);
                dout.write(e.nodeHost.getBytes());
                dout.writeInt(e.nodePort);
                dout.writeInt(e.nodeId);
            }
            dout.writeInt(-1);
        }

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
        System.out.println("UNPACK:type:"+type);

        int hostLength;
        byte[] hostbytes;
        String host;
        int port;
        int id;

        byte dinTrack = 0;
        //first entry
        while(din.available() > 4) {
            hostLength = din.readInt();
            System.out.println("hostlen: "+hostLength);
            hostbytes = new byte[hostLength];
            din.readFully(hostbytes, 0, hostLength);
            host = new String(hostbytes);
            System.out.println("host: "+host);
            port = din.readInt();
            System.out.println("port: "+port);
            id = din.readInt();
            System.out.println("id: "+id);
            receivedTable.buildEntry(host, port, id);
            System.out.println("??: ");
        }

        baInputStream.close();
        din.close();
    }

    public byte[] packEntry(RoutingEntry e) throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(e.nodeHost.getBytes().length);
        dout.write(e.nodeHost.getBytes());
        dout.write(e.nodePort);
        dout.writeInt(e.nodeId);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

        int fourcount = 0;
        for (byte b : messageBytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("--------");
                fourcount = 0;
            }
        }

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }

    public byte[] packRoutesBytes(RoutingEntry e, int n) throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(6);//ONSM sends manifest type

        dout.writeByte(n);//number of routes in this table

        ArrayList<RoutingEntry> myRoutes = e.routes;

        // ORDER PER EACH
        //nodeId int
        //hostlen byte
        //host byte[]
        //port int
        for(RoutingEntry routePoint : myRoutes){

            dout.writeInt(routePoint.nodeId);
            dout.writeInt(routePoint.nodeHost.getBytes().length);
            dout.write(routePoint.nodeHost.getBytes());
            dout.writeInt(routePoint.nodePort);

        }

        //separator
        dout.writeInt(-1);

        // second part :
        //numNodes
        dout.writeInt(table.getNumberOfNodes());
        //int[] nodeIds
        for(int id : table.manifest){
            dout.writeInt(id);
        }

        //true tail
        dout.writeInt(-1);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

        baOutputStream.close();
        dout.close();
        return marshalledBytes;

    }

    public void unpackRoutesBytes(byte[] pack) throws IOException {

        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        //get to and eat message header
        while(din.readByte() != -1);
        type = din.readInt();
        int hostLength;
        byte[] hostbytes;
        String host;
        int port;
        int id;

        int numNodes = din.readByte();//number of routes in this table

        //first entry
        while(din.available() > 4) {
            id = din.readInt();
            if(id == -1) break;

            hostLength = din.readInt();
            hostbytes = new byte[hostLength];
            din.readFully(hostbytes, 0, hostLength);
            host = new String(hostbytes);
            port = din.readInt();
            receivedTable.buildEntry(host, port, id);
        }

        //broke from while loop, next is number of nodes with ids
        int numIds = din.readInt();//for read fully, not using
        //now all ids
        int singleId = -1;

        while(din.available() > 4) {
            singleId = din.readInt();
            this.ids.add(singleId);
        }
        //tail eat, might not need
        while(din.readByte() != -1);
        din.readByte();din.readByte();din.readByte();

        baInputStream.close();
        din.close();
    }

    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now
        int eCount=0;

        for(RoutingEntry e : table.table){
            eCount++;
            sendToEntryThread s = new sendToEntryThread(e);
            Thread sending = new Thread(s);
            sending.start();
            try {
                sending.join();
            } catch (InterruptedException ex) {
                System.out.println("RSNM: run() JOIN ERR: error sending to entry?");
                ex.printStackTrace();
            }
        }
    }

    public class sendToEntryThread implements Runnable {

        public RoutingEntry e;

        public sendToEntryThread(RoutingEntry e){
            this.e = e;
        }

        @Override
        public void run() {
            synchronized (this) {
                TCPSender tcpOut = new TCPSender(socket, messageBytes);//SENDS WHOLE TABLE TO NODE IN TABLE
                Thread sendThread = new Thread(tcpOut);
                sendThread.start();

            }
        }
    }
}
