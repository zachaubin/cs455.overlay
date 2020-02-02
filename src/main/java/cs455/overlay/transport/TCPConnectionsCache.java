package cs455.overlay.transport;

import java.util.ArrayList;

public class TCPConnectionsCache {
    public ArrayList<TCPConnection> connections;

    public void addConnection(TCPConnection t){
        connections.add(t);
    }
    public void removeConnection(TCPConnection t){
        connections.remove(t);
    }

}
