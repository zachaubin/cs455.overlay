package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender extends TCPConnection {

    private Socket socket;
    private DataOutputStream dout;
    private byte[] msg;

    public TCPSender(Socket socket) throws IOException {
        this.socket = socket;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
        } catch(IOException e){
            System.err.println("TCPSender IOException on obj init: " + e);
        }
    }

    public TCPSender(Socket socket, byte[] msg){
        this.socket = socket;
        this.msg = msg;
        try {
            dout = new DataOutputStream(socket.getOutputStream());
        } catch(IOException e){
            System.err.println("TCPSender IOException on obj init: " + e);
        }

    }

    public void sendData(byte[] dataToSend) throws IOException {
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }

    public void run(){
        try {
            System.out.println("TCPSender: data sending...");
            this.sendData(msg);
            System.out.println("TCPSender: data sent");
            dout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    public void run(byte[] b) throws IOException {
//        System.out.println("TCPSender: data sending...");
//        sendData(b);
//        System.out.println("TCPSender: data sent");
//    }
}
