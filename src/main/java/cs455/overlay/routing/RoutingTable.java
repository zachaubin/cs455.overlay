package cs455.overlay.routing;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class RoutingTable {

    public volatile ArrayList<RoutingEntry> table;
    public AtomicLong nodesWorking;

    //    public Map<Integer,ArrayList<RoutingEntry>> routes;
    public volatile ArrayList<ArrayList<RoutingEntry>> routes;
    public volatile ArrayList<Integer> manifest;

    public int[] doneYet;

    public RoutingTable(){
        table = new ArrayList<>();
        manifest = new ArrayList<>();
        routes = new ArrayList<>(128);
        nodesWorking = new AtomicLong(0);
    }

    public void sortTable(){

        Collections.sort(table, new SortEntryById());

    }
    static class SortEntryById implements Comparator<RoutingEntry>
    {
        // Used for sorting in ascending order of nodeId
        public int compare(RoutingEntry a, RoutingEntry b)
        {
            return a.nodeId - b.nodeId;
        }
    }

    public void buildRoutes(int n){

        for(int i = 0; i < table.size(); i++) {

            ArrayList<RoutingEntry> myRoutes = new ArrayList<>();
            int distance = 1;
            for(int j = 0; j < n; j++) {
                if( (i+distance) % table.size() ==i) continue;
                //add entry just after this and then double distance
                myRoutes.add(table.get( (i+distance) % table.size() ));
                distance = distance * 2 % (table.size());
            }
            table.get(i).routes = myRoutes;
            routes.add(myRoutes);
        }
    }

    public void buildManifest(){
        for(RoutingEntry e : table){
            manifest.add(e.nodeId);
        }
    }

    public void printManifest(){
        System.out.println("#Printing Manifest:");
        for(int i : manifest){
            System.out.println("#id: " + i);
        }
    }

    //this will generate node id
    public int addRoutingEntry(String host, int port){
        int nodeId = newNodeId();

        //if id already in table return false
        //size is 0 ... n
        for (int i = 1; i < table.size(); i++) {
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
        int max = 127;
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

    public void printEntry(RoutingEntry e){
        System.out.println("");
        System.out.println("id["+e.nodeId+"]");
        System.out.println("host["+e.nodeHost+"]");
        System.out.println("port["+e.nodePort+"]");
        System.out.println("");
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

    public int getIndexOfNodeId(int id){
        int index = 0;
        for(RoutingEntry e : table){
            if(e.nodeId == id){
                break;
            }
            index++;
        }
        return index;
    }

    public byte[] packEntry(RoutingEntry e) throws IOException {
        byte[] packed;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //type, host length, hostname, nodeId
        int hostlen = e.nodeHost.length();
        byte[] hostbytes = e.nodeHost.getBytes();

        dout.writeInt(-1);
        dout.writeInt(hostlen);
        dout.write(hostbytes);
        dout.writeInt(e.nodePort);

        dout.flush();
        packed = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();

        return packed;

    }

    public void removeRoutingEntry(int id){
        for(RoutingEntry e : table){
            if(id == e.nodeId){
                table.remove(e);
                break;
            }
        }
    }
    public void establishDoneYetArray(){
        doneYet = new int[128];
        for(RoutingEntry e : table){
            doneYet[e.nodeId] = -1;
        }
        nodesWorking.set(getNumberOfNodes());
    }
    public void printDoneYet(){
        System.out.println("_Working_ still:");
        for(int i = 0; i< doneYet.length; i++){
            if(doneYet[i] == -1){
                System.out.println("! NodeId["+i+"]");
            }
        }
        System.out.println("Reported as finished:");
        for(int i = 0; i< doneYet.length; i++){
            if(doneYet[i] == 1){
                System.out.println(". NodeId["+i+"]");
            }
        }
    }

}