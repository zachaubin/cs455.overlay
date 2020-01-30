package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender extends TCPConnection {

    private Socket socket;
    private DataOutputStream dout;

    public TCPSender(Socket socket) throws IOException {
        this.socket = socket;
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

    @Override
    public void run() {

    }
}
