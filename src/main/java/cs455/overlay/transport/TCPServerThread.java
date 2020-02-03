package cs455.overlay.transport;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServerThread implements Runnable {

    int port;
    public static Socket listen;
    public MessagingNode node;

    int type;
    int regStatus;
    int numberOfNodesInRegistry;
    ServerSocket serverSocket;

    public TCPServerThread(Socket socket, MessagingNode node) throws IOException {
        this.listen = socket;
        this.node = node;
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
            ServerSocket serverSocket = new ServerSocket(0);
            this.port = serverSocket.getLocalPort();
            System.out.println("NODE is listening on port[" + port + "]");
            node.nodePort = port;


            while(true) {
                System.out.println("BEGINNING OF {NODE SERVER WHILE true ACCEPT} BLOCK");
                //wait for connect
                listen = serverSocket.accept();
                //got incoming connection

                System.out.println("receiver thread start FOR NODE");
                //receiver thread start
                //pass socket to read from
                TCPConnection nodeServer = new TCPConnection(listen,0);
                //add to cache ;
//                registry.connections.addConnection((rec));
                Thread receive =  new Thread(nodeServer);
                receive.start();
                //writes to socket stream

                //read byte[] from listen.socket
                InputStream is = listen.getInputStream();

                byte[] bytes = toByteArray(is);

                System.out.println("\t\t\t\t\tbyte in sizeMOD::" + bytes.length);

                System.out.println("== READ ALL BYTES INPUT STREAM IN node THREAD ==");
                int fourcount = 0;
                for (byte b : bytes) {
                    System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
                    fourcount++;
                    if(fourcount == 4) {
                        System.out.println("--------");
                        fourcount = 0;
                    }
                }

                RegistryReportsRegistrationStatus marshall = new RegistryReportsRegistrationStatus();
                marshall.unpackBytes(bytes);

                if(marshall.type == 3) {
                    System.out.println("Registration request successful. The number of messaging nodes currently constituting the overlay is <[" + marshall.numberOfNodes + "]>");
                }
                if(marshall.success == -1){
                    System.out.println("Registration somehow unsuccessful. This should not happen.");
                }

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
    public int getPort(){
        return port;
    }
}