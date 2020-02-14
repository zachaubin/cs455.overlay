package cs455.overlay.node;

import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPRegistryServerThread;

import java.io.IOException;

public class RegistrySingleton {


    public static <TCPRegistryServerThreadServerThread> void main(String[] args) throws IOException {

        System.out.println("Create registry on CLI specified port number, this will listen");

        int argPort = Integer.parseInt(args[0]);

        //new registry singleton
        //create registry on CLI specified port number, this will listen
        Registry registry = Registry.getInstance(argPort);
        System.out.println("port:"+registry.port);
        registry.nodes = new RoutingTable();
        registry.socket = null;

        //listen server
        System.out.println("server thread runs on loop to tcp receive and register nodes");
        TCPRegistryServerThread serverthreadsocket = new TCPRegistryServerThread(registry.socket,registry,registry.port);
        Thread serverThread = new Thread(serverthreadsocket);
        serverThread.start();

        //command controller
        CommandInputRegistryThread commands = new CommandInputRegistryThread(registry);
        Thread commandsThread = new Thread(commands);
        commandsThread.start();



    }
}