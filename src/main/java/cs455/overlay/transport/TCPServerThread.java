package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerThread implements Runnable {

    int portNumber;
    public static Socket listen;

    public TCPServerThread(int port) {
        portNumber = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
//            listen = new Socket(portNumber);
            System.out.println("listening on port[" + portNumber + "]");
            while(true) {
                listen = serverSocket.accept();
//                listen.accept();
//                yield listen;
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection: ");
            System.out.println(e.getMessage());
        }
    }
}
