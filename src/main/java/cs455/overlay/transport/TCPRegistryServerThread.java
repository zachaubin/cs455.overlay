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

    public TCPRegistryServerThread(Socket socket, Registry registry) {
        this.listen = socket;
        this.registry = registry;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("listening on port[" + port + "]");

            while(true) {

                //wait for connect
                listen = serverSocket.accept();
                //got incoming connection

                //receiver thread start
                //pass socket to read from
                TCPConnection rec = new TCPConnection(listen,0);
                //add to cache ;
                registry.connections.addConnection((rec));
                Thread receive =  new Thread(rec);
                receive.start();
                //writes to socket stream

//              register;
                //read byte[] from listen.socket
                OutputStream bos = listen.getOutputStream();
                InputStream is = listen.getInputStream();

                byte[] bytes = is.readAllBytes();

                OverlayNodeSendsRegistration marshall = new OverlayNodeSendsRegistration();
                marshall.unpackBytes(bytes);

                // host port
                int nodeId = registry.nodes.addRoutingEntry(marshall.hostname,marshall.port);
                System.out.println("registered node with id="+nodeId);

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
