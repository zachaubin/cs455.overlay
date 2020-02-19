package cs455.overlay.transport;

import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPRegistryServerThread implements Runnable {

    int port;
    public static Socket listen;
    public Registry registry;

    public TCPConnectionsCache cache;

    public TCPRegistryServerThread(Socket socket, Registry registry,int port) {
        this.listen = socket;
        this.registry = registry;
        this.port = port;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {

                listen = serverSocket.accept();
                //got incoming connection

                Thread threadRegistryReceiverThread = new Thread(new ReceiverThread(listen));
                threadRegistryReceiverThread.start();
            }

        } catch (IOException e) {
            System.out.println("TCP RS T:run():Exception caught when trying to listen on port "
                    + port + " or listening for a connection: ");
            System.out.println(e.getMessage());
        }
    }
    private class ReceiverThread implements Runnable{

        private Socket listen;

        private ReceiverThread(Socket socket){
            listen = socket;
        }

        @Override
        public void run() {
            InputStream is = null;
            try {
                is = listen.getInputStream();
            } catch (IOException e) {
                System.out.println("No input stream for active socket sending to TCP ST?\n");
                e.printStackTrace();
            }
            while(true) {
                try {
                    if (!(is.available() == 0)) {
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("Registry has no input stream for active incoming socket.\n");
                    e.printStackTrace();
                }
            }
            byte[] bytes = new byte[0];
            try {
                bytes = toByteArray(is);
            } catch (IOException e) {
                System.out.println("TCP Registry ST actual receive thread: error converting registry input stream msg to byte[]");
                e.printStackTrace();
            }

            int findHead = 0;
            while (bytes[findHead] != -1) {
                findHead++;
            }
            int type = (int) bytes[findHead + 4];
            try {
                typeSwitch(type,bytes);
            } catch (IOException | InterruptedException e) {
                System.out.println("TCP RST: Error: @ type switch in server thread.");
                System.out.println("TCP RST: Error: type="+type);
                System.out.println("TCP RST: Error: bytes[]=");
                printBytes(bytes);
                e.printStackTrace();
            }

        }
    }
    public void printBytes(byte[] bytes){
        int fourcount = 0;
        int bytecount = 0;
        for (byte b : bytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;bytecount++;
            if(fourcount == 4) {
                System.out.println("TCP S T--------TCP S T :: bytecount:"+ bytecount);
                fourcount = 0;
            }
        }
    }
    public void typeSwitch(int type, byte[] bytes) throws IOException, InterruptedException {
        //yes this is where EventFactory should go
        // 2 4 7 10 12
        switch (type) {
            case 2://node sends registration
                OverlayNodeSendsRegistration marshall = new OverlayNodeSendsRegistration();
                marshall.unpackBytes(bytes);

                // host port
                int nodeId = registry.nodes.addRoutingEntry(marshall.hostname,marshall.port);

                //respond
                RegistryReportsRegistrationStatus rrrs = new RegistryReportsRegistrationStatus();
                byte[] packedBytes = rrrs.packBytes(3,1,registry.nodes.getNumberOfNodes(),nodeId);
                Socket socket = new Socket(marshall.hostname,marshall.port);
                registry.cache.sockets[nodeId] = socket;
                TCPSender tcpSender = new TCPSender(socket,packedBytes);
                Thread threadSender = new Thread(tcpSender);
                threadSender.start();

                break;
            case 4://node sends deregistration
                OverlayNodeSendsDeregistration dereg = new OverlayNodeSendsDeregistration();
                dereg.unpackBytes(bytes);
                registry.nodes.removeRoutingEntry(dereg.nodeId);
                break;
            case 7://node reports overlay setup
                synchronized (this){
                    registry.setupCount.incrementAndGet();
                    if(registry.setupCount.get() == registry.nodes.getNumberOfNodes()){
                        registry.overlayed = true;
                        System.out.println("Overlay reported online and ready to send messages. Please enter 'start x' to send some number of messages.\n\n");
                    }
                }
                break;
            case 10://node reports task finished
                OverlayNodeReportsTaskFinished onrtf = new OverlayNodeReportsTaskFinished();
                onrtf.unpackBytes(bytes);
                registry.nodes.doneYet[onrtf.nodeId] = 1;
                registry.nodes.nodesWorking.decrementAndGet();
                if((int) registry.nodes.nodesWorking.get() == 0){
                    //all done
                    System.out.println("");
                    System.out.println("All Nodes Reported Complete. Requesting summaries and printing totals.");
                    System.out.println("");

                    //trigger stat requests, this one loops over nodes
                    RegistryRequestsTrafficSummary rrts = new RegistryRequestsTrafficSummary(registry);
                    Thread rrtsThread = new Thread(rrts);
                    rrtsThread.start();
                    rrtsThread.join();
                }
                break;
            case 12:
                OverlayNodeReportsTrafficSummary onrts = new OverlayNodeReportsTrafficSummary();
                onrts.unpackBytes(bytes);
                registry.summaryTable.addEntry(onrts.nodeId,onrts.countSent,onrts.countRelayed,onrts.countReceived,onrts.sentSum,onrts.sum);

                if(registry.nodes.table.size() == registry.summaryTable.table.size()){
                    //all reported, print stats
                    registry.summaryTable.printSummary();
                    //reset registry counter
                    registry.summaryTable.table.clear();
                    registry.running = false;
                }
                break;
            case 21:
                synchronized (this) {
                    PingRandomNode pingRandomNode = new PingRandomNode();
                    pingRandomNode.unpackBytesFromNode(bytes);
                    System.out.println("");
                    System.out.println("Received results from a ping:");
                    System.out.println("");
                    System.out.println("Node Id: " + pingRandomNode.nodeId);
                    System.out.println("Sent: " + pingRandomNode.countSent);
                    System.out.println("Relayed: " + pingRandomNode.countRelayed);
                    System.out.println("Received: " + pingRandomNode.countReceived);
                    System.out.println("SumOut: " + pingRandomNode.sentSum);
                    System.out.println("SumIn: " + pingRandomNode.sum);
                    System.out.println("");
                }
                break;
            default:
                System.out.println(">>");
                System.out.println(">>>> Registry JUST RECEIVED A MESSAGE OF TYPE = " + type);
                System.out.println(">>");
                System.out.println("ERROR when THIS NODE received message, INVALID TYPE!!!?!?!?:: type = " + type);
                System.out.println("ERROR: printing bytes msg...");
                printBytes(bytes);

        }
    }
    public Socket getSocket(){
        return listen;
    }
}
