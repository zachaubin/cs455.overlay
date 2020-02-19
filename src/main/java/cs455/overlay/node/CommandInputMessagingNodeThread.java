package cs455.overlay.node;

import cs455.overlay.transport.TCPSender;
import cs455.overlay.wireformats.OverlayNodeSendsDeregistration;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class CommandInputMessagingNodeThread implements Runnable {

    public MessagingNode node;

    public CommandInputMessagingNodeThread(MessagingNode node){
        this.node = node;
    }


    private void command(String command) throws IOException, InterruptedException {
        //list-messaging-nodes
        System.out.println("command function called");
        boolean good = false;
        if(  command.equalsIgnoreCase("print-counters-and-diagnostics") || command.equalsIgnoreCase("print")){
            System.out.println("print-counters-and-diagnostics entered, printing stats...\n");
            System.out.println("Messages Sent     <:["+node.countSent + "]:>");
            System.out.println("Messages Relayed  <:["+node.countRelayed + "]:>");
            System.out.println("Messages Received <:["+node.countReceived + "]:>");
            System.out.println("");
            System.out.println("Payload Sent Summation <:["+node.sentSum+"]:>");
            System.out.println("Payload Received Summation <:["+node.sum+"]:>");

            good = true;
        }
        if(  command.equalsIgnoreCase("exit-overlay") ){
            System.out.println("exit-overlay deregisters this node from the registry and exits.\n");
            //this sends node deregistration and
            // upon successful reply it kills itself
            if(node.idList.length != 0){
                System.out.println("Relay already established, cannot deregister\n");
                good=true;
            }else {
                OverlayNodeSendsDeregistration onsd = new OverlayNodeSendsDeregistration();
                TCPSender tcpSender = new TCPSender(new Socket(node.registryHostname, node.portNumber), onsd.packBytes(node.nodeId));
                Thread tcpSenderThread = new Thread(tcpSender);
                tcpSenderThread.start();
                tcpSenderThread.join();
                System.exit(0);

                good = true;
            }
        }

        if(!good){
            System.out.println("");
            System.out.println(":: :: :: :: :: :: :: :: :: :: :: :: :: :: :: ::");
            System.out.println("  !!!! Invalid command [[\""+command+"\"]] !!!!");
            System.out.println(":: :: :: :: :: :: :: :: :: :: :: :: :: :: :: ::");
            System.out.println("");

            System.out.println("  >> Valid commands are: <<");
            System.out.println("");
            System.out.println("print-counters-and-diagnostics\n :: prints number msgs ( sent, received, relayed ), and total *values* ( sent, received ).");
            System.out.println("");
            System.out.println("exit-overlay\n :: deregisters the node from the registry, must be run before overlay is set up.");
            System.out.println("");
        }

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
                e.printStackTrace();
            }
        }
    }
}
