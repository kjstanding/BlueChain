package node.blockchain.ml_verification;

import node.blockchain.defi.Account;
import node.blockchain.defi.DefiTransaction;
import node.blockchain.merkletree.MerkleTreeProof;
import node.communication.Address;
import node.communication.messaging.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Pattern;

public class Client {
    BufferedReader reader; // To read user input
    ArrayList<Account> accounts; // Our DeFi account list
    ServerSocket ss;
    Address myAddress;
    ArrayList<Address> fullNodes; // List of full nodes we want to use
    HashSet<DefiTransaction> seenTransactions; // Transactions we've seen from full nodes
    Object updateLock; // Lock for multithreading
    boolean test; // Boolean for test vs normal output

    public Client (int port) {
        fullNodes = new ArrayList<>();
        reader = new BufferedReader(new InputStreamReader(System.in));
        accounts = new ArrayList<>();
        seenTransactions = new HashSet<>();
        updateLock = new Object();

        boolean boundToPort = false;
        int portBindingAttempts = 10; // Amount of attempts to bind to a port
        int fullNodeDefaultAmount = 3; // Full nodes we will try to connect to by default

        String path = "./src/main/java/node/nodeRegistry/";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        /* Iterate through each file in the nodeRegistry dir in order to derive our full nodes dynamically */
        for (File listOfFile : listOfFiles) {
            if (fullNodes.size() >= fullNodeDefaultAmount) break; // We have enough full nodes, no need to continue

            // Make sure each item is in fact a file, isn't the special '.keep' file
            if (listOfFile.isFile() && !listOfFile.getName().contains("keep")) {
                /* Extracting address from file name */
                String[] addressStrings = listOfFile.getName().split("_");
                String hostname = addressStrings[0];
                String[] portString = addressStrings[1].split((Pattern.quote(".")));
                int fullNodePort = Integer.parseInt(portString[0]);
                fullNodes.add(new Address(fullNodePort, hostname));
            }
        }

        /* Binding to our Server Socket so full nodes can hit us up */
        try {
            ss = new ServerSocket(port);
            boundToPort = true;
        } catch (IOException e) {
            for(int i = 1; i < portBindingAttempts; i++){ // We will try several attempts to find a port we can bind too
                try {
                    ss = new ServerSocket(port - i);
                    boundToPort = true;
                    port = port - i;
                }
                catch (IOException ignored) {}
            }
        }

        if(!boundToPort){
            System.out.println("Specify a new port in args[0]");
            System.exit(1);
        }

        InetAddress ip;
        try { ip = InetAddress.getLocalHost(); }
        catch (UnknownHostException e) { throw new RuntimeException(e); }

        String host = ip.getHostAddress();
        myAddress = new Address(port, host);

        System.out.println("Client bound to " + myAddress);

        if(!this.test) System.out.println("Full Nodes to connect to by default: \n" + fullNodes +
                "\nTo update Full Nodes address use 'u' command. \nUse 'h' command for full list of options");

        Client.Acceptor acceptor = new Client.Acceptor(this);
        acceptor.start();
    }

    public static void main(String[] args) throws IOException{

        System.out.println("============ BlueChain NN Verification Client =============");

        BufferedReader mainReader = new BufferedReader(new InputStreamReader(System.in));

        // Reading data using readLine
        String input;
        int port = 7999;
        if(args.length > 0){
            if(args[0].equals("-port")){
                port = Integer.parseInt(args[0]);
            }
            else if(args[0].equals("-test")){
                Client wallet = new Client(port);
                wallet.test = true;
                wallet.testNetwork(Integer.valueOf(args[1]));
                System.exit(0); // We just test then exit
            }
        }

        Client client = new Client(port);
        client.test = false; // This is not a test

        while (true) {
            System.out.print(">");
            input = mainReader.readLine();
            client.interpretInput(input);
        }
    }

    /**
     * Interpret the string input
     *
     * @param input the string to interpret
     */
    public void interpretInput(String input){
        try {
            switch(input){
                case("t"):
                    submitModel();
                    break;
                case("u"):
                    updateFullNode();
                    break;
                case("h"):
                    printUsage();
                    break;
            }
        } catch (IOException e) {
            System.out.println("Input malformed. Try again.");
        }
    }

    public void updateFullNode() throws IOException{
        System.out.println("Updating Full Nodes. \nAdd or remove? ('a' or 'r'): ");
        String response = reader.readLine();
        if(response.equals("a")){
            System.out.println("Full Node host?: ");
            String hostname = reader.readLine();
            System.out.println("Full Node port?: ");
            String port = reader.readLine();
            fullNodes.add(new Address(Integer.parseInt(port), hostname));
        }else if(response.equals("r")){
            System.out.println("Full Node index to remove?: \n" + fullNodes);
            int index = Integer.parseInt(reader.readLine());
            if(index > fullNodes.size()){
                System.out.println("Index not in range.");
                return;
            }

            Address removedAddress = fullNodes.remove(index);
            System.out.println("Removed full node: " + removedAddress);
        }else{
            System.out.println("Invalid option");
        }
    }

