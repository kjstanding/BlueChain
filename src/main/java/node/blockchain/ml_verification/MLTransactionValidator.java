package node.blockchain.ml_verification;

import node.blockchain.TransactionValidator;

public class MLTransactionValidator extends TransactionValidator {

    @Override
    public boolean validate(Object[] objects) {
        // TODO: Possibly change validator
        return true;
    }
}