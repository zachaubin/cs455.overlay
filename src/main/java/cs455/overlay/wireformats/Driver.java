package cs455.overlay.wireformats;

import java.nio.ByteBuffer;

public class Driver {

    public static void main(String[] args) throws EventFactory.EventOutOfRangeException {

        EventFactory factory = EventFactory.getInstance();

        int type = 0;
        byte[] marshalledBytes = ByteBuffer.allocate( 4 ).putInt( type ).array();

        Event event = factory.createEvent( marshalledBytes );
        System.out.println("Event Factory Created \'" + event.action() + "\'");
    }
}
