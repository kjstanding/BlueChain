package node;

import node.blockchain.Block;
import node.communication.Address;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Node represents a peer, a cooperating member within the network
 */
public class Node  {
    private final int MAX_PEERS;
    private final int INITIAL_CONNECTIONS;
    private final Object lock;
    private ServerSocket ss;
    private ArrayList<Block> blockchain;
    private ArrayList<Address> globalPeers;
    private ArrayList<Address> localPeers;
    private Address myAddress;

    /* A collection of getters */
    public int getMaxPeers(){return this.MAX_PEERS;}
    public int getInitialConnections(){return this.INITIAL_CONNECTIONS;}
    public Address getAddress(){return this.myAddress;}
    public ArrayList<Address> getLocalPeers(){synchronized (lock){return this.localPeers;}}

    /**
     * Node constructor creates node and begins server socket to accept connections
     *
     * @param port
     * @param maxPeers
     * @param initialConnections
     */
    public Node(int port, int maxPeers, int initialConnections) {
        lock =  new Object();
        blockchain = new ArrayList<Block>();
        myAddress = new Address(port, "localhost");
        this.localPeers = new ArrayList<Address>();
        this.INITIAL_CONNECTIONS = initialConnections;
        this.MAX_PEERS = maxPeers;
        try {
            ss = new ServerSocket(port);
            Acceptor acceptor = new Acceptor(this);
            acceptor.start();
            System.out.println("node.Node up and running on port " + port + " " + InetAddress.getLocalHost());
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * If eligible, add a connection to our dynamic list of peers to speak with
     * @param address
     */
    public void establishConnection(Address address){
        synchronized(lock) {
            if (localPeers.size() < MAX_PEERS && !containsAddress(localPeers, address)) {
                localPeers.add(address);
                System.out.println("Added peer: " + address.getPort());

            }
        }
    }

    /**
     * Iterate through a list of peers and attempt to establish a mutual connection
     * with a specified amount of nodes
     * @param globalPeers
     */
    public void requestConnections(ArrayList<Address> globalPeers){
        this.globalPeers = globalPeers;
        try {
            if(globalPeers.size() > 0){
                ClientConnection connect = new ClientConnection(this, globalPeers);
                connect.start();
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the provided address is in the list, otherwise false
     * @param list
     * @param address
     * @return
     */
    public boolean containsAddress(ArrayList<Address> list, Address address){
        synchronized(lock) {
            for (Address existingAddress : list) {
                if (existingAddress.equals(address)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Acceptor is a thread responsible for maintaining the server socket by
     * accepting incoming connection requests, and starting a new ServerConnection
     * thread for each request. Requests terminate in a finite amount of steps, so
     * threads return upon completion.
     */
    class Acceptor extends Thread {
        Node node;

        Acceptor(Node node){
            this.node = node;
        }

        public void run() {
            Socket client;
            while (true) {
                try {
                    client = ss.accept();
                    new ServerConnection(client, node).start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

