package node.blockchain.ml_verification;

import node.blockchain.Block;

public class MLBlock extends Block {
    private final ModelData modelData;

    public MLBlock(ModelData modelData, String prevBlockHash, int blockId){
        this.modelData = modelData;
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;
    }
}
