package cs455.overlay.transport;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TCPConnectionsCache implements Runnable{

    public volatile Socket[] sockets = new Socket[129];//extra slot for registry

    @Override
    public void run() {
        while(true){
            //forever
        }
    }



}
