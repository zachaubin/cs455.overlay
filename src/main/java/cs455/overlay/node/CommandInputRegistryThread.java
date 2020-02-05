package cs455.overlay.node;

import cs455.overlay.routing.RoutingTable;

import java.util.Scanner;

public class CommandInputRegistryThread implements Runnable {

    public Registry registry;

    public CommandInputRegistryThread(Registry registry){
        this.registry = registry;
    }


    private void command(String command){
        //list-messaging-nodes
        System.out.println("command function called");
        boolean good = false;
        if(  command.equalsIgnoreCase("list-messaging-nodes") ){
            System.out.println("list messaging nodes entered...\n");
            registry.nodes.printTable();
            good = true;
        }
        if(  command.equalsIgnoreCase("setup-overlay") ){
            System.out.println("setup overlay needs to be implemented\n");
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
            System.out.println("setup-overlay number-of-routing-table-entries (e.g. setup-overlay 3)\n :: sets up overlay by reporting [host/port/id] table to each node in table");
            System.out.println("");
            System.out.println("list-routing-tables\n :: same as list-messaging nodes with note and more spacing");
            System.out.println("");
            System.out.println("start number-of-messages (e.g. start 25000)\n :: asks each node to send this many messages to random other nodes");

        }

    }


    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        String typed;
        while(input.hasNextLine()){
            typed = input.next();
            System.out.println("waiting for command");
            command(typed);
        }
    }
}
