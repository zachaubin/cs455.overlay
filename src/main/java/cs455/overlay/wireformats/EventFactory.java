package cs455.overlay.wireformats;

import java.nio.ByteBuffer;

public class EventFactory {
    //this will take event/msg of whatever type and create sub-event
    // of message type

    private static final EventFactory instance = new EventFactory();

    //default constructor to defeat instantiation
    private EventFactory(){}
    //@return returns the instance for the class
    public static EventFactory getInstance(){
        return instance;
    }

    ////create new event defined by the declared type in msg
    //@param marshalledBytes of message
    //@return the event object

    /*
    SEND RECEIVE
    :0:: OverlayNodeReceivesData
    :1:: OverlayNodeSendsData

    :2:: NodeReportsOverlaySetupStatus

    OVERLAY
    :3:: OverlayNodeReportsTaskFinished
    :4:: OverlayNodeReportsTrafficSummary
    :5:: OverlayNodeSendsDeregistration
    :6:: OverlayNodeSendsRegistration

    :7:: Protocol

    REGISTRY
    :8:: RegistryReportsDeregistrationStatus
    :9:: RegistryReportsRegistrationStatus
    :10: RegistryRequestsTaskInitiate
    :11: RegistryRequestsTrafficSummary
    :12: RegistrySendsNodeManifest

    :13: WireFormatWidget ?? just example or..
     */

    public Event createEvent(byte[] marshalledBytes) throws EventOutOfRangeException {
        int type = ByteBuffer.wrap( marshalledBytes ).getInt();


        switch(type){
            case 0: return new OverlayNodeReceivesData();
//                break;
            case 1: return new OverlayNodeSendsData();
//                break;
            case 2: return new NodeReportsOverlaySetupStatus();
//                break;
            case 3: return new OverlayNodeReportsTaskFinished();
//                break;
            case 4: return new OverlayNodeReportsTrafficSummary();
//                break;
            case 5: return new OverlayNodeSendsDeregistration();
//                break;
//            case 6: return new OverlayNodeSendsRegistration();//need to send byte[] of info// type,lengthrest,hostname,id
//                break;
            case 7: return new Protocol();
//                break;
            case 8: return new RegistryReportsDeregistrationStatus();
//                break;
//            case 9: return new RegistryReportsRegistrationStatus();
//                break;
//            case 10: return new RegistryRequestsTaskInitiate();
//                break;
            case 11: return new RegistryRequestsTrafficSummary();
//                break;
            case 12: return new RegistrySendsNodeManifest();
//                break;
//            case 13: return new WireFormatWidget();
//                break;
            default:
                throw new EventOutOfRangeException("Event could not be created, Id out of range: " + type);
        }

    }

    class EventOutOfRangeException extends Exception {
        public EventOutOfRangeException(String message)
        {
            super(message);
        }
    }
}
