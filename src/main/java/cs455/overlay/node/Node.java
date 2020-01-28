//import wireformats.Event;
package cs455.overlay.node;

import java.util.Random;

public class Node  {

//    void onEvent(Event) {
//        //switch to say what kind of node to use based on event
//
//        //event==reg
//            //new reg
//        //event=msg
//            //new msgnode
//
//    }


    public static void main(String[] args) throws InterruptedException {

        System.out.println("!!!!!!node started!!!!!!!!!!");

        if(args.length > 0) {
            String command = args[0];

            if (command.compareTo("server") == 0) {
                Node server = new Registry(0);
            }
            if (command.compareTo("client") == 0) {
               // Node client = new MessagingNode("localhost",0);
            }
        }

        Random rd = new Random(); // creating Random object
        System.out.println(rd.nextBoolean()); // displaying a random boolean


        //general test
        //Node server = new Registry();
        if(rd.nextBoolean()) {
            new Thread(new Registry(0));
        } else {
            //Node client = new MessagingNode();
            //new Thread(new MessagingNode("localhost",0));
        }



    }

}
