package cs455.overlay.wireformats;

import java.net.Socket;

public class Event extends Driver{

    public boolean success;
    public Socket superSocket;

    public Event(Socket socket){
        this.superSocket = socket;
        success = false;
    }

    public Event() { success = false; }


    public String action(){
        return this.getClass().getSimpleName();
    }

}
