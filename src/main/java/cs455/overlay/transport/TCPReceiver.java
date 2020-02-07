package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPReceiver extends TCPConnection {

    private Socket socket;
    private DataInputStream din;

    public TCPReceiver(Socket socket) throws IOException {
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    public void run() {
        int dataLength;
        while (socket != null) {
            try {
                System.out.println("TCPReceiver: inside tcp receiver run|0");
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> :: "+din.available());
                while(din.available() == 0);
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< == "+din.available());
                dataLength = din.readInt();
                System.out.println("TCPReceiver: inside tcp receiver run|1");

                byte[] data = new byte[dataLength];
                System.out.println("TCPReceiver: inside tcp receiver run|2");

                din.readFully(data, 0, dataLength);
                System.out.println("TCPReceiver: inside tcp receiver run|3");

                socket = null;
                System.out.println("TCPReceiver: inside tcp receiver run|4");

            } catch (SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
                break;
            }
        }
        try {
            din.close();
        } catch (IOException e) {
            System.err.println("TCPReceiver. din close error");
            e.printStackTrace();
        }
    }
}