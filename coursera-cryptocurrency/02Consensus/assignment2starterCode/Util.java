import java.util.HashMap;
import java.util.Set;

public class Util {
	/**
	 * This subroutine prints out statistics on the current set of nodes. It
	 * identifies the most common non-empty proposed list of transactions and
	 * prints out its statistics.
	 * 
	 * It also finds the longest proposed list of transactions and prints its
	 * statistics.
	 * 
	 * It works by creating a hash code over a proposed set of transactions, and
	 * then counts sets with the same hash code. So, in the print out, this hash
	 * code is printed as an identifier of this specific proposed set.
	 * 
	 * 
	 * @param numNodes
	 * @param nodes
	 */
	public static void printStats(int numNodes, Node[] nodes) {
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> lengths = new HashMap<Integer, Integer>();
		int maxCount = 0;
		int maxH = 0;
		int maxI = numNodes;
		int maxL = 0;
		int longestC = 0;
		int longestH = 0;
		int longestI = numNodes;
		int longestL = 0;

		for (int i = 0; i < numNodes; i++) {
			Set<Transaction> transactions = nodes[i].sendToFollowers();
			Integer h = 0;
			for (Transaction tx : transactions) {
				// Hash the list of tx's together
				h ^= tx.id;
			}
			int ct = counts.getOrDefault(h, 0) + 1;
			counts.put(h, ct);
			int l = transactions.size();
			lengths.put(h, l);
			// System.out.format("For node: %d number of tx's = %d h = %h%n", i,
			// l, h);
			if (ct > maxCount && l > 0) {
				maxCount = ct;
				maxH = h;
				maxL = l;
				maxI = i;
			}
			if (l > longestL) {
				longestC = ct;
				longestH = h;
				longestL = l;
				longestI = i;
			}
			longestC = h == longestH ? ct : longestC;
		}
		System.out
				.format("Best consensus is like node: %d for %.0f%% with %d transactions. hash = %h%n",
						maxI, (100.0 * maxCount) / numNodes, maxL, maxH);
		System.out
				.format("Longest consensus is like node: %d for %.0f%% with %d transactions. hash = %h%n",
						longestI, (100.0 * longestC) / numNodes, longestL,
						longestH);

	}

}
