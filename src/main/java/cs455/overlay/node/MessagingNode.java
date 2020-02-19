package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeReportsTaskFinished;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//Upon starting up, each messaging node should register its IP address, and port number with the registry.
/*
It should be possible to register messaging nodes that are running on the same host but are listening
to communications on different ports. There should be 4 fields in this registration request:

byte: Message Type (OVERLAY_NODE_SENDS_REGISTRATION)
byte: length of following "IP address" field
byte[^^]: IP address; from InetAddress.getAddress()
int: Port number

 */

/*
Unlike the registry, there are multiple messaging nodes (minimum of 10) in the system. A messaging
node provides two closely related functions: it

1. initiates
and
2. accepts
both communications and messages within the system.

Communications that nodes have with each other are based on TCP. Each messaging node needs to
automatically configure the port over which it listens for communications i.e. the server-socket port
numbers should not be hard-coded or specified at the command line. TCPServerSocket is used to accept
incoming TCP communications.
Once the initialization is complete, the node should send a registration request to the registry.
Each node in the system has a routing table that is used to route content along to the sink. This routing
table contains information about a small subset of nodes in the system. Nodes should use this routing
table to forward packets to the sink specified in the message. Every node makes local decisions based
on its routing table to get the packets closer to the sink. Care must be taken to ensure you don’t change
directions or overshoot the sink: in such a case, packets may continually traverse the overlay.
 */


public class MessagingNode extends Node {

    private static MessagingNode instance = null;
    public static MessagingNode getInstance(String host, int port) {
        if(instance == null) {
            instance = new MessagingNode(host, port);
        }
        return instance;
    }


    public int portNumber;
    public int nodeId;

    Socket nodeSocket;
    public int nodePort = -2;

//    private class Registry(){
    public String registryHostname;

    public ArrayList<RoutingEntry> myRoutes;
    public RoutingTable table;

    public RoutingTable routes;
    public int[] idList;

    public int numMsgsToSend;
    public int pathLength;
    public byte[] messageBytes;

    public volatile AtomicLong countSent;
    public volatile AtomicLong countReceived;
    public volatile AtomicLong countRelayed;
    public volatile AtomicLong sentSum;

    public volatile AtomicLong sum;


    public volatile ArrayList<Integer> routeArrayList;
    public volatile Socket[] routeLookupSocket;

    public volatile TCPConnectionsCache cache = new TCPConnectionsCache();
    public volatile Thread cacheThread = null;

    private MessagingNode(){}

    private MessagingNode(String host, int port) {
        registryHostname = host;
        portNumber  = port;
        routeArrayList = new ArrayList<Integer>();
        routeLookupSocket = new Socket[128];
        countSent = new AtomicLong(0);
        countReceived = new AtomicLong(0);
        countRelayed = new AtomicLong(0);
        sum = new AtomicLong(0);
        sentSum = new AtomicLong(0);

    }

    public RoutingEntry socketFinder(int id){
        for(RoutingEntry e : routes.table){
            if(e.nodeId == id){
                return e;
            }
        }
        System.out.println("MessagingNode:socketFinder:error, routing entry not found for id="+id);
        return null;
    }

    private void buildMyRoutes(RoutingTable t){
        //from manifest-received-table t,
        int myIndex = t.getIndexOfNodeId(nodeId);
        //  find = THISNODEID as index0
        //  calculate i= +1,+2,+4.. such that i<n
        //   add each i to myRoutes
        for(int distance = 1; distance < t.table.size();  distance *= 2){
            myRoutes.add(t.table.get( distance + myIndex & t.table.size()));
        }
    }

    public void send_some_messages() throws IOException, InterruptedException {
        Random random = new Random();
        int max = idList.length-1;
        int min = 0;
        int choose=random.nextInt((max - min) + 1) + min;

        int myIndex = 0;
        for(int i = 0; i < idList.length; i++){
            if(idList[i] == nodeId){
                break;
            }
            myIndex++;
        }
        int payload;

        for(int i = 0; i < numMsgsToSend; i++){
            sleep(69);// nice, this line of code solved concurrency issues
            synchronized (this) {
                while (choose == myIndex) {
                    choose = random.nextInt((max - min) + 1) + min;
                }
                payload = random.nextInt();// random int
                sentSum.addAndGet(payload);
                send_a_message(choose, myIndex, payload);
                choose = random.nextInt((max - min) + 1) + min;
            }

        }
        sleep(3000);
        sleep(numMsgsToSend * 2 );
        reportStatsComplete();
    }
    public void send_a_message(int destinationIdIndex, int sourceIdIndex, int payload) throws IOException {
        //we might need to pull in packbytes to this method
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        pathLength = 1;

        dout.writeInt(0);
        dout.write(-1);

        dout.writeInt(33);//type
        dout.writeInt(idList[destinationIdIndex]);
        dout.writeInt(idList[sourceIdIndex]);
        dout.writeInt(payload);//payload
        dout.writeInt(pathLength);

        dout.writeInt(nodeId);//path start here

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

        baOutputStream.close();
        dout.close();
        // nextDest
        AtomicInteger i = new AtomicInteger(0);
        int backstep = routeArrayList.get(i.get());
        int step = routeArrayList.get(i.get());
        while(step < idList[destinationIdIndex]) {

            backstep = routeArrayList.get(i.get());
            i.incrementAndGet();
            if(i.get() >= routeArrayList.size()){
                break;
            };// == really
            step = routeArrayList.get(i.get());
        }
        int nextDestination = backstep;

        //socket to next in hop
        TCPSender tcpSender = new TCPSender(new Socket(socketFinder(nextDestination).nodeHost, socketFinder(nextDestination).nodePort),messageBytes);
        Thread sendThread = new Thread(tcpSender);
        sendThread.start();
        this.countSent.incrementAndGet();
    }

    public void reportStatsComplete() throws IOException, InterruptedException {
        OverlayNodeReportsTaskFinished onrtf = new OverlayNodeReportsTaskFinished();
        byte[] msg = onrtf.packBytes(nodeId);
        Socket socket = new Socket(registryHostname,portNumber);
        TCPSender tcpSender = new TCPSender(socket,msg);
        Thread tcpSenderThread = new Thread(tcpSender);
        tcpSenderThread.start();
        tcpSenderThread.join();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        MessagingNodeSingleton.main(args);
    }
}
