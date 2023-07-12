package node.blockchain.ml_verification;

import node.blockchain.Transaction;

import java.util.ArrayList;

public class ModelData extends Transaction {
    // Hashes of model training data
    private ArrayList<String> snapshotHashes;
    private final String datasetHash = null;
    private final String modelHash = null;

    private final String snapshotsFilePath;

    private boolean isVerified;

    public ModelData(String snapshotsFilePath) {
        this.snapshotsFilePath = snapshotsFilePath;
        this.isVerified = false;
    }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public ArrayList<String> getSnapshotHashes() { return snapshotHashes; }
    public void setSnapshotHashes(ArrayList<String> snapshotHashes) { this.snapshotHashes = snapshotHashes; }

    public String getSnapshotsFilePath() { return snapshotsFilePath; }

    @Override
    public String toString() {
        return null;
    }
}
