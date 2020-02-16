package cs455.overlay.transport;

import cs455.overlay.node.MessagingNode;
import cs455.overlay.node.Registry;
import cs455.overlay.routing.RoutingEntry;
import cs455.overlay.routing.RoutingTable;
import cs455.overlay.wireformats.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPServerThread implements Runnable {

    int port;
    public static Socket listen;
    public MessagingNode node;

    int type;
    int regStatus;
    int numberOfNodesInRegistry;
    ServerSocket serverSocket;

    public OverlayNodeReceivesData onrd;
    public OverlayNodeSendsData onsd;

    public TCPServerThread(Socket socket, MessagingNode node) throws IOException {
        this.listen = socket;
        this.node = node;
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
        synchronized (this) {
            try {
                ServerSocket serverSocket = new ServerSocket(0);
                this.port = serverSocket.getLocalPort();
                System.out.println("NODE is listening on port[" + port + "]");
                node.nodePort = port;

                onrd = new OverlayNodeReceivesData();
                onsd = new OverlayNodeSendsData();

                while (true) {
                    System.out.println("BEGINNING OF {NODE SERVER WHILE true ACCEPT} BLOCK");
                    //wait for connect
                    listen = serverSocket.accept();
                    //got incoming connection

                    System.out.println("receiver thread start FOR NODE");
                    //receiver thread start
                    //pass socket to read from
//                TCPConnection nodeServer = new TCPConnection(listen,0);
//                Thread receive =  new Thread(nodeServer);
//                receive.start();
//                receive.join();


                    //writes to socket stream

                    //read byte[] from listen.socket
                    InputStream is = listen.getInputStream();

                    System.out.println("receiver thread Medium FOR NODE");


                    byte[] bytes = toByteArray(is);

                    System.out.println("\t\t\t\t\tbyte in sizeMOD::" + bytes.length);

                    System.out.println("TCPServerThread. == READ ALL BYTES INPUT STREAM IN node THREAD ==");
                    int fourcount = 0;
                    for (byte b : bytes) {
                        System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
                        fourcount++;
                        if (fourcount == 4) {
                            System.out.println("--------");
                            fourcount = 0;
                        }
                    }

//
//                System.out.println("");
//                System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = "+type);
//                System.out.println("");


                    int findHead = 0;
                    while (bytes[findHead] != -1) {
                        findHead++;
                    }
                    int type = (int) bytes[findHead + 4];

                    //yes this is where EventFactory should go
                    // 3 5 6 8 11
                    switch (type) {
                        case 3:
                            System.out.println("");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println("");
                            RegistryReportsRegistrationStatus msgRRRS = new RegistryReportsRegistrationStatus();
                            msgRRRS.unpackBytes(bytes);
                            node.nodeId = msgRRRS.nodeId;


                            if (msgRRRS.success == 1) {
                                System.out.println("Registration request successful. The number of messaging nodes currently constituting the overlay is <[" + msgRRRS.numberOfNodes + "]>");
                            }
                            if (msgRRRS.success == -1) {
                                System.out.println("Registration somehow unsuccessful. This should not happen.");
                            }
                            break;
                        case 5:
                            System.out.println(">>");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println(">>");
                            RegistryReportsDeregistrationStatus msgRRDS = new RegistryReportsDeregistrationStatus();
                            msgRRDS.unpackBytes(bytes);
                            break;
                        case 6:
                            System.out.println(">>");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println(">>");
                            RegistrySendsNodeManifest msgRSNM = new RegistrySendsNodeManifest();
                            msgRSNM.unpackRoutesBytes(bytes);
                            node.routes = msgRSNM.receivedTable;

                            node.idList = new int[msgRSNM.ids.size()];
                            for (int i = 0; i < msgRSNM.ids.size(); i++) {
                                node.idList[i] = msgRSNM.ids.get(i);
                            }
//                        System.out.println("trying to add sockets to routeLookupSocket, has size=" + node.routeLookupSocket.length);
                            for (RoutingEntry e : node.routes.table) {
                                Socket socket = new Socket(e.nodeHost, e.nodePort);
                                node.routeLookupSocket[e.nodeId] = socket;
                            }

                            System.out.println(":::::printing received table:::::::nodeId=" + node.nodeId);
                            node.routes.printTable();
                            System.out.println(":::::printed  received table:::::::nodeId=" + node.nodeId);

                            break;
                        case 8:
                            System.out.println(">>");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println(">>");
                            RegistryRequestsTaskInitiate msgRRTI = new RegistryRequestsTaskInitiate();
                            msgRRTI.unpackBytes(bytes);
                            node.numMsgsToSend = msgRRTI.numMsgs;
                            if (msgRRTI.numMsgs == 0) {
                                System.out.println("Send zero messages? Use 'start x' for x>0.");
                                break;
                            }
                            node.send_some_messages();
                            break;
                        case 11:
                            System.out.println(">>");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println(">>");
                            RegistryRequestsTrafficSummary msgRRTS = new RegistryRequestsTrafficSummary();
                            msgRRTS.unpackBytes(bytes);
                            break;
                        case 33:
                            System.out.println(">>");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println(">>");
                            // msg data in
                            onrd.unpackBytes(bytes);
                            int where = interpretMsg(onrd);
                            if (where == 1) {// for me

                                consumeMsg(onrd);

                            } else if (where == 0) {//for someone else

                                passMsg(onrd);

                            } else if (where == -1) {//error somehow
                                System.out.println("Error in received DATA msg?");

                            }


                            break;
                        default:
                            System.out.println(">>");
                            System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                            System.out.println(">>");
                            System.out.println("ERROR when THIS NODE received message, INVALID TYPE!!!?!?!?:: type = " + type);

                    }

                    System.out.println("here is the end of the server while loop");

                }

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
                        + port + " or listening for a connection: ");
                System.out.println(e.getMessage());
            }
        }
    }
    public int interpretMsg(OverlayNodeReceivesData onrd){
        if(onrd.idList[onrd.destinationIdIndex] == node.nodeId){
            return 1;//for me
        }
        return 0;//for else
    }

    public int consumeMsg(OverlayNodeReceivesData onrd){
        synchronized (this){//for me
            node.countReceived.incrementAndGet();
            node.sum.addAndGet(onrd.payload);
        }
        return 0;
    }
    public int passMsg(OverlayNodeReceivesData onrd) throws IOException {
        synchronized (this){//for else
            node.countRelayed.incrementAndGet();
        }
        try{
            pass_a_message(onrd);
        } catch(IOException e){
            System.out.println("++ ++ error passing a message: " + onrd.destinationId + " ("+onrd.payload+") -> " + onrd.destinationId);
            System.out.println("++ ++ ++ ++ printing path: ");
            for(int i : onrd.path){
                System.out.println(i);
            }
            System.out.println("++ ++ ++ ++ ++ ++ ++ ++");
        }
        return 0;
    }
    public void pass_a_message(OverlayNodeReceivesData onrd) throws IOException {
        for(int id : node.routeArrayList){

        }
        // nextDest
        AtomicInteger i = new AtomicInteger(0);
        int backstep = node.routeArrayList.get(i.get());
        int step = node.routeArrayList.get(i.get());
        while(step < onrd.destinationId) {
            backstep = node.routeArrayList.get(i.get());
            i.incrementAndGet();
            step = node.routeArrayList.get(i.get());
        }
        int nextDestination = backstep;
        byte[] msg = this.onsd.packBytes(onrd.type,onrd.idList[onrd.destinationIdIndex], node.nodeId,onrd.payload,onrd.path,node.nodeId);
        //socket to next in hop
        TCPSender tcpSender = new TCPSender(node.socketFinder(nextDestination),msg);
        System.out.println("i'm passing to "+nextDestination);
        Thread sendThread = new Thread(tcpSender);
        sendThread.start();



    }
    public Socket getSocket(){
        return listen;
    }
    public int getPort(){
        return port;
    }
    public void printBytes(byte[] bytes){
        int fourcount = 0;
        int bytecount = 0;
        for (byte b : bytes) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
            fourcount++;bytecount++;
            if(fourcount == 4) {
                System.out.println("--------TCP S T" + bytecount);
                fourcount = 0;
            }
        }
    }
}