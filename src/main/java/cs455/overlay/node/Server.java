package cs455.overlay.node;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {


    public Server() throws IOException {
    }

    public static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException{
        //grab incoming socket from server
        SocketChannel client = serverSocket.accept();
        //configure it to be a new channel
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("\t\tNew client registered.");

    }

    private static void readAndRespond(SelectionKey key) throws IOException {
        //create buffer to write into
        ByteBuffer buffer = ByteBuffer.allocate(256);

        //grab the socket from the key
        SocketChannel client = (SocketChannel) key.channel();
        //Read from it
        int bytesRead = client.read(buffer);
        //handle a closed connection
        if(bytesRead == -1){
            client.close();
            System.out.println("\t\tReceived: " + new String(buffer.array()));
            //flip the buffer to new write
            buffer.flip();
            client.write(buffer);
            //clear the buffer
            buffer.clear();
        }
    }

    public static void main(String[] args) throws IOException {
        //open selector
        Selector selector = Selector.open();
        //create input channel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        String hostname = "localhost";
        int port = 1090;
        serverSocket.bind(new InetSocketAddress(hostname,port));
        serverSocket.configureBlocking(false);
        //register our channel to the selector
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        //loop on selector
        while(true) {
            System.out.println("listening for new connection or new messages on >>> host["+hostname+"]:port["+port+"]");
            //block here
            selector.select();
            System.out.println("\tActivity on selector!");

            //Keys are ready
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            //loop over ready keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while(iter.hasNext()) {
                //grab current key
                SelectionKey key = iter.next();

                //Optional
                if(key.isValid() == false){
                    register(selector, serverSocket);

                }

                //previous connection has data to read
                if(key.isReadable()) {
                    readAndRespond(key);
                }

                //remove it from our set
                iter.remove();
            }
        }
    }
}
