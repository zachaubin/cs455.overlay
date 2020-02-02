package cs455.overlay.routing;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

public class RoutingTable {

    public ArrayList<RoutingEntry> table;

    //position 0 should be registry

    public RoutingTable(){
        table = new ArrayList<>();
    }

    //this will generate node id
    public int addRoutingEntry(String host, int port){

        int nodeId;
        boolean conflict = false;
        while(true) {
            nodeId = newNodeId();
            //if id already in table return false
            for (int i = 0; i < table.size(); i++) {
                if (nodeId == table.get(i).nodeId) {
                    conflict = true;
                    break;
                }
            }
            if(!conflict){
                //is unique so make/add
                RoutingEntry entry = new RoutingEntry(host, port,nodeId);
                table.add(entry);
                break;
            }
        }

        //success
        return nodeId;

    }
    private int newNodeId(){
        Random rand = new Random();
        int max = 255;
        int min = 0;
        return rand.nextInt((max - min) + 1) + min;
    }

    public void printTable(){
        System.out.println("Printing table:");
        if(table.isEmpty()){
            System.out.println("?table is empty?");
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