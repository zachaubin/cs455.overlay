package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class CommandInputRegistryThread implements Runnable {

    public Registry registry;
    public RegistrySendsNodeManifest rsnm;

    public CommandInputRegistryThread(Registry registry){

        this.registry = registry;
        this.rsnm = new RegistrySendsNodeManifest(registry.nodes);
    }


    private void command(String commands) throws IOException, InterruptedException {


        String command = commands;
        int numberArg = 2;


//        System.out.println("111111111111YOU ENTERED ["+commands+"]");
//
//
//        String delims = "[ ]+";
//        String[] part = commands.split(delims);
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>.1part.length:"+part.length);
//
//        System.out.println("222222222222222YOU ENTERED ["+commands+"]");
//
//        for(String thing : part){
//            System.out.println("args: "+thing);
//        }
//        String command = part[0];
//        int numberArg = 0;
//
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>2part.length:"+part.length);
//        if(part.length > 1){
//            numberArg = Integer.parseInt(part[1]);
//        }



        System.out.println("command function called");
        boolean good = false;
        if(  command.equalsIgnoreCase("list-messaging-nodes") ){
            System.out.println("list messaging nodes entered...\n");
            registry.nodes.printTable();
            good = true;
        }
        if(  command.equalsIgnoreCase("setup-overlay") ){
            System.out.println("setup-overlay sends manifest (routing table all entries) to all nodes in manifest\n");
            RegistrySendsNodeManifest rsnm = new RegistrySendsNodeManifest(registry.nodes);

            if(numberArg == 0 || numberArg > registry.nodes.getNumberOfNodes()){
                System.out.println("Cannot ask to set up route of size ["+numberArg+"] when we have ["+registry.nodes.getNumberOfNodes()+"] nodes.");
                good = true;
            } else {

                    System.out.println("CIRT DEBUG setup-overlay |0");
                sortTable();
                    System.out.println("CIRT DEBUG setup-overlay |PRINT TABLE SORTED");
                registry.nodes.printTable();
                    System.out.println("CIRT DEBUG setup-overlay |1");
                buildManifest();
                registry.nodes.printManifest();
                    System.out.println("CIRT DEBUG setup-overlay |2");
                sendManifest(registry.nodes);
                    System.out.println("CIRT DEBUG setup-overlay |3");
                buildRoutes(numberArg);
                printRoutes();
//                  System.out.println("buildRoutes built this:::::");

                    System.out.println("CIRT DEBUG setup-overlay |4");
                sendRoutes(numberArg);
                    System.out.println("CIRT DEBUG setup-overlay |5");

                good = true;

            }
        }
        if(  command.equalsIgnoreCase("list-routing-tables") ){
            System.out.println("list-routing-tables entered, printing routing tables...\n");

            int nid  = 0;
            for(ArrayList<RoutingEntry> ale : registry.nodes.routes) {
                System.out.println("======================================");
                System.out.println(">><< Begin routing table for nodeId: "+registry.nodes.manifest.get(nid));
                System.out.println("======================================");


                for(RoutingEntry e : ale){
                    registry.nodes.printEntry(e);
                }
                System.out.println("======================================");
                System.out.println("<<>> End routing table for nodeId: "+registry.nodes.manifest.get(nid));
                System.out.println("======================================");
                System.out.println("");
                nid++;

            }
            good = true;
        }

//        if(  command.equalsIgnoreCase("list-routing-tables") ){
//            System.out.println("list routing tables entered...\n");
//            good = true;
//
//        }
        if(!good){
            System.out.println("");
            System.out.println(":: :: :: :: :: :: :: :: :: :: :: :: :: :: :: ::");
            System.out.println("  !!!! Invalid command [[\""+command+"\"]] !!!!");
            System.out.println(":: :: :: :: :: :: :: :: :: :: :: :: :: :: :: ::");
            System.out.println("");


            System.out.println("  >> Valid commands are: <<");
            System.out.println("");
            System.out.println("list-messaging-nodes\n :: list all messaging nodes in routing table for each node");
            System.out.println("");
            System.out.println("setup-overlay [N]\n :: sets up overlay by sending routing table (size [N]) and manifest to each node in table");
            System.out.println("");
            System.out.println("list-routing-tables\n :: same as list-messaging nodes with note and more spacing");
            System.out.println("");
            System.out.println("start number-of-messages (e.g. start 25000)\n :: asks each node to send this many messages to random other nodes");
            System.out.println("");
            System.out.println("");

        }

    }

    private void sortTable(){
        registry.nodes.sortTable();
    }

    private void buildManifest(){
        registry.nodes.buildManifest();
    }

    private void buildRoutes(int n){
        registry.nodes.buildRoutes(n);
    }

    private void printRoutes(){
        for(ArrayList<RoutingEntry> ale : registry.nodes.routes){
            System.out.println("entry...");
            for(RoutingEntry e : ale) {
                registry.nodes.printEntry(e);
            }
        }
    }

    private void sendRoutes(int n) throws IOException, InterruptedException {
        for(RoutingEntry e : registry.nodes.table){
            System.out.println("sending route to nodeId:"+e.nodeId);
            //send to entry e, routing table size n
            sendOneRoute(e,n);
        }
    }
    private synchronized void sendOneRoute(RoutingEntry e,int n) throws IOException, InterruptedException {
        //pack bytes for routing entry's routes
        System.out.println("sendOneRoute |0");

        byte[] outgoingMsg;
        System.out.println("sendOneRoute |1");

        outgoingMsg = rsnm.packRoutesBytes(e,n);
        System.out.println("sendOneRoute |2");

        //socket new
        Socket outgoingSocket = new Socket(e.nodeHost,e.nodePort);
        System.out.println("sendOneRoute |3");

        //prep send packed bytes
        TCPSender sender = new TCPSender(outgoingSocket,outgoingMsg);
        System.out.println("sendOneRoute |4 , socket:"+outgoingSocket.isConnected());

        //thread send start
        Thread sendThread = new Thread(sender);
        sendThread.start();
        System.out.println("sendOneRoute |5 , socket:"+outgoingSocket.isConnected());

        sendThread.join();
        System.out.println("sendOneRoute |6 , socket:"+outgoingSocket.isConnected());

        //close connection
//        outgoingSocket.close();
    }

    private void sendManifest(RoutingTable t) throws IOException {

//        Socket response = new Socket(e.nodeHost,e.nodePort);
//        System.out.println("CommandRegistryThread.sendManifest: reporting status |0");
//        RegistrySendsNodeManifest reportReg = new RegistrySendsNodeManifest(response, registry.nodes);
//
//        System.out.println("CommandRegistryThread.sendManifest: reporting status |1");
//        reportReg.packBytes(6,registry.nodes);
//
//        Thread report = new Thread(reportReg);
//        System.out.println("CommandRegistryThread.sendManifest: reporting status |2");
//
//        report.start();
//        System.out.println("CommandRegistryThread.sendManifest: reporting status |3");

    }




    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        String typed;
        while(input.hasNextLine()){
            typed = input.next();
            System.out.println("waiting for command");
            try {
                command(typed);
            } catch (IOException | InterruptedException e) {
                System.out.println("!!! CIRT Registry command IO error?");
                e.printStackTrace();
            }
        }
    }
}
