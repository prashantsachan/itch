import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	private Set<Transaction> receivedTxns;
	private Set<Transaction> acceptedTxns;
	private HashMap<Transaction, Set<Integer>> txnSenders;
	private int minNbrCount;
	private int doneRounds;
	private final int nRounds;
//	private f
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    		receivedTxns = new HashSet<>();
    		acceptedTxns = new HashSet<>();
    		txnSenders = new HashMap<>();
    		doneRounds=0;
    		nRounds = numRounds;
    }

    public void setFollowees(boolean[] followees) {
    		int count=0;
    		for(boolean f:followees)
    			count+=f?1:0;
    		minNbrCount= (int) Math.round(count*0.5);
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
    		acceptedTxns  = new HashSet<>(pendingTransactions);
    		receivedTxns  = new HashSet<>(pendingTransactions);

    }

    public Set<Transaction> sendToFollowers() {
    		if(doneRounds < nRounds)
    			return new HashSet<>(receivedTxns);
    		else {
    			System.out.println("sending final txns");
    			return acceptedTxns;
    		}
   }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    		doneRounds++;
    		for(Candidate c: candidates) {
    			if(acceptedTxns.contains(c.tx))
    				continue;
    			if(!txnSenders.containsKey(c.tx))
    				txnSenders.put(c.tx, new HashSet<>());
    			txnSenders.get(c.tx).add(c.sender);
    			receivedTxns.add(c.tx);
    		}
    		for (Transaction t: txnSenders.keySet()) {
    			if(txnSenders.get(t).size()>=minNbrCount) {
    				acceptedTxns.add(t);
    			}
    		}

   }
}
