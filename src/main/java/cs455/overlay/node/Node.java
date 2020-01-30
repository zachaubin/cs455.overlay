//import wireformats.Event;
package cs455.overlay.node;

import java.util.Random;

//
// !!
// !! FROM SLIDES
// !! Node [Interface with the onEvent(Event)  method]
// !!
//

public class Node  {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("!!!!!!node started!!!!!!!!!!");

        System.out.println("starting registry::");

        Registry registry = new Registry(0);

        System.out.println("success, open on port: " + registry.portNumber);

        System.out.println("trying to register two nodes...");

        MessagingNode node1 = new MessagingNode("localhost",registry.portNumber);
        node1.registerNode();
        MessagingNode node2 = new MessagingNode("localhost",registry.portNumber);
        node2.registerNode();


        System.out.println("printing routing table:");
        registry.nodes.printTable();



    }

}
