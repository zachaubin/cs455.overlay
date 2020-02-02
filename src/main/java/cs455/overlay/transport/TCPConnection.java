package cs455.overlay.transport;

import cs455.overlay.routing.RoutingTable;

import java.io.IOException;
import java.net.Socket;

public class TCPConnection implements Runnable {
    Socket socket;
    Thread thread;
    int rec0send1;

    //add switch to say if send or rec
    public TCPConnection(){}
    public TCPConnection(Socket socket, int rec0send1) throws IOException {
        this.socket = socket;
        this.rec0send1 = rec0send1;
    }

    public Socket getSocket(){
        return socket;
    }
    public Thread getThread(){
        return thread;
    }

    @Override
    public void run() {
        if(rec0send1 == 0){
            Thread receiver = null;
            try {
                receiver = new Thread(new TCPReceiver(socket));
            } catch (IOException e) {
                System.out.println("error opening receiver thread.");
                e.printStackTrace();
            }
            receiver.run();
        }

        if(rec0send1 == 1){
            Thread sender = null;
            try {
                sender = new Thread(new TCPSender(socket));
            } catch (IOException e) {
                System.out.println("error opening sender thread.");
                e.printStackTrace();
            }
            sender.run();
        }

    }
}
