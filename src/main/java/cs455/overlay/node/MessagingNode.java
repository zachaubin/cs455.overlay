package cs455.overlay.node;

import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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


public class MessagingNode extends Node implements Runnable {

    byte messageType;
    byte ipAddressLength;
    byte[] ipAddress;
    int portNumber;

    String hostname;
    InetAddress ip;
    int nodeId;

//    private class Registry(){
    String registryHostname;



    MessagingNode(String host, int port) {
        try {
            ip = InetAddress.getByName(host);
        } catch(UnknownHostException e){
            System.out.println("Unknown host exception: " + e);
        }

        hostname = host;
        portNumber  = port;
        nodeId = 0;//newNodeId();

    }

    private int newNodeId(){
        Random rand = new Random();
        int max = 1024;
        int min = 1;
        return rand.nextInt((max - min) + 1) + min;
    }

    public void registerNode(){
        Event sendReg = new Event();

        while(!sendReg.success){
            this.nodeId = newNodeId();
            new OverlayNodeSendsRegistration(this.hostname,this.portNumber,this.nodeId);
        }
        //nodeId is now finalized

    }

    //messageType , ipAddressLength , ipAddressSource , ipdestlen , ipAddressDest , checksum
    //1send 0rec  ,      get len    , get byte[]
    //0000 0000   ,    0000 0000    , 0000 ... 0000   , 0000 0000 , 0000 ... 0000
    private byte[] Message(byte send1rec0, String dest){
        byte[] messageRaw = {0};

        messageType = send1rec0;//register is 0b10101010

        //source IP
        ipAddress = ip.getAddress();
        ipAddressLength = (byte) ipAddress.length;

        //dest IP
//        InetAddress destIp = InetAddress.getByName(dest);
//        byte[] destIpAddress = InetAddress.get


        //port max is 16 bit
        byte[] port = ByteBuffer.allocate(16).putInt(portNumber).array();

        byte[] payload = ByteBuffer.allocate(16).putInt(0).array();
        byte checksum = 0;//this needs to be a real checksum later



        return messageRaw;

    }

    //destIpAddress, payload
    public void sendMessage(){
        int destinationIpAddress;

    }


    public void run() {
        System.out.println("Hello from a CLIENT thread!");
        Event sendReg = new OverlayNodeSendsRegistration(this.hostname,this.portNumber,this.nodeId);
    }

    public static void main(String[] args){

        //new node on (host,port)
        MessagingNode currentNode = new MessagingNode(args[0],Integer.parseInt(args[1]));
        currentNode.registerNode();




    }
}
