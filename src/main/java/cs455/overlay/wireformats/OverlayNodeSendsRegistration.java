package cs455.overlay.wireformats;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPSender;

import java.io.*;
import java.net.Socket;

public class OverlayNodeSendsRegistration extends Event {

    Socket socket;
    int type;
    int hostLength;
    public String hostname;
    public int port;
    byte[] messageBytes;
    public String regHost;

    public OverlayNodeSendsRegistration(){}

    public OverlayNodeSendsRegistration(Socket socket, String regHost, int port){
        this.socket = socket;
        this.regHost = regHost;
        this.port = port;
    }

    public boolean sendBytes(byte[] bytes) throws IOException {
        try(DataOutputStream dout = new DataOutputStream(socket.getOutputStream());) {
            dout.write(bytes);
            dout.flush();
        } catch(IOException e) {
            System.err.println("ONSR:sendBytes:Couldn't get I/O for the connection to [" + hostname + "], exiting...");
            return false;
        }
        return true;
    }

    //packs primitives into a byte[]
    public byte[] packBytes(int type, String hostname, int portNumber) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //type, host length, hostname, nodeId
        int hostlen = hostname.length();
        byte[] hostbytes = hostname.getBytes();

        dout.writeInt(0);
        dout.writeByte(-1);
        dout.writeInt(type);
        dout.writeInt(hostlen);
        dout.write(hostbytes);
        dout.writeInt(portNumber);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }
    //unpacks primitives from byte[]
    public void unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        //get to and eat message header
        while(din.readByte() != -1);

        type = din.readInt();
        hostLength = din.readInt();
        byte[] hostbytes = new byte[hostLength];
        din.readFully(hostbytes, 0, hostLength);
        hostname = new String(hostbytes);
        port =  din.readInt();

        baInputStream.close();
        din.close();
    }
    @Override
    public void run() {
        //this sends the packed bytes as messageBytes to socket
        TCPSender out = new TCPSender(this.socket,messageBytes);
        Thread sendThread = new Thread(out);
        sendThread.start();
    }


}
