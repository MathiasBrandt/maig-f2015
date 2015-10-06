package mfli.mcts;

import java.util.ArrayList;

public class TreeNode {
	private ArrayList<TreeNode> children;
	private TreeNode parent;
	private double value;
	
	public TreeNode(TreeNode parent) {
		this.parent = parent;
	}
	
	/**
	 * Expands the node by going in all possible directions until reaching new junctions.
	 */
	public void expand() {
		// expand current node by going in all possible directions until reaching new junctions.
		// create new nodes for each junction.
		// add new nodes as children.
		
	}
	
	public void backpropagate() {
		// update own q value based on children's values
		double sum = value;
		int childCount = 0;
		if(children != null) {
			for(TreeNode child : children) {
				sum += child.getValue();
			}
			
			childCount = children.size();
		}
		
		value = sum / childCount + 1;
		
		if(parent != null) {
			parent.backpropagate();
		}
 	}
	
	public double getValue() {
		return value;
	}
	
	public ArrayList<TreeNode> getChildren() {
		return children;
	}
	
	public boolean isLeaf() {
		return children == null;
	}
}
