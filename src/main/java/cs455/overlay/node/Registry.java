package cs455.overlay.node;

import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistryRequestsTaskInitiate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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

public class Registry extends Node implements Runnable {

    public int portNumber;
    public Socket socket;

    public RoutingTable nodes;

    //when started, it listens on a given port
    Registry(int port) {
        portNumber = port;

    }

    public void run() {
//        Registry registry = new Registry(0);//0 looks for any free port
//        Event startRegistry = new RegistryRequestsTaskInitiate(registry.portNumber);
//
//        System.out.println("Reg made on port " + registry.portNumber);
//        //Event reportReg = new RegistryReportsRegistrationStatus(this.);
    }


    public static void main(String[] args){

        System.out.println("create registry on CLI specified port number, this will listen");

        //create registry on CLI specified port number, this will listen
        Registry registry = new Registry(Integer.parseInt(args[0]));
        registry.nodes = new RoutingTable();
//        Thread server = new Thread(new TCPServerThread(registry.portNumber)){
//            run();
//        };

        TCPServerThread server = new TCPServerThread(registry.portNumber);


        while(true){
            System.out.println("listening?");
            registry.socket = TCPServerThread.listen;
            registry.nodes.printTable();
        }


        // >>> overlay sends node registration, we receive........


        // and add to reg table if id available




        //Event reportReg = new RegistryReportsRegistrationStatus(registry.socket, registry.portNumber);


    }

}
