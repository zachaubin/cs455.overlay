package cs455.overlay.node;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.transport.TCPServerThread;
import cs455.overlay.wireformats.OverlayNodeSendsRegistration;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class MessagingNodeSingleton {

    public static void main(String[] args) throws IOException, InterruptedException {

        //new node opens server on (reg host,reg port)
        String hostname_REGISTRY = args[0];
        int port_REGISTRY = Integer.parseInt(args[1]);
        MessagingNode currentNode = MessagingNode.getInstance(hostname_REGISTRY, port_REGISTRY);

        //connections cache
        currentNode.cacheThread = new Thread(currentNode.cache);
        currentNode.cacheThread.start();

        //NODE SERVER START
        int port_NODE_SERVER = -1;
        TCPServerThread server_thread = new TCPServerThread(currentNode.nodeSocket,currentNode);
        Thread serverThread = new Thread(server_thread);
        serverThread.start();
        while(currentNode.nodePort < 0) {
            sleep(69);
            port_NODE_SERVER = currentNode.nodePort;
        }

        //command thread
        CommandInputMessagingNodeThread cmnt = new CommandInputMessagingNodeThread(currentNode);
        Thread cmntThread = new Thread(cmnt);
        cmntThread.start();

        //// register ////
        //socket for registration
        Socket regSock = null;
        currentNode.cache.sockets[128] = regSock;
        try {
            regSock = new Socket(hostname_REGISTRY,port_REGISTRY);
        }catch (ConnectException e){
            System.out.println("\nCould not contact registry, bring that online first. \n\nExiting...\n");
            System.exit(1);
        }
        TCPConnection registerMe = new TCPConnection(regSock,1);

        //pack info
        OverlayNodeSendsRegistration regEvent = new OverlayNodeSendsRegistration(  regSock, hostname_REGISTRY , port_REGISTRY  );
        //external not needed, stored in marshallbytes in overlayevent
        System.out.println("about to PACK");
        byte[] bytes = regEvent.packBytes(2, InetAddress.getLocalHost().getHostName(),port_NODE_SERVER);

        //thread for registration USED WITH EVENT
        Thread registration = new Thread(regEvent);
        registration.start();
        //we are registered
    }
}
