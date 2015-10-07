package mfli.mcts;

import java.util.Collection;
import java.util.HashMap;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class TreeNode {
	private TreeNode parent;
	private HashMap<MOVE, TreeNode> children;
	private MOVE move;
	private double score;
	private int visitCount;
	
	public TreeNode(TreeNode parent, MOVE move) {
		this.parent = parent;
		this.move = move;
	}
	
	public MOVE getMove() {
		return move;
	}
	
	public Collection<TreeNode> getChildren() {
		return children.values();
	}
	
	public TreeNode getParent() {
		return parent;
	}
	
	public boolean isLeaf() {
		return children == null;
	}
	
	public void expand(Game game) {
		children = new HashMap<MOVE, TreeNode>();
		int currentPosition = game.getPacmanCurrentNodeIndex();
		MOVE[] possibleMoves = game.getPossibleMoves(currentPosition);
		
		for(MOVE move : possibleMoves) {
			children.put(move, new TreeNode(this, move));
		}
	}
	
	/**
	 * TreePolicy
	 * @return
	 */
	public TreeNode getBestChild() {
//		return node.getAverageScore() + Math.sqrt(2 * Math.log(node.getParent().getNumberOfVisits()) / node.getNumberOfVisits());
		
		TreeNode bestChild = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		
		for(TreeNode child : children.values()) {
			if(child.getScore() > bestValue) {
				bestChild = child;
				bestValue = child.getScore();
			}
		}
		
		return bestChild;
	}
	
	public void updateScore(double score) {
		this.score += score;
	}
	
	public double getScore() {
		return score;
	}
	
	public void incrementVisitCount() {
		visitCount++;
	}
}
