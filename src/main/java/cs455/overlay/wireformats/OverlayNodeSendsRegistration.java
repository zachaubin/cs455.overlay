package cs455.overlay.wireformats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class OverlayNodeSendsRegistration extends Event {

    public OverlayNodeSendsRegistration(Socket socket, String hostname, int portNumber, int nodeId) {
//        super(socket);

        //really we need to:
        // connect to registry
        // send nodeId
        // that's it, registry will report success through another method

        //for now this is all node will send, keep int nodeId change to b[] message later



        try (

//                Socket socket = new Socket(hostname, portNumber);



                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);


                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                //userInput = "This is a test from the client.";
                out.println(userInput);
                System.out.println("CLIENT " + nodeId + " SAYS: " + userInput);
            }
        } catch (
                UnknownHostException e) {
            System.err.println("Don't know about host " + hostname);
            System.exit(1);
        } catch (
                IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostname);
            System.exit(1);
        }
    }
}
