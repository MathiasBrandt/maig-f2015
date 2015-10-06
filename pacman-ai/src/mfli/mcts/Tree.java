package mfli.mcts;

public class Tree {
	/**
	 * Traverse the tree to find the next node the should be expanded.
	 * @param start The root of the tree
	 * @return The best node to expand
	 */
	public TreeNode treePolicy(TreeNode start) {
		// if children is null, node has not yet been expanded.
		// I.e., we have reached the best node
		if(start.getChildren() == null) { return start; }
		
		TreeNode bestChild = null;
		
		for(TreeNode child : start.getChildren()) {
			if(bestChild == null) { bestChild = child; }
			
			if(child.getValue() > bestChild.getValue()) {
				bestChild = child;
			}
		}
		
		return treePolicy(bestChild);
	}
	
	public double defaultPolicy() {
		return 0;
	}
}
