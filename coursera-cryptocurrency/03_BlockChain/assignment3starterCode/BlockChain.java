import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    
    private TransactionPool txnPool;
    private Map<ByteArrayWrapper, UTXOPool>hashToUTXOs ;
    private Map<ByteArrayWrapper, Integer>hashToHeight;
    private Block maxHeightBlock;
    
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
    		txnPool = new TransactionPool();
    		ByteArrayWrapper blockHash = new ByteArrayWrapper(genesisBlock.getHash());
    		maxHeightBlock = genesisBlock; 
    		hashToHeight = new HashMap<ByteArrayWrapper, Integer>();
    		hashToHeight.put(blockHash, 1);
    		UTXOPool utxos= new UTXOPool();
    		utxos.addUTXO(new UTXO(genesisBlock.getCoinbase().getHash(), 0), genesisBlock.getCoinbase().getOutput(0));
    		hashToUTXOs = new HashMap<>();
    		hashToUTXOs.put(blockHash, utxos);

    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
    		return maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
    		return hashToUTXOs.get(new ByteArrayWrapper(maxHeightBlock.getHash()));
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
    		return this.txnPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
    		// return false if this block tries to replace genesis block
    		if(block.getPrevBlockHash() ==null)
    			return false;
    		ByteArrayWrapper prevBlockHash = new ByteArrayWrapper(block.getPrevBlockHash());
    		ByteArrayWrapper BlockHash = new ByteArrayWrapper(block.getHash());

    		// validate that it points to valid block
    		if(! hashToHeight.containsKey(prevBlockHash)) {
    			return false;
    		}
    		TxHandler handler = new TxHandler(hashToUTXOs.get(prevBlockHash));
    		for(Transaction tx: block.getTransactions()) {
    			if( ! handler.isValidTx(tx)){
    				return false;
    			}
    		}
    		// check for double spend across transactions in the block
    		if(hasDoubleSpend(block)) {
    			return false;
    		}
    		// create new UTXOPool for this block
    		int blockHeight = 1+ hashToHeight.get(prevBlockHash);
    		UTXOPool newUTXOs = getNewPool(hashToUTXOs.get(prevBlockHash), block) ;
    		// add entry for the new block in the height & UTXO's map
    		hashToHeight.put(BlockHash, blockHeight);
    		hashToUTXOs.put(BlockHash, newUTXOs);
    		// updateMaxheight block value, remove entries for old blocks if maxheight is increased 
    		if(blockHeight > hashToHeight.get(new ByteArrayWrapper(maxHeightBlock.getHash()))) {
    			maxHeightBlock = block;
    			int minEligibleHeight = blockHeight-CUT_OFF_AGE ;
    			for(Iterator<ByteArrayWrapper> hashIt= hashToHeight.keySet().iterator(); hashIt.hasNext();) {
    				ByteArrayWrapper hash = hashIt.next();
    				if(hashToHeight.get(hash)< minEligibleHeight) {
    					hashIt.remove();
    					hashToUTXOs.remove(hash);
    				}
    			}
    		}
    		return true;
    }
    private boolean hasDoubleSpend(Block block) {
    		HashSet<UTXO> spentUTXOs = new HashSet<>();
	    	for(Transaction tx: block.getTransactions()) {
		    	for( Transaction.Input txInput : tx.getInputs()) {
		    		UTXO utxo = new UTXO(txInput.prevTxHash, txInput.outputIndex);
		    		if(spentUTXOs.contains(utxo))
		    			return true;
		    		spentUTXOs.add(utxo);
		    }
	    	}
	    	return false;
    }
    private UTXOPool getNewPool(UTXOPool origPool, Block block) {
    		UTXOPool blockUTXOs = new UTXOPool(origPool);
    		for(Transaction tx: block.getTransactions()) {
    			// remove UTXOs for consumed inputs
    			for(Transaction.Input input: tx.getInputs()) {
    				UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
    				blockUTXOs.removeUTXO(utxo);
    			}
    			// add UTXOs for outputs
    			for(int i=0;i< tx.getOutputs().size();i++) {
    				UTXO utxo = new UTXO(tx.getHash(), i);
    				blockUTXOs.addUTXO(utxo, tx.getOutput(i));
    			}
    		}
    		// add UTXO for coinbase transaction
    		UTXO utxo = new UTXO(block.getCoinbase().getHash(), 0);
    		blockUTXOs.addUTXO(utxo, block.getCoinbase().getOutput(0));
    		return blockUTXOs;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
    		txnPool.addTransaction(tx);
    }
}