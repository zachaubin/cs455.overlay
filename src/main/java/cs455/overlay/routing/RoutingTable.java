package cs455.overlay.routing;

import java.net.InetAddress;
import java.util.ArrayList;

public class RoutingTable {

    public ArrayList<RoutingEntry> table;

    //position 0 should be registry

    public RoutingTable(){
        table = new ArrayList<>();
    }

    public boolean addRoutingEntry(String host, int port, int id){
        //if id already in table return false
        for(int i = 0; i<table.size(); i++){
            if(id == table.get(i).nodeId){
                return false;
            }
        }
        //is unique so make/add
        RoutingEntry entry = new RoutingEntry(host, port, id);
        table.add(entry);

        //success
        return true;

    }

    public void printTable(){
        if(table.isEmpty()){
            return;
        }
        for(RoutingEntry e : table){
            System.out.println("id["+e.nodeId+"]");
            System.out.println("host["+e.nodeHost+"]");
            System.out.println("port["+e.nodePort+"]");
            System.out.println("");
        }
    }

}