package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;
import cs455.overlay.wireformats.RegistrySendsNodeManifest;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class CommandInputRegistryThread implements Runnable {

    public Registry registry;

    public CommandInputRegistryThread(Registry registry){
        this.registry = registry;
    }


    private void command(String command) throws IOException {
        //list-messaging-nodes
        System.out.println("command function called");
        boolean good = false;
        if(  command.equalsIgnoreCase("list-messaging-nodes") ){
            System.out.println("list messaging nodes entered...\n");
            registry.nodes.printTable();
            good = true;
        }
        if(  command.equalsIgnoreCase("setup-overlay") ){
            System.out.println("setup overlay sends manifest (routing table all entries) to all nodes in manifest\n");

            for(RoutingEntry e : registry.nodes.table){
                sendManifest(e);
            }

            good = true;
        }
        if(  command.equalsIgnoreCase("list-routing-tables") ){
            System.out.println("list routing tables entered...\n");
            registry.nodes.printTableSpaced();
            good = true;
        }

        if(  command.equalsIgnoreCase("list-routing-tables") ){
            System.out.println("list routing tables entered...\n");
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
            System.out.println("list-messaging-nodes\n :: list all messaging nodes in routing table");
            System.out.println("");
            System.out.println("'setup-overlay' will become setup-overlay number-of-routing-table-entries (e.g. setup-overlay 3)\n :: sets up overlay by reporting [host/port/id] table to each node in table");
            System.out.println("");
            System.out.println("list-routing-tables\n :: same as list-messaging nodes with note and more spacing");
            System.out.println("");
            System.out.println("start number-of-messages (e.g. start 25000)\n :: asks each node to send this many messages to random other nodes");
            System.out.println("");
            System.out.println("");

        }

    }

    private void sendManifest(RoutingEntry e) throws IOException {
        Socket response = new Socket(e.nodeHost,e.nodePort);
        System.out.println("CommandRegistryThread.sendManifest: reporting status |0");
        RegistrySendsNodeManifest reportReg = new RegistrySendsNodeManifest(response, registry.nodes);

        System.out.println("CommandRegistryThread.sendManifest: reporting status |1");
        reportReg.packBytes(6,registry.nodes);

        Thread report = new Thread(reportReg);
        System.out.println("CommandRegistryThread.sendManifest: reporting status |2");

        report.start();
        System.out.println("CommandRegistryThread.sendManifest: reporting status |3");

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
            } catch (IOException e) {
                System.out.println("!!!Registry command IO error?");
                e.printStackTrace();
            }
        }
    }
}
