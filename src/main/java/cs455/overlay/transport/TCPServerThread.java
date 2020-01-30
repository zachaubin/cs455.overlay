package cs455.overlay.transport;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServerThread implements Runnable {

    int portNumber;

    public TCPServerThread(int port) {
        portNumber = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("listening on port[" + portNumber + "]");
            while(true) {
                new TCPReceiver(serverSocket.accept());
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection: ");
            System.out.println(e.getMessage());
        }
    }
}
