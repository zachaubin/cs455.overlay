package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPRegistryServerThread;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

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

    byte[] byteMessageLength;
    byte byteMessageType;

    byte ipAddressLength;
    byte[] ipAddress;

    int portNumber;
    byte[] bytePortNumber;

    String hostname;
    byte[] byteHostName;
    byte[] byteHostNameLength;

    InetAddress ip;
    int nodeId;

    Socket nodeSocket;
    public int nodePort = -2;

//    private class Registry(){
    String registryHostname;

    public int msgsSent = 0;
    public int msgsReceived = 0;
    public int msgsRelayed = 0;

    public ArrayList<RoutingEntry> myRoutes;
    public RoutingTable table;

    public RoutingTable routes;
    public ArrayList<Integer> idList;





    MessagingNode(String host, int port) {
        registryHostname = host;
        portNumber  = port;
    }

    private int newNodeId(){
        Random rand = new Random();
        int max = 255;
        int min = 0;
        return rand.nextInt((max - min) + 1) + min;
    }

    private void buildMyRoutes(RoutingTable t){
        //from manifest-received-table t,
        int myIndex = t.getIndexOfNodeId(nodeId);
        //  find=THISNODEID as index0

        //  calculate i= +1,+2,+4.. such that i<n
        //   add each i to myRoutes

        for(int distance = 1; distance < t.table.size();  distance *= 2){
            myRoutes.add(t.table.get( distance + myIndex & t.table.size()));
        }

        System.out.println(" >> built routing table");
    }


    public static void main(String[] args) throws IOException {

        //new node opens server on (reg host,reg port)
        System.out.println("starting a node");
        String hostname_REGISTRY = args[0];
        int port_REGISTRY = Integer.parseInt(args[1]);
        MessagingNode currentNode = new MessagingNode(hostname_REGISTRY, port_REGISTRY);

        //NODE SERVER START
//        currentNode.nodeSocket= new Socket("localhost",0);
//        int port_NODE_SERVER = currentNode.nodeSocket.getPort();
        int port_NODE_SERVER = -1;
        System.out.println("NODE server thread runs on loop to tcp receive and print based on msg");
        TCPServerThread server_thread = new TCPServerThread(currentNode.nodeSocket,currentNode);
        System.out.println("1 OF {NODE SERVER START} BLOCK");

        System.out.println("2 OF {NODE SERVER START} BLOCK");
        Thread serverThread = new Thread(server_thread);
        System.out.println("3 OF {NODE SERVER START} BLOCK");
        serverThread.start();
        while(currentNode.nodePort < 0) {
            System.out.println("port is -2");
            port_NODE_SERVER = currentNode.nodePort;
        }
        System.out.println("after while port node server is " + port_NODE_SERVER);
        System.out.println("END OF {NODE SERVER START} BLOCK");


        //// register ////

        //socket for registration
        Socket regSock = new Socket(hostname_REGISTRY,port_REGISTRY);
        TCPConnection registerMe = new TCPConnection(regSock,1);

        //pack info
        OverlayNodeSendsRegistration regEvent = new OverlayNodeSendsRegistration(  regSock, hostname_REGISTRY , port_REGISTRY  );
        //external not needed, stored in marshallbytes in overlayevent
        System.out.println("about to PACK");
        byte[] bytes = regEvent.packBytes(2,InetAddress.getLocalHost().getHostName(),port_NODE_SERVER);
//        currentNode.connections.addConnection(registerMe);

        System.out.println("   >registering on port["+port_NODE_SERVER+"]");
        //thread for registration USED WITH EVENT
        Thread registration = new Thread(regEvent);
        registration.start();
        //we are registered


        System.out.println("made to end of main in MSGNODE");


//        currentNode.registerNode();
    }




}
