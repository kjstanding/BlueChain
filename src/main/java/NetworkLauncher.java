import node.Node;
import node.communication.Address;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Launches a network given specified configurations
 */
public class NetworkLauncher {
    /* Make a list of the entirety of each node's address */
    private static final ArrayList<Address> globalPeers = new ArrayList<>();

    public static void main(String[] args) {
        String usage = "Usage: NetworkLauncher " +
                "[-o <see options>] [-t <TimedWaitDelayMilliseconds>]" +
                "\n Options:" +
                "\n -o <myNodesStartingPort> <myNodesEndingPort> <otherSubNetStartingPort> <otherSubNetEndingPort> <otherSubNetHostName> ..." +
                "\n     Specifies information regarding other subnets of nodes. " +
                "\n     First we specify our range of port for localhost, then list other subnets." +
                "\n     Total number of nodes must be under the specified amount in config.properties" +
                "\n     No limit for number of subnets one can reasonably specify" +
                "\n\n -t <TimedWaitDelayMilliseconds>" +
                "\n     Specifies the time for a subnet to wait before seeking out connections. " +
                "\n     Useful to allow all subnets to bind to their ports before connecting" +
                "\n\n Default: NetworkLauncher will launch number of nodes specified in config.properties " +
                "\n on localhost with no other scope of nodes";
        try {
            /* Grab values from config file */
            String configFilePath = "src/main/java/config.properties";
            FileInputStream fileInputStream = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(fileInputStream);

            int numNodes = Integer.parseInt(prop.getProperty("NUM_NODES"));
            int maxConnections = Integer.parseInt(prop.getProperty("MAX_CONNECTIONS"));
            int minConnections = Integer.parseInt(prop.getProperty("MIN_CONNECTIONS"));
            int startingPort = Integer.parseInt(prop.getProperty("STARTING_PORT"));
            int quorumSize = Integer.parseInt(prop.getProperty("QUORUM"));
            int minimumTransactions = Integer.parseInt(prop.getProperty("MINIMUM_TRANSACTIONS"));
            float percentMalicious = Float.parseFloat(prop.getProperty("PERCENT_MALICIOUS"));
            int debugLevel = Integer.parseInt(prop.getProperty("DEBUG_LEVEL"));
            String use = prop.getProperty("USE");

            int timedWaitDelay = 0;
            if (args.length > 0 && args[0].equals("-t")) timedWaitDelay = Integer.parseInt(args[1]);

            int numMaliciousNodes = (int) Math.ceil(numNodes * percentMalicious);

            // List of node objects for the launcher to start
            ArrayList<Node> nodes = new ArrayList<>();
            for (int i = startingPort; i < startingPort + numNodes; i++) {
                if (nodes.size() < numMaliciousNodes)
                    nodes.add(new Node(use, i, maxConnections, minConnections, numNodes,
                            quorumSize, minimumTransactions, debugLevel, true));
                else
                    nodes.add(new Node(use, i, maxConnections, minConnections, numNodes,
                            quorumSize, minimumTransactions, debugLevel, false));
            }

            try {
                Thread.sleep(timedWaitDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            StringTokenizer st;
            String path = "./src/main/java/node/nodeRegistry/";
            File folder = new File(path);
            File[] registeredNodes = folder.listFiles();
            assert registeredNodes != null;

            for (File registeredNode : registeredNodes) {
                String name = registeredNode.getName();

                if (!name.contains("keep")) {
                    st = new StringTokenizer(name, "_");
                    String host = st.nextToken();
                    int port = Integer.parseInt(st.nextToken().replaceFirst(".txt", ""));
                    globalPeers.add(new Address(port, host));
                }
            }       

            NetworkLauncher n = new NetworkLauncher();
            n.startNetworkClients(globalPeers, nodes); // Begins network connections

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e){
            System.out.println("Error: args formatted incorrect" + e);
            System.out.println(usage);
        }
    }

    /* Gives each node a thread to start node connections */
    public void startNetworkClients(ArrayList<Address> globalPeers, ArrayList<Node> nodes){
        for (Node node : nodes) {
            new NodeLauncher(node, globalPeers).start();
        }
    }

    /**
     * Thread which is assigned to start a single node within the NetworkLaunchers managed nodes
     */
    static class NodeLauncher extends Thread {
        Node node;
        ArrayList<Address> globalPeers;

        NodeLauncher(Node node, ArrayList<Address> globalPeers) {
            this.node = node;
            this.globalPeers = globalPeers;
        }

        public void run() { node.requestConnections(globalPeers); }
    }
}
