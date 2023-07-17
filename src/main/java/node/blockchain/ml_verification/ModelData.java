package node.blockchain.ml_verification;

import node.blockchain.Transaction;
import node.communication.utils.Hashing;

public class ModelData extends Transaction {
    private final String snapshotsFilePath;
    private final boolean[] intervalsValidity;

    public ModelData(String snapshotsFilePath, String timestamp, boolean[] intervalStates) {
        this.snapshotsFilePath = snapshotsFilePath;
        this.intervalsValidity = intervalStates;
        this.timestamp = timestamp;
        UID = Hashing.getSHAString(snapshotsFilePath + timestamp);
    }

    public String getSnapshotsFilePath() { return snapshotsFilePath; }
    public boolean[] getIntervalsValidity() { return intervalsValidity; }

    @Override
    public String toString() { return "FilePath: " + snapshotsFilePath; }
}