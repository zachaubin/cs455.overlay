package cs455.overlay.wireformats;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPSender;

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
    public String hostname;
    public int nodeId;
    public int port;
    byte[] messageBytes;
    public String regHost;

    public OverlayNodeSendsRegistration(){}

//    public OverlayNodeSendsRegistration(Socket socket, String hostname, int portNumber) throws IOException {
//        this.socket = socket;
//        byte[] msg = packBytes(2,hostname,portNumber);
//        //sendBytes opens data output stream on socket passed to us
//        if(!sendBytes(msg)){
//            System.err.println("-could not send bytes in registration-");
//        } else {
//            success = true;
//        }
//    }
    public OverlayNodeSendsRegistration(Socket socket, String regHost, int port){
        this.socket = socket;
        this.regHost = regHost;
        this.port = port;

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
    public byte[] packBytes(int type, String hostname, int portNumber) throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        System.out.println(">PACK:type:"+type);
        System.out.println(">PACK:hostname:"+hostname);
        System.out.println(">PACK:portNumber:"+portNumber);


        //type, host length, hostname, nodeId
        int hostlen = hostname.length();
        byte[] hostbytes = hostname.getBytes();

        dout.writeInt(-1);
        dout.writeInt(type);
        dout.writeInt(hostlen);
        dout.write(hostbytes);
        dout.writeInt(portNumber);


        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        messageBytes = marshalledBytes;

        int fourcount = 0;
        for (byte b : messageBytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("--------");
                fourcount = 0;
            }
        }

        baOutputStream.close();
        dout.close();

        return marshalledBytes;
    }
    //unpacks primitives from byte[]
    public void unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        System.out.println("==unpackbytes==");
        int fourcount = 0;
        for (byte b : pack) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("--------");
                fourcount = 0;
            }
        }

        //get to and eat message header
        while(din.readByte() != -1);
        din.readByte();din.readByte();din.readByte();

        type = din.readInt();
        System.out.println("UNPACK:type:"+type);

        hostLength = din.readInt();
        System.out.println("UNPACK:hostlen:"+hostLength);
        byte[] hostbytes = new byte[hostLength];
        din.readFully(hostbytes, 0, hostLength);
        hostname = new String(hostbytes);
        System.out.println("UNPACK:hostname:"+hostname);
        port =  din.readInt();
        System.out.println("UNPACK:port:"+port);

        baInputStream.close();
        din.close();
    }
    @Override
    public void run() {
        //this sends the packed bytes from messageBytes
        //we should have called packbytes by now

        System.out.println("this is sending event thread");
        TCPSender out = new TCPSender(this.socket,messageBytes);
        Thread sendThread = new Thread(out);
        sendThread.run();

    }


}
