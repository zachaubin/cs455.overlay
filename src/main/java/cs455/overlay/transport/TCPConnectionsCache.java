package cs455.overlay.transport;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TCPConnectionsCache {
    public Map<Socket, List<String>> cache;

    public ArrayList<TCPConnection> test;

    public void add(TCPConnection t,String host, int port){
        this.cache.put( t.socket, Arrays.asList(host,Integer.toString(port)) );
    }

    public void testadd(TCPConnection t){
        test.add(t);
    }
//
//    public List<String> get(Socket s){
//
//    }

    public void removeConnection(TCPConnection t, String host, int port){


    }

}
