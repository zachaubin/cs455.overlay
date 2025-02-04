package cs455.overlay.node;

import cs455.overlay.routing.RoutingTable;
import cs455.overlay.transport.TCPRegistryServerThread;

import java.io.IOException;

public class RegistrySingleton {


    public static <TCPRegistryServerThreadServerThread> void main(String[] args) throws IOException {

        int argPort = Integer.parseInt(args[0]);

        //new registry singleton
        //create registry on CLI specified port number, this will listen
        Registry registry = Registry.getInstance(argPort);
        System.out.println("Registry online and listening on port " +registry.port+".");
        System.out.println("");
        registry.nodes = new RoutingTable();
        registry.socket = null;
        registry.cacheThread = new Thread(registry.cache);
        registry.cacheThread.start();

        //listen server
        TCPRegistryServerThread serverthreadsocket = new TCPRegistryServerThread(registry.socket,registry,registry.port);
        Thread serverThread = new Thread(serverthreadsocket);
        serverThread.start();

        //command controller
        CommandInputRegistryThread commands = new CommandInputRegistryThread(registry);
        Thread commandsThread = new Thread(commands);
        commandsThread.start();
    }
}