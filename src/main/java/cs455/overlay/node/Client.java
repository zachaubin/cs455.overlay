package cs455.overlay.node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    private static SocketChannel client;
    private static ByteBuffer buffer;

    public static void main(String[] args) throws IOException{
        String hostname = "localhost";
        int port = 1090;
        try {
            //connect to server
            client = SocketChannel.open(new InetSocketAddress(hostname,port));
            //create buffer
            buffer = ByteBuffer.allocate(256);
        } catch (IOException e){
            System.err.println("error connecting to server, stacktrace:...");
            e.printStackTrace();
        }

        buffer = ByteBuffer.wrap("Please send this back to me.".getBytes());
        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println("Server responded with: " + response);
            buffer.clear();
        } catch (IOException e){
            System.err.println("error receiving from server, stacktrace:...");
            e.printStackTrace();
        }
    }
}
