package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;

import java.io.IOException;
import java.net.Socket;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Scanner;


/*
There is exactly one registry in the system. The registry provides the following functions:
A. Allows messaging nodes to register themselves. This is performed when a messaging node starts
up for the first time.
B. Assign random identifiers (between 0-127) to nodes within the system; the registry also has to
ensure that two nodes are not assigned the same IDs i.e., there should be no collisions in the
ID space.
C. Allows messaging nodes to deregister themselves. This is performed when a messaging node
leaves the overlay.
D. Enables the construction of the overlay by populating the routing table at the messaging nodes.
The routing table dictates the connections that a messaging node initiates with other messaging
nodes in the system.
 */

/*
The basic flow of logic in such a server is this:

while (true) {
    accept a connection;
    create a thread to deal with the client;
}
>from oracle tutorial
 */

public class Registry extends Node {


    public int port;
    public Socket socket;

    public RoutingTable nodes;

    public TCPConnection tcp;

    public TCPConnectionsCache tcpCache;
//    public ArrayList<TCPReceiver> tcpReceiverCache;
//    public ArrayList<TCPSender> tcpSenderCache;


    //when started, it listens on a given port
    Registry(int port) {
        this.port = port;
        tcpCache = new TCPConnectionsCache();
    }

    private void command(String command){
        //list-messaging-nodes
        if(command.equalsIgnoreCase("list-messaging-nodes")){
            nodes.printTable();
        }
    }

    public int getNumberOfNodes(){
        return nodes.getNumberOfNodes();
    }
    public static <TCPRegistryServerThreadServerThread> void main(String[] args) throws IOException {

        System.out.println("Create registry on CLI specified port number, this will listen");

        //new registry
        //create registry on CLI specified port number, this will listen
        Registry registry = new Registry(Integer.parseInt(args[0]));
        System.out.println("port:"+registry.port);
        registry.nodes = new RoutingTable();
        registry.socket = null;

        //listen server
        //make tcp , add to cache , and registry server thread
//        registry.tcp = new TCPConnection(registry.socket,0);
//        registry.connections.addConnection((registry.tcp));
//        Thread tcpThread = new Thread(registry.tcp);
//        tcpThread.start();

        System.out.println("server thread runs on loop to tcp receive and register nodes");
        TCPRegistryServerThread serverthreadsocket = new TCPRegistryServerThread(registry.socket,registry,registry.port);
        Thread serverThread = new Thread(serverthreadsocket);
        serverThread.start();

//        RegistryReportsRegistrationStatus reportReg = new RegistryReportsRegistrationStatus(registry.socket, registry);

        //command controller
        CommandInputRegistryThread commands = new CommandInputRegistryThread(registry);
        Thread commandsThread = new Thread(commands);
        commandsThread.start();



    }


}


