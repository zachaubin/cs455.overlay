package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class CommandInputRegistryThread implements Runnable {

    public Registry registry;
    public RegistrySendsNodeManifest rsnm;
    private boolean ready = false;

    public CommandInputRegistryThread(Registry registry){

        this.registry = registry;
        this.rsnm = new RegistrySendsNodeManifest(registry.nodes);
    }


    private void command(String commands) throws IOException, InterruptedException {

        String delims = "[ ]+";
        String[] line = commands.split(delims);

        String command = line[0];
        int numberArg = 0;

        if(line.length > 1){
            try {
                numberArg = Integer.parseInt(line[1]);
            } catch (NumberFormatException e){
                System.out.println("Enter an actual positive integer for x.\n");
                command = "not_a_number";
            }
        }

        boolean good = false;
        if(  command.equalsIgnoreCase("list-messaging-nodes") ){
            System.out.println("");
            System.out.println("list messaging nodes entered...\n");
            registry.nodes.printTable();
            good = true;
        }
        if(  command.equalsIgnoreCase("setup-overlay") ){
            System.out.println("");
            System.out.println("Setting up overlay...");
            System.out.println("");
            RegistrySendsNodeManifest rsnm = new RegistrySendsNodeManifest(registry.nodes);


            if(ready) {
               System.out.println("Overlay has already been set up, on this version you will need to restart all nodes for a new overlay.\n");
               good = true;
            } else if(registry.nodes.getNumberOfNodes() ==1 ){
                System.out.println("Only one node has registered. Nodes cannot send messages to themselves, please register more nodes.\n");
                good = true;
            }

            else if(numberArg <= 0 || numberArg > registry.nodes.getNumberOfNodes() || numberArg > Math.pow(2,registry.nodes.getNumberOfNodes() - 1) + 1){
                System.out.println("Cannot ask to set up route of size ["+numberArg+"] (default = 0) when we have ["+registry.nodes.getNumberOfNodes()+"] nodes.");
                System.out.println("Please setup-overlay with another value.\n");
                good = true;
            } else {
                sortTable();
                buildManifest();
                buildRoutes(numberArg);
                sendRoutes(numberArg);
                registry.nodes.establishDoneYetArray();//for printing doneYet while running

                ready = true;// we can now call start
                good = true;
            }
        }
        if(  command.equalsIgnoreCase("list-routing-tables") ){
            System.out.println("list-routing-tables entered, printing routing tables for each node...\n");

            int nid  = 0;
            for(ArrayList<RoutingEntry> ale : registry.nodes.routes) {
                System.out.println("=========================================");
                System.out.println(">><< Begin routing table for nodeId: "+registry.nodes.manifest.get(nid));
                System.out.println("=========================================");


                for(RoutingEntry e : ale){
                    registry.nodes.printEntry(e);
                }
                System.out.println("=========================================");
                System.out.println("<<>> End   routing table for nodeId: "+registry.nodes.manifest.get(nid));
                System.out.println("=========================================");
                System.out.println("");
                nid++;

            }
            good = true;
        }

        if(  command.equalsIgnoreCase("start" ) ){

            System.out.println("");

            if(!ready) {
                System.out.println("Please use 'setup-overlay x' before sending messages.\n");
                good = true;
            } else if(registry.running){
                System.out.println("Overlay currently occupied, please wait for this round to complete before starting again. \n");
                        System.out.println("Enter 'ping' for live message counters of a hot node.\n");


                good = true;
            } else if(numberArg == 0){
                System.out.println("Thank you, zero messages were sent before you finished pressing enter.\n");
                good=true;
            } else if(numberArg < 0){
                System.out.println("Please use 'start x' for x >= 0, we do not currently support the upside-down.\n");
                good = true;
            } else {
                registry.running = true;

                registry.nodes.nodesWorking.set(registry.nodes.getNumberOfNodes());
                for(int i = 0 ; i < registry.nodes.doneYet.length; i++) {
                    registry.nodes.doneYet[i] = 0;
                }
                int numToSend = numberArg;
                System.out.println("start entered, sending " + numToSend + " messages...\n");
                RegistryRequestsTaskInitiate rrti = new RegistryRequestsTaskInitiate();
                rrti.packBytes(8, numToSend);
                sendTaskInitiate(rrti.messageBytes);
                good = true;
            }

        }
        if(  command.equalsIgnoreCase("print-done-yet") ){
            System.out.println("");
            System.out.println("print-done-yet entered, displaying active v. complete nodes:");
            registry.nodes.printDoneYet();
            good = true;
        }
        if(  command.equalsIgnoreCase("status") ){
            System.out.println("");

            System.out.println("Status: ");
            if (registry.summaryTable.table.size() == 0) {
                System.out.println("Overlay looks done. Ready to send messages. If 'start x' fails, restart Registry and Overlay.");
            }
            registry.nodes.printDoneYet();
            System.out.println("in-progress summary stats should be empty");
            registry.summaryTable.printSummary();
            good = true;


        }
        if(  command.equalsIgnoreCase("ping") ) {
            //pings a random node for their counters, can be done hot
            Random rand = new Random();
            int max = registry.nodes.getNumberOfNodes()-1;
            int min = 0;

            int entry = rand.nextInt((max - min) + 1) + min;

            PingRandomNode pingRandomNode = new PingRandomNode();
            byte[] msg = pingRandomNode.packBytes();
            Socket socket = new Socket(registry.nodes.table.get(entry).nodeHost, registry.nodes.table.get(entry).nodePort);
            TCPSender tcpSender = new TCPSender(socket,msg);
            Thread tcpSenderThread = new Thread(tcpSender);
            tcpSenderThread.start();


            good = true;

        }
        if(  command.equalsIgnoreCase("ping-all") ) {
            //pings all nodes for their counters, can be done hot
            for(RoutingEntry e : registry.nodes.table) {
                synchronized (this) {
                    PingRandomNode pingRandomNode = new PingRandomNode();
                    byte[] msg = pingRandomNode.packBytes();
                    Socket socket = new Socket(e.nodeHost, e.nodePort);
                    TCPSender tcpSender = new TCPSender(socket, msg);
                    Thread tcpSenderThread = new Thread(tcpSender);
                    tcpSenderThread.start();
                    tcpSenderThread.join();
                }
            }
            good = true;
        }

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
            System.out.println("print-done-yet\n :: prints running nodes (! Node[id]) vs. complete nodes (. Node[id])");
            System.out.println("");
            System.out.println("ping\n :: pings a random node for their counters");
            System.out.println("");
            System.out.println("ping-all\n :: pings all nodes for their counters\n :: :: if you want, you can hammer this at an actively sending system to see if it breaks\n\n");
        }
    }

    private void sendTaskInitiate(byte[] msg) throws IOException, InterruptedException {
        for(RoutingEntry e : registry.nodes.table){
            TCPSender tcpSender = new TCPSender((new Socket(e.nodeHost,e.nodePort)),msg);
            Thread tcpSenderThread = new Thread(tcpSender);
            tcpSenderThread.start();
            tcpSenderThread.join();

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
            sendOneRoute(e,n);
        }
    }
    private synchronized void sendOneRoute(RoutingEntry e,int n) throws IOException, InterruptedException {
        byte[] outgoingMsg;
        outgoingMsg = rsnm.packRoutesBytes(e,n);
        Socket outgoingSocket = new Socket(e.nodeHost,e.nodePort);
        registry.sockets[e.nodeId] = outgoingSocket;
        //prep send packed bytes
        TCPSender sender = new TCPSender(outgoingSocket,outgoingMsg);
        //thread send start
        Thread sendThread = new Thread(sender);
        sendThread.start();
        sendThread.join();
    }

    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        String typed;

        while(input.hasNextLine()){
            typed = input.nextLine();
            try {
                command(typed);
            } catch (IOException | InterruptedException e) {
                System.out.println("!!! CIRT Registry command IO error?");
                e.printStackTrace();
            }
        }
    }
}
