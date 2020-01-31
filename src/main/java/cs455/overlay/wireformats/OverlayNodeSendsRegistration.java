package cs455.overlay.wireformats;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
/*
how to use this:
make new object with all required parameters
 */
public class OverlayNodeSendsRegistration extends Event {

    Socket socket;
    int type;
    int hostLength;
    String hostname;
    int nodeId;
    byte[] messageBytes;

    public OverlayNodeSendsRegistration(Socket socket, String hostname, int portNumber, int nodeId) throws IOException {
        this.socket = socket;
        byte[] msg = packBytes(2,hostname,portNumber,nodeId);
        //sendBytes opens data output stream on socket passed to us
        if(!sendBytes(msg)){
            System.err.println("-could not send bytes in registration-");
        } else {
            success = true;
        }
    }

//    public void registerNode(int type, String hostname, int nodeId)
    //better tu just call object

    public boolean sendBytes(byte[] bytes) throws IOException {

        try(DataOutputStream dout = new DataOutputStream(socket.getOutputStream());) {
            //write a byte array to data writer
//            dout.writeInt(bytes.length);
            dout.write(bytes);
            dout.flush();
//            dout.close();

        } catch(IOException e) {
            System.err.println("Couldn't get I/O for the connection to [" + hostname + "], exiting...");
            return false;
//            System.exit(1);
        }
        return true;
    }

    //packs primitives into a byte[]
    public byte[] packBytes(int type, String hostname, int portNumber, int nodeId) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));


        //type, host length, hostname, nodeId
        int hostlen = hostname.length();
        byte[] hostbytes = hostname.getBytes();

        dout.writeInt(type);
        dout.writeInt(hostlen);
        dout.write(hostbytes);
        dout.writeInt(portNumber);
        dout.writeInt(nodeId);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }
    //unpacks primitives from byte[]
    public void unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();

        hostLength = din.readInt();
        byte[] hostbytes = new byte[hostLength];
        din.readFully(hostbytes, 0, hostLength);
        hostname = new String(hostbytes);
        nodeId = din.readInt();

        baInputStream.close();
        din.close();
    }
}
