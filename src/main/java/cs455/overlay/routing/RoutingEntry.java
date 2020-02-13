package cs455.overlay.routing;

import java.util.ArrayList;

public class RoutingEntry extends RoutingTable {

    public String nodeHost;
    public int nodePort;
    public int nodeId;
    public ArrayList<RoutingEntry> routes;

    public RoutingEntry(String host, int port, int id){
        nodeHost = host;
        nodePort = port;
        nodeId = id;
        routes = new ArrayList<>();
    }



}
