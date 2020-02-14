package cs455.overlay.transport;

import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPRegistryServerThread implements Runnable {

    int port;
    public static Socket listen;
    public Registry registry;

    public TCPConnectionsCache cache;

    public TCPRegistryServerThread(Socket socket, Registry registry,int port) {
        this.listen = socket;
        this.registry = registry;
        this.port = port;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
//            System.out.println("REGISTRY is listening on port[" + port + "]");

            while(true) {
                //wait for connect
                listen = serverSocket.accept();
                //got incoming connection
                InputStream is = listen.getInputStream();
                byte[] bytes = toByteArray(is);

                OverlayNodeSendsRegistration marshall = new OverlayNodeSendsRegistration();
                marshall.unpackBytes(bytes);

                // host port
                int nodeId = registry.nodes.addRoutingEntry(marshall.hostname,marshall.port);
//                System.out.println("registered node with id="+nodeId);
                registry.nodes.printTable();

            }

        } catch (IOException e) {
            System.out.println("TCP RS T:run():Exception caught when trying to listen on port "
                    + port + " or listening for a connection: ");
            System.out.println(e.getMessage());
        }
    }
    public Socket getSocket(){
        return listen;
    }
}
