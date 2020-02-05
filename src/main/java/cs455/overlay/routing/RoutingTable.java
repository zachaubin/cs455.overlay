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

        int nodeId = newNodeId();

        //if id already in table return false
        //size is 0 ... n
        for (int i = 1; i <= table.size(); i++) {
            if (nodeId == table.get(i).nodeId) {
                nodeId = newNodeId();
                i=0;
            }
        }

            RoutingEntry entry = new RoutingEntry(host, port,nodeId);
            table.add(entry);

        //success
        return nodeId;

    }
    public void buildEntry(String host, int port, int id){
        RoutingEntry entry = new RoutingEntry(host,port,id);
        table.add(entry);
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

    public void printTableSpaced(){
        System.out.println("Printing routing table, hostname always used over IP and is listed here:");
        if(table.isEmpty()){
            System.out.println("?table is empty?");
            return;
        }
        for(RoutingEntry e : table){
            System.out.println("");
            System.out.println("");
            System.out.println("");

            System.out.println("id["+e.nodeId+"]");
            System.out.println("host["+e.nodeHost+"]");
            System.out.println("port["+e.nodePort+"]");

            System.out.println("");

        }
    }

    public int getNumberOfNodes(){
        int count = 0;
        for(RoutingEntry e : table){
            count++;
        }
        return count;
    }

}