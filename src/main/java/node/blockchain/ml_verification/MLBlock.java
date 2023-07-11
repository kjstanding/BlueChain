package node.blockchain.ml_verification;

import node.blockchain.Block;
import node.blockchain.Transaction;

import java.util.HashMap;
import java.util.HashSet;

public class MLBlock extends Block {

    public MLBlock(HashMap<String, Transaction> txList, String prevBlockHash, int blockId){
        // Setting variables inherited from Block class
        this.txList = new HashMap<>();
        this.prevBlockHash = prevBlockHash;
        this.blockId = blockId;

        // Converting the transaction from Block to MLTransactions (Model Data)
        HashSet<String> keys = new HashSet<>(txList.keySet());
        for(String key : keys){
            ModelData transactionInList = (ModelData) txList.get(key);
            this.txList.put(key, transactionInList);
        }
    }
}
