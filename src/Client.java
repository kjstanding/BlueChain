import node.communication.Address;
import node.communication.Message;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * One shot client that queries the network's nodes for each nodes' amount of connections
 */
public class Client {
    private final static int MIN_PORT = 8000;
    private final static int NUM_NODES = 100;

    public static void main(String[] args) {

        int port;

        if(args.length > 0){
            try{
                port = Integer.parseInt(args[0]);
                queryPeer(port);
            }catch (NumberFormatException e){
                System.out.println("Expected integer or no arguments");
                System.out.println("Usage: [Node port]");
            }
        }else{
            LinkedList<GraphNode> graphNodes = new LinkedList<>();
            for(int i = 0; i < NUM_NODES; i++){
                port = MIN_PORT + i;
                ArrayList<Address> localPeers = queryPeer(port);
                if(localPeers != null){
                    graphNodes.add(new GraphNode(port, localPeers));
                }
            }
            new Graph(graphNodes);
            int i = 0;
        }
    }

    private static ArrayList<Address> queryPeer(int port){
        try {
            Socket s = new Socket("localhost", port);
            InputStream in = s.getInputStream();
            ObjectInputStream oin = new ObjectInputStream(in);
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.QUERY_PEERS);
            oout.writeObject(message);
            oout.flush();
            Message messageReceived = (Message) oin.readObject();
            ArrayList<?> localPeers = (ArrayList<?>) messageReceived.getMetadata();
            s.close();
            return (ArrayList<Address>) localPeers;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error occurred");
        }
        return null;
    }
}
