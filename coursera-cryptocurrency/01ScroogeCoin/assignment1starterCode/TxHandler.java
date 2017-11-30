import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;

public class TxHandler {
	protected UTXOPool pool; 
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        pool =  new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    		/* conditions (1), (2) & (3) */
    		double spentSum=0;
    		HashSet<UTXO> spentUTXO = new HashSet<>();
        for( int i=0;i<tx.getInputs().size();i++) {
        		Transaction.Input txInput = tx.getInput(i);
        		UTXO utxo = new UTXO(txInput.prevTxHash, txInput.outputIndex);
        		if(!pool.contains(utxo))
        			return false;
        		else if(spentUTXO.contains(utxo))
        			return false;
        		else if (!Crypto.verifySignature(pool.getTxOutput(utxo).address,tx.getRawDataToSign(i), txInput.signature))
        			return false;
        		spentUTXO.add(utxo);
        		spentSum+= pool.getTxOutput(utxo).value;
        }
        /* conditions (4) */
        double createdSum = 0;
        for(Transaction.Output txOutput: tx.getOutputs()) {
        		if(txOutput.value<0)
        			return false;
        		createdSum+= txOutput.value;
        }
        /* conditions (5) */
        return spentSum >= createdSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
		ArrayList<Transaction> acceptedTxns = new ArrayList<>();
    		Transaction addedTxn = null;
    		do {
			addedTxn = addOneTx(possibleTxs);
			acceptedTxns.add(addedTxn);
		}while(addedTxn !=null);
    		return acceptedTxns.toArray(new Transaction[acceptedTxns.size()]);
    }
    public Transaction addOneTx(Transaction[] possibleTxs) {
	    Transaction tx = getTransactionToAdd(possibleTxs);
    		/* remove all consumed coins from UTXOpool*/
    		for(Transaction.Input txInput: tx.getInputs()) {
    			UTXO utxo = new UTXO(txInput.prevTxHash, txInput.outputIndex);
    			pool.removeUTXO(utxo);
    		}
    		
    		/* add all created coins to UTXOpool*/
    		for(int i=0;i<tx.getOutputs().size();i++) {
    			UTXO utxo = new UTXO(tx.getHash(), i);
    			pool.addUTXO(utxo, tx.getOutput(i));
    		}
    		return tx;
    }
    protected Transaction getTransactionToAdd(Transaction[] possibleTxs) {
    	/* find one valid transaction */
		int validIdx = -1;
		for(int i=0;i< possibleTxs.length;i++) {
			if(isValidTx(possibleTxs[i])) {
				validIdx = i;
				break;
			}
		}
		if(validIdx<0)
			return null;
		return possibleTxs[validIdx];
    }

}
