package node.blockchain.ml_verification;

import node.blockchain.Transaction;
import node.communication.utils.Hashing;

public class ModelData extends Transaction {
    private final String snapshotsFilePath;
    private boolean[] intervalStates;

    public boolean[] getIntervalStates() {
        return intervalStates;
    }

    public ModelData(String snapshotsFilePath, String timestamp, boolean[] intervalStates) {
        this.snapshotsFilePath = snapshotsFilePath;
        UID = Hashing.getSHAString(snapshotsFilePath + timestamp);
        this.timestamp = timestamp;
        this.intervalStates = intervalStates;
    }

    public String getSnapshotsFilePath() { return snapshotsFilePath; }

    @Override
    public String toString() { return "FilePath: " + snapshotsFilePath; }
}