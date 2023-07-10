package node.blockchain.ml_verification;

import java.util.ArrayList;

public class ModelData {
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
}
