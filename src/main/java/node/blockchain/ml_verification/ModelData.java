package node.blockchain.ml_verification;

import node.blockchain.Transaction;

import java.util.ArrayList;

public class ModelData extends Transaction {
    private final ArrayList<String> snapshotHashes;
    private final String datasetHash;
    private final String modelHash;
    private final boolean isVerified;

    public ModelData(ArrayList<String> snapshotHashes, String datasetHash, String modelHash, boolean isVerified){
        this.snapshotHashes = snapshotHashes;
        this.datasetHash = datasetHash;
        this.modelHash = modelHash;
        this.isVerified = isVerified;
    }

    public boolean isVerified() { return isVerified; }

    @Override
    public String toString() {
        return null;
    }
}
