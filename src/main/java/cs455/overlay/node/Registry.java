package cs455.overlay.node;

import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.routing.SummaryTable;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.RegistryReportsRegistrationStatus;

import java.io.IOException;
import java.net.Socket;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;


public class Registry extends Node {

    private static Registry instance = null;
    public static Registry getInstance(int port) {
        if(instance == null) {
            instance = new Registry(port);
        }
        return instance;
    }

    public int port;
    public Socket socket;
    public volatile Socket[] sockets;

    public volatile RoutingTable nodes;
    public SummaryTable summaryTable;

    public TCPConnection tcp;

    public volatile AtomicInteger setupCount;

    public boolean overlayed = false;
    public volatile boolean running = false;

    public volatile TCPConnectionsCache cache = new TCPConnectionsCache();
    public volatile Thread cacheThread = null;

//    public ArrayList<TCPReceiver> tcpReceiverCache;
//    public ArrayList<TCPSender> tcpSenderCache;

    private Registry() { }
    //when started, it listens on a given port
    private Registry(int port) {
        this.port = port;
        sockets = new Socket[128];
        summaryTable = new SummaryTable();
        setupCount = new AtomicInteger(0);
    }

    private void command(String command){
        //list-messaging-nodes
        if(command.equalsIgnoreCase("list-messaging-nodes")){
            nodes.printTable();
        }
    }

    public int getNumberOfNodes(){
        return nodes.getNumberOfNodes();
    }
    public static <TCPRegistryServerThreadServerThread> void main(String[] args) throws IOException {
        RegistrySingleton.main(args);
    }


}


