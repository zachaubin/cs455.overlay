package cs455.overlay.node;

import java.util.Scanner;

public class CommandInputMessagingNodeThread implements Runnable {

    public MessagingNode node;

    public CommandInputMessagingNodeThread(MessagingNode node){
        this.node = node;
    }


    private void command(String command){
        //list-messaging-nodes
        System.out.println("command function called");
        boolean good = false;
        if(  command.equalsIgnoreCase("print-counters-and-diagnostics") ){
            System.out.println("print-counters-and-diagnostics entered...\n");
            good = true;
        }
        if(  command.equalsIgnoreCase("exit-overlay") ){
            System.out.println("exit-overlay needs to be implemented\n");
            good = true;
        }
        if(  command.equalsIgnoreCase("list-routing-tables") ){
            System.out.println("list routing tables entered...\n");
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
            System.out.println("print-counters-and-diagnostics\n :: prints number msgs ( sent, received, relayed ), and total *values* ( sent, received )");
            System.out.println("");
            System.out.println("exit-overlay\n :: sets up overlay by reporting [host/port/id] table to each node in table");
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
