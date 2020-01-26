//import wireformats.Event;
package node;

public class Node {

//    void onEvent(Event) {
//        //switch to say what kind of node to use based on event
//
//        //event==reg
//            //new reg
//        //event=msg
//            //new msgnode
//
//    }
    public static void main(String[] args){

        System.out.println("node started");

        if(!args[0].isEmpty()) {
            String command = args[0];

            if (command.compareTo("server") == 0) {
                Node server = new Registry();
            }
            if (command.compareTo("client") == 0) {
                Node client = new MessagingNode();
            }
        }


    }

}
