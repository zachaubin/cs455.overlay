package cs455.overlay.node;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MessagingNodeSingleton {

    public static void main(String[] args) throws IOException, InterruptedException {

        //new node opens server on (reg host,reg port)
//        System.out.println("starting a node");
        String hostname_REGISTRY = args[0];
        int port_REGISTRY = Integer.parseInt(args[1]);
        MessagingNode currentNode = MessagingNode.getInstance(hostname_REGISTRY, port_REGISTRY);

        //NODE SERVER START
//        currentNode.nodeSocket= new Socket("localhost",0);
//        int port_NODE_SERVER = currentNode.nodeSocket.getPort();
        int port_NODE_SERVER = -1;
//        System.out.println("NODE server thread runs on loop to tcp receive and print based on msg");
        TCPServerThread server_thread = new TCPServerThread(currentNode.nodeSocket,currentNode);
//        System.out.println("1 OF {NODE SERVER START} BLOCK");

//        System.out.println("2 OF {NODE SERVER START} BLOCK");
        Thread serverThread = new Thread(server_thread);
//        System.out.println("3 OF {NODE SERVER START} BLOCK");
        serverThread.start();
        while(currentNode.nodePort < 0) {
//            System.out.println("port is -2");
            sleep(69);
            port_NODE_SERVER = currentNode.nodePort;
        }
//        System.out.println("after while port node server is " + port_NODE_SERVER);
//        System.out.println("END OF {NODE SERVER START} BLOCK");

        //command thread
        CommandInputMessagingNodeThread cmnt = new CommandInputMessagingNodeThread(currentNode);
        Thread cmntThread = new Thread(cmnt);
        cmntThread.start();



        //// register ////

        //socket for registration
        Socket regSock = new Socket(hostname_REGISTRY,port_REGISTRY);
        TCPConnection registerMe = new TCPConnection(regSock,1);

        //pack info
        OverlayNodeSendsRegistration regEvent = new OverlayNodeSendsRegistration(  regSock, hostname_REGISTRY , port_REGISTRY  );
        //external not needed, stored in marshallbytes in overlayevent
        System.out.println("about to PACK");
        byte[] bytes = regEvent.packBytes(2, InetAddress.getLocalHost().getHostName(),port_NODE_SERVER);
//        currentNode.connections.addConnection(registerMe);

        System.out.println("   >registering on port["+port_NODE_SERVER+"]");
        //thread for registration USED WITH EVENT
        Thread registration = new Thread(regEvent);
        registration.start();
        //we are registered


        System.out.println("made to end of main in MSGNODE");


//        currentNode.registerNode();
    }
}
