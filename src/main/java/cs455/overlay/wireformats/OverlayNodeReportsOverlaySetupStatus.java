package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPSender;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class OverlayNodeReportsOverlaySetupStatus extends Event implements Runnable {

    public MessagingNode node;

    public OverlayNodeReportsOverlaySetupStatus(MessagingNode node){
        this.node = node;
    }

    public byte[] packBytes() throws IOException {
        byte[] msg = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(7);//type
        dout.writeInt(node.nodeId);

        dout.flush();
//        marshalledBytes = baOutputStream.toByteArray();
//        messageBytes = marshalledBytes;

        msg = baOutputStream.toByteArray();


        baOutputStream.close();
        dout.close();

        return msg;


    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(node.registryHostname,node.portNumber);
            TCPSender tcpSender = new TCPSender(socket,packBytes());
            Thread tcpSenderThread = new Thread(tcpSender);
            tcpSenderThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
