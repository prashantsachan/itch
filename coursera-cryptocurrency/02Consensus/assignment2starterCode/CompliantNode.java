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
	private static final double contFactor = 0.2;
	private Set<Transaction> receivedTxns;
	private HashMap<Transaction, Set<Integer>> txnSenders;
	private double graphFactor;
	private int acceptThreshold;
	private final int nRounds;
	private int doneRounds;
	private HashMap<Integer, Map<Transaction, Integer>> contReports;
	
	
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    		receivedTxns = new HashSet<>();
    		txnSenders = new HashMap<>();
    		graphFactor = 1/(1+10*p_malicious) ; // varies from [0.6 - 1.0] * (0.0- 1.0) =>  (0.0 to 1.0]
    		nRounds = numRounds;
    		doneRounds= 0;
    		contReports = new HashMap<>();
    }

    public void setFollowees(boolean[] followees) {
    		int count = 0;
    		for(boolean f : followees)
    			count+= f?1:0;
    		acceptThreshold = (int) Math.round(graphFactor * count);
    		System.out.printf("graphFactor: %3f, count: %d, threshold: %d nodes %n", graphFactor,count, acceptThreshold);
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
    		receivedTxns  = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
    		if(doneRounds < nRounds)
    			return new HashSet<>(receivedTxns);
    		else {
//    			System.out.println("sending final set of txns");
    			HashSet<Transaction> finalSet = new HashSet<>();
    			for(Transaction t: txnSenders.keySet()) {
//    				if(txnSenders.get(t).size()>= acceptThreshold)
    					finalSet.add(t);
    			}
    			return finalSet;
    		}
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    		doneRounds++;
    		// every followee's reports in this round
    		HashMap<Integer, Set<Transaction>> curReports = new HashMap<>();
    		for (Candidate c: candidates) {
    			if(!contReports.containsKey(c.sender))
    				contReports.put(c.sender, new HashMap<>());
    			HashMap<Transaction, Integer> m = (HashMap<Transaction, Integer>) contReports.get(c.sender);
    			if(!m.containsKey(c.tx))
    				m.put(c.tx, 1);
    			else
    				m.put(c.tx, m.get(c.tx)+1);
    			if(!curReports.containsKey(c.sender))
    				curReports.put(c.sender, new HashSet<>());
    			curReports.get(c.sender).add(c.tx);
    			
    		}
    		// remove transaction from the contReports if not reported in this round
    		for(Iterator<Entry<Integer, Map<Transaction, Integer>>> senderIt =  contReports.entrySet().iterator();senderIt.hasNext();) {
    			Entry<Integer, Map<Transaction, Integer>> senderEntry = senderIt.next();
    			Integer sender  = senderEntry.getKey();
    			if(!curReports.containsKey(sender)) 
    				senderIt.remove();
    			else
    				for(Iterator<Entry<Transaction, Integer>> it = senderEntry.getValue().entrySet().iterator();it.hasNext();) {
    					Transaction t = it.next().getKey();
    					if(!curReports.get(sender).contains(t))
    						it.remove();
    				}
    		}
    		// accept the transactions which are reported continuously for more than a specific rounds
    		for(Integer sender: contReports.keySet()) {
    			for(Transaction t: contReports.get(sender).keySet()) {
    				if(contReports.get(sender).get(t)>= nRounds*contFactor) {
    					if(!txnSenders.containsKey(t) ) {
    						txnSenders.put(t, new HashSet<>(Arrays.asList(sender)));
    					}else {
    						txnSenders.get(t).add(sender);
    					}
    					receivedTxns.add(t);
    				}
    			}
    		}
			
    		
    }
}
