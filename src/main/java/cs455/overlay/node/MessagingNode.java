package cs455.overlay.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessagingNode extends Node implements Runnable {

    MessagingNode() {
        String hostName = "localhost";
        int portNumber = 1090;

        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out =
                        new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn =
                        new BufferedReader(
                                new InputStreamReader(System.in))
        ) {
            String userInput;
//            while ((userInput = stdIn.readLine()) != null) {
                userInput = "This is a test from the client.";
                out.println(userInput);
                System.out.println("CLIENT SAYS: " + in.readLine());
//            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }

    public void run() {
        System.out.println("Hello from a CLIENT thread!");

    }

    public static void main(String[] args){

    }
}
