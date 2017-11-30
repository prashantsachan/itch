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
	private static final double holdRoundFraction = 0.4;
	private final int nHold;
	private Set<Transaction> receivedTxns;
	private final int nRounds;
	private int doneRounds;
	private Map<Integer, Set<Transaction>>lastTransmitted;
	private Set<Integer> malNodes;
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    		receivedTxns = new HashSet<>();
    		nRounds = numRounds;
    		doneRounds= 0;
    		lastTransmitted = new HashMap<>();
    		malNodes = new HashSet<>();
    		nHold = (int) Math.round(holdRoundFraction*nRounds);
    }

    public void setFollowees(boolean[] followees) {
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
    		receivedTxns  = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
		return new HashSet<>(receivedTxns);
   }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    		doneRounds++;
    		if(doneRounds ==1) {
    			for(Candidate c: candidates) {
    				if(!lastTransmitted.containsKey(c.sender))
    					lastTransmitted.put(c.sender, new HashSet<>());
    				lastTransmitted.get(c.sender).add(c.tx);
    			}
    		}else if(doneRounds <=  nHold){
    			Map<Integer, Set<Transaction>> txns = groupBySender(candidates);
    			for(Integer sender: txns.keySet()) {
    				if(! txns.get(sender).equals(lastTransmitted.get(sender)))
    					malNodes.add(sender);
    			}
    			// if this is the last Hold Round, add all the transactions from honest nodes to received list
    			if(doneRounds +1 > nHold) {
    				for(Integer sender : lastTransmitted.keySet()) {
    					for(Transaction t: lastTransmitted.get(sender)) {
    						receivedTxns.add(t);
    					}
    				}
    			}
    		}else {
    			for(Candidate c: candidates) {
    				if(!malNodes.contains(c.sender))
    					receivedTxns.add(c.tx);
    			}
    		}
    }
    private Map<Integer, Set<Transaction>> groupBySender(Set<Candidate> candidates){
    		Map<Integer, Set<Transaction>> txns = new HashMap<>();
    		for(Candidate c: candidates) {
    			if(!txns.containsKey(c.sender))
    				txns.put(c.sender, new HashSet<>());
    			txns.get(c.sender).add(c.tx);
    		}
    		return txns;
    }
}
