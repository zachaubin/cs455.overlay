package cs455.overlay.wireformats;

public class Event extends Driver{

    public boolean success;

    public Event(){
        success = false;
    }

    public String action(){
        return this.getClass().getSimpleName();
    }

}
