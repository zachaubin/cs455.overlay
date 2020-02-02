package cs455.overlay.transport;

import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPRegistryServerThread implements Runnable {

    int port;
    public static Socket listen;
    public Registry registry;

    public TCPRegistryServerThread(Socket socket, Registry registry,int port) {
        this.listen = socket;
        this.registry = registry;
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("listening on port[" + port + "]");

            while(true) {

                //wait for connect
                listen = serverSocket.accept();
                //got incoming connection

                System.out.println("receiver thread start");
                //receiver thread start
                //pass socket to read from
                TCPConnection rec = new TCPConnection(listen,0);
                //add to cache ;
//                registry.connections.addConnection((rec));
                Thread receive =  new Thread(rec);
                receive.start();
                //writes to socket stream

//              register;
                //read byte[] from listen.socket
                OutputStream bos = listen.getOutputStream();
                InputStream is = listen.getInputStream();


                byte[] bytes_headgarbage = is.readAllBytes();
                System.out.println("\t\t\tbyte in size::"+bytes_headgarbage.length);

                // Get the slice of the Array
                byte[] bytes = new byte[bytes_headgarbage.length - 4];

                // Copy elements of arr to slice
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = bytes_headgarbage[4 + i];
                }

                System.out.println("==READALLBYTESINPUTSTREAMINRSTHREAD==");
                int fourcount = 0;
                for (byte b : bytes) {
                    System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
                    fourcount++;
                    if(fourcount == 4) {
                        System.out.println("--------");
                        fourcount = 0;
                    }
                }

                OverlayNodeSendsRegistration marshall = new OverlayNodeSendsRegistration();
                marshall.unpackBytes(bytes);

                System.out.println("marshalled hostname: ");
                System.out.println("marshalled port: ");

                // host port
                int nodeId = registry.nodes.addRoutingEntry(marshall.hostname,marshall.port);
                System.out.println("registered node with id="+nodeId);
                registry.nodes.printTable();

            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + port + " or listening for a connection: ");
            System.out.println(e.getMessage());
        }
    }
    public Socket getSocket(){
        return listen;
    }
}
