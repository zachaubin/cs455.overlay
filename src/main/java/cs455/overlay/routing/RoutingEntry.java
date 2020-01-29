package cs455.overlay.routing;

public class RoutingEntry extends RoutingTable {

    public String nodeHost;
    public int nodePort;
    public int nodeId;

    public RoutingEntry(String host, int port, int id){
        nodeHost = host;
        nodePort = port;
        nodeId = id;
    }

}
