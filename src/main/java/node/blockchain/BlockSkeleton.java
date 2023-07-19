package node.blockchain;

import node.communication.BlockSignature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BlockSkeleton implements Serializable{
    private final int blockId;
    private String hash;
    private final ArrayList<String> keys;
    private ArrayList<BlockSignature> signatures;

    // For ML Blocks
    private final HashMap<Integer, Boolean> validatedIntervals;

    public BlockSkeleton (int blockId, ArrayList<String> keys, ArrayList<BlockSignature> signatures,
                          String hash, HashMap<Integer, Boolean> validatedIntervals, boolean isVerified){
        this.keys = keys;
        this.blockId = blockId;
        this.signatures = signatures;
        this.hash = hash;

        // For ML Blocks
        this.validatedIntervals = validatedIntervals;
    }

    public ArrayList<BlockSignature> getSignatures() {return signatures;}
    public int getBlockId(){return blockId;}
    public ArrayList<String> getKeys() {return keys;}
    public String getHash(){return hash;}

    // For ML Blocks
    public HashMap<Integer, Boolean> getValidatedIntervals() { return validatedIntervals; }
}

