package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RegistryReportsDeregistrationStatus extends Event {

    public void unpackBytes(byte[] pack) throws IOException {
//        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
//        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
//
//        System.out.println("OverlayNodeSendsRegistration:: ==unpackbytes==");
//        int fourcount = 0;
//        for (byte b : pack) {
//            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
//            fourcount++;
//            if(fourcount == 4) {
//                System.out.println("--------");
//                fourcount = 0;
//            }
//        }
//
//        //get to and eat message header
//        while(din.readByte() != -1);
//        din.readByte();din.readByte();din.readByte();
//
//        type = din.readInt();
//        System.out.println("UNPACK:type:"+type);
//
//        hostLength = din.readInt();
//        System.out.println("UNPACK:hostlen:"+hostLength);
//        byte[] hostbytes = new byte[hostLength];
//        din.readFully(hostbytes, 0, hostLength);
//        hostname = new String(hostbytes);
//        System.out.println("UNPACK:hostname:"+hostname);
//        port =  din.readInt();
//        System.out.println("UNPACK:port:"+port);
//
//        baInputStream.close();
//        din.close();
    }
}
