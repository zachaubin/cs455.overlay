package cs455.overlay.wireformats;

import java.io.*;

public class OverlayNodeReceivesData extends Event {
    public int type;
    public int[] idList;
    public int destinationIdIndex;
    public int sourceIdIndex;
    public int payload;
    public int nodeId;
    public int pathLength;
    public int[] path;

    public byte[] messageBytes;

    public void unpackBytes(byte[] pack) throws IOException {
        //we might need to pull in packbytes to this method
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        System.out.println("==unpackbytes==");
        int fourcount = 0;
        for (byte b : pack) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;
            if(fourcount == 4) {
                System.out.println("ONRD--------ONRD");
                fourcount = 0;
            }
        }

        while(din.readByte() != -1);

        payload = din.read();
        //size is len plus 5 ints * 4 to bytes

        int msgLength = din.readInt();
        this.type = din.readInt();
        this.idList[destinationIdIndex] = din.readInt();
        this.idList[sourceIdIndex] = din.readInt();
        this.payload = din.readInt();
        this.pathLength = din.readInt();

        path = new int[pathLength+1];

        for(int i = 0; i < pathLength; i++){
            path[i] = din.readInt();
        }


//        this.nodeId = din.readInt();

//        for(int id : path) { //saved for relay code
//            dout.writeInt(id);
//        }

        System.out.println("<>>unPACK:type:"+9);
        System.out.println("<>>unPACK:destinationId:"+idList[destinationIdIndex]);
        System.out.println("<>>unPACK:sourceId:"+idList[sourceIdIndex]);
        System.out.println("<>>unPACK:payload:"+payload);
        System.out.println("<>>unpackPACK:path:"+path);

        baInputStream.close();
        din.close();
    }
}
