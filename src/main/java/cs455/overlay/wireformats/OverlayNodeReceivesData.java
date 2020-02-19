package cs455.overlay.wireformats;

import cs455.overlay.node.MessagingNode;

import java.io.*;

public class OverlayNodeReceivesData extends Event {
    public int type;
    public int destinationIdIndex;
    public int destinationId;
    public int sourceIdIndex;
    public int sourceId;
    public int payload;
    public int nodeId;
    public int pathLength;
    public int[] path;

    public MessagingNode node;

    public byte[] messageBytes;

    public OverlayNodeReceivesData(MessagingNode node){
        this.node = node;
    }

    public void unpackBytes(byte[] pack) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(pack);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        while(din.readByte() != -1);
        this.type = din.readInt();
        destinationId = din.readInt();
        int index = 0;
        for(int id : node.idList){
            if(id == destinationId) break;
            index++;
        }
        destinationIdIndex = index;
        sourceId = din.readInt();
        index = 0;
        for(int id : node.idList){
            if(id == sourceId) break;
            index++;
        }
        sourceIdIndex = index;
        this.payload = din.readInt();
        this.pathLength = din.readInt();

        path = new int[pathLength+1];

        for(int i = 0; i < pathLength; i++){
            path[i] = din.readInt();
        }

        baInputStream.close();
        din.close();
    }
}