    public void submitModel() throws IOException{
        // hardcoded filepaths and interval validity
        ArrayList<String> cleanModelFiles = new ArrayList<>();
        ArrayList<String> randomPoisonedModelFiles = new ArrayList<>();
        ArrayList<String> groupedPoisonedModelFiles = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            cleanModelFiles.add("/Users/kjstanding/Projects/MNIST_NeuralNetwork/clean_model_snapshots/model_" + i);
            if (i < 5) {
                randomPoisonedModelFiles
                        .add("/Users/kjstanding/Projects/MNIST_NeuralNetwork/random_poisoned_model_snapshots/model_" + i);
                groupedPoisonedModelFiles
                        .add("/Users/kjstanding/Projects/MNIST_NeuralNetwork/grouped_poisoned_model_snapshots/model_" + i);
            }
        }

        ArrayList<int[]> randomPoisonedIntervals = new ArrayList<>();
        randomPoisonedIntervals.add(0, new int[]{8, 11, 9, 12, 7});
        randomPoisonedIntervals.add(1, new int[]{9, 5, 17, 11, 12});
        randomPoisonedIntervals.add(2, new int[]{10, 8, 1, 6, 3});
        randomPoisonedIntervals.add(3, new int[]{18, 17, 2, 1, 3});
        randomPoisonedIntervals.add(4, new int[]{15, 7, 6, 10, 17});
        ArrayList<int[]> groupedPoisonedIntervals = new ArrayList<>();
        groupedPoisonedIntervals.add(0, new int[]{0, 1, 2, 3, 4});
        groupedPoisonedIntervals.add(1, new int[]{15, 16, 17, 18, 19});
        groupedPoisonedIntervals.add(2, new int[]{10, 11, 12, 13, 14});
        groupedPoisonedIntervals.add(3, new int[]{10, 11, 12, 13, 14});
        groupedPoisonedIntervals.add(4, new int[]{10, 11, 12, 13, 14});

        System.out.println("Collecting model data");
        System.out.println("Clean, random poisoned, or grouped poisoned? ('c', 'r', or 'g'): ");
        String modelType = reader.readLine();

        System.out.println("Which model? (0-9) for clean. (0-4) for poisoned: ");
        int indexString = Integer.parseInt(reader.readLine());

        String modelSnapshotsFilePath;
        switch (modelType) {
            case "c":
                modelSnapshotsFilePath = cleanModelFiles.get(indexString);
                break;
            case "r":
                modelSnapshotsFilePath = randomPoisonedModelFiles.get(indexString);
                break;
            case "g":
                modelSnapshotsFilePath = groupedPoisonedModelFiles.get(indexString);
                break;
            default:
                System.out.println("Invalid option");
                return;
        }

        boolean[] intervalValidity = new boolean[20];
        Arrays.fill(intervalValidity, true);
        if (Objects.equals(modelType, "r") || Objects.equals(modelType, "g")) {
            int[] indexes = (Objects.equals(modelType, "r")) ? randomPoisonedIntervals.get(indexString)
                    : groupedPoisonedIntervals.get(indexString);
            for (int index : indexes) {
                intervalValidity[index] = false;
            }
        }

        ModelData modelData = new ModelData(modelSnapshotsFilePath, String.valueOf(System.currentTimeMillis()), intervalValidity);

        System.out.println("Submitting model to nodes: ");
        for(Address address : fullNodes){
            submitModel(modelData, address);
        }
    }

    private void submitModel(ModelData modelData, Address address){
        try {
            Socket s = new Socket(address.getHost(), address.getPort());
            OutputStream out = s.getOutputStream();
            ObjectOutputStream oOut = new ObjectOutputStream(out);
            Message message = new Message(Message.Request.ADD_TRANSACTION, modelData);
            oOut.writeObject(message);
            oOut.flush();
            Thread.sleep(1000);
            s.close();
            if (!this.test) System.out.println("Full node: " + address);
        } catch (IOException e) {
            System.out.println("Full node at " + address + " appears down.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void printUsage(){
        System.out.println("BlueChain NN Verification Client Usage:");
        System.out.println("t: Create a transaction");
        System.out.println("u: Update full nodes");
    }

    private void testNetwork(Integer integer) {}

    class Acceptor extends Thread {
        Client client;

        Acceptor(Client client) { this.client = client; }

        public void run() {
            Socket client;
            while (true) {
                try {
                    client = ss.accept();
                    InputStream in = client.getInputStream();
                    ObjectInputStream oin = new ObjectInputStream(in);
                    Message incomingMessage = (Message) oin.readObject();

                    if(incomingMessage.getRequest().name().equals("ALERT_WALLET")){
                        MerkleTreeProof mtp = (MerkleTreeProof) incomingMessage.getMetadata();
                        // updateAccounts(mtp);
                    }
                } catch (IOException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
