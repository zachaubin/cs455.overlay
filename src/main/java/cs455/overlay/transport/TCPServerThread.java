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
    public static volatile Socket listen;
    public MessagingNode node;

    public OverlayNodeReceivesData onrd;
    public OverlayNodeSendsData onsd;

    public TCPServerThread(Socket socket, MessagingNode node) throws IOException {
        this.listen = socket;
        this.node = node;
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
//        System.out.println("to byte array tobytearray |0");

        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        System.out.println("to byte array tobytearray |1");

        byte[] buffer = new byte[1024];
        int len;

//        System.out.println("to byte array tobytearray |2, in.available=" + in.available());

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
//            System.out.println("to byte array tobytearray |3l");

            os.write(buffer, 0, len);
        }
//        System.out.println("to byte array tobytearray |4");


        return os.toByteArray();
    }



    public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(0);
                this.port = serverSocket.getLocalPort();
//                System.out.println("NODE is listening on port[" + port + "]");
                node.nodePort = port;

                onrd = new OverlayNodeReceivesData(node);
                onsd = new OverlayNodeSendsData();

                while (true) {
//                    System.out.println("BEGINNING OF {NODE SERVER WHILE true ACCEPT} BLOCK");
                    //wait for connect

                    listen = serverSocket.accept();
                    //got incoming connection

                    Thread threadReceiverThread = new Thread(new ReceiverThread(listen));
                    threadReceiverThread.start();


//                    System.out.println("here is the end of the server while loop");

                }

            } catch (IOException e) {
                System.out.println("Exception caught when trying to listen on port "
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
//            System.out.println("TCP ST: receiver thread start FOR NODE");
            //receiver thread start
            //pass socket to read from

            //read byte[] from listen.socket
            InputStream is = null;
            try {
                is = listen.getInputStream();
            } catch (IOException e) {
                System.out.println("No input stream for active socket sending to TCP ST?");
                e.printStackTrace();
            }

//            System.out.println("receiver thread Medium FOR NODE");

            while(true) {
                try {
                    if (!(is.available() == 0)) {
//                        System.out.println(">>> input stream available, exiting loop...");
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ;
            }
            byte[] bytes = new byte[0];
            try {
                bytes = toByteArray(is);
            } catch (IOException e) {
                e.printStackTrace();
            }

//            if(type == 8) {

//                System.out.println("\t\t\t\t\tbyte in sizeMOD::" + bytes.length);

//                System.out.println("TCPServerThread. == READ ALL BYTES INPUT STREAM IN node THREAD ==");
//                int fourcount = 0;
//                for (byte b : bytes) {
//                    System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
//                    fourcount++;
//                    if (fourcount == 4) {
//                        System.out.println("TCP ST RecTH -------- Start Message Init?");
//                        fourcount = 0;
//                    }
//                }
//            }

            int findHead = 0;
            while (bytes[findHead] != -1) {
                findHead++;
            }
            int type = (int) bytes[findHead + 4];

            try {
                typeSwitch(type,bytes);
            } catch (IOException | InterruptedException e) {
                System.out.println("TCP ST: Error: @ type switch in server thread.");
                System.out.println("TCP ST: Error: type="+type);
                System.out.println("TCP ST: Error: bytes[]=");
                printBytes(bytes);
                e.printStackTrace();
            }

        }
    }
    public int interpretMsg(OverlayNodeReceivesData onrd){
        if(node.idList[onrd.destinationIdIndex] == node.nodeId){
            return 1;//for me
        }
        return 0;//for else
    }

    public int consumeMsg(OverlayNodeReceivesData onrd){
        synchronized (this){//for me
            node.countReceived.incrementAndGet();
            node.sum.addAndGet(onrd.payload);
//            System.out.println("path to get here:");
//            for(int i : onrd.path){
//                System.out.println(i);
//            }
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
            if(i.get() >= node.routeArrayList.size()){
                break;
            }
            step = node.routeArrayList.get(i.get());
        }
        int nextDestination = backstep;
        byte[] msg = this.onsd.packBytes(onrd.type,node.idList[onrd.destinationIdIndex], node.nodeId,onrd.payload,onrd.path,node.nodeId);
        //socket to next in hop
        TCPSender tcpSender = new TCPSender(new Socket(node.socketFinder(nextDestination).nodeHost,node.socketFinder(nextDestination).nodePort),msg);
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
                System.out.println("TCP S T--------TCP S T :: bytecount:"+ bytecount);
                fourcount = 0;
            }
        }
    }
    public void typeSwitch(int type, byte[] bytes) throws IOException, InterruptedException {
        //yes this is where EventFactory should go
        // 3 5 6 8 11
        switch (type) {
            case 3:
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
                RegistryReportsDeregistrationStatus msgRRDS = new RegistryReportsDeregistrationStatus();
                msgRRDS.unpackBytes(bytes);
                break;
            case 6:
                RegistrySendsNodeManifest msgRSNM = new RegistrySendsNodeManifest();
                msgRSNM.unpackRoutesBytes(bytes);
                node.routes = msgRSNM.receivedTable;
                node.idList = new int[msgRSNM.ids.size()];
                for (int i = 0; i < msgRSNM.ids.size(); i++) {
                    node.idList[i] = msgRSNM.ids.get(i);
                }
                for (RoutingEntry e : node.routes.table) {
                    node.routeArrayList.add(e.nodeId);
                }
                //reply with setup ok
                OverlayNodeReportsOverlaySetupStatus onross = new OverlayNodeReportsOverlaySetupStatus(node);
                Thread onrossThread = new Thread(onross);
                onrossThread.start();
                break;
            case 8:
                RegistryRequestsTaskInitiate msgRRTI = new RegistryRequestsTaskInitiate();
                msgRRTI.unpackBytes(bytes);
                node.numMsgsToSend = msgRRTI.numMsgs;
                if (msgRRTI.numMsgs == 0) {
                    //pretty sure this is handled elsewhere, keeping redundancy
                    System.out.println("Send zero messages? Use 'start x' for x>0.");
                    break;
                }
                node.send_some_messages();
                break;
            case 11:
                OverlayNodeReportsTrafficSummary onrts = new OverlayNodeReportsTrafficSummary(node);
                Thread onrtsThread = new Thread(onrts);
                onrtsThread.start();
                onrtsThread.join();
                node.countSent.set(0);
                node.countRelayed.set(0);
                node.countReceived.set(0);
                node.sentSum.set(0);
                node.sum.set(0);
                break;
            case 21:
                PingRandomNode pingRandomNode = new PingRandomNode();
                byte[] msg = pingRandomNode.nodePackBytes(node);
                Socket socket = new Socket(node.registryHostname,node.portNumber);
                TCPSender tcpSender = new TCPSender(socket,msg);
                Thread tcpSenderThread = new Thread(tcpSender);
                tcpSenderThread.start();
                break;
            case 33:
                // msg data in
                synchronized (this) {
                    onrd.unpackBytes(bytes);
                    int where = interpretMsg(onrd);
                    if (where == 1) {// for me

                        consumeMsg(onrd);

                    } else if (where == 0) {//for someone else

                        passMsg(onrd);

                    } else if (where == -1) {//error somehow
                        System.out.println("Error in received DATA msg?");
                    }
                }
                break;
            default:// this was for debugging, but will trigger if a random program tries to connect
                System.out.println(">>");
                System.out.println(">>>> THIS NODE JUST RECEIVED A MESSAGE OF TYPE = " + type);
                System.out.println(">>");
                System.out.println("ERROR when THIS NODE received message, INVALID TYPE!!!?!?!?:: type = " + type);
                System.out.println("ERROR: printing bytes msg...");
                printBytes(bytes);
        }
    }

}