package mfli.mcts;

import java.util.HashMap;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class TreeNode {
	private TreeNode parent;
	private HashMap<MOVE, TreeNode> children;
	private MOVE move;
	private double score;
	
	public TreeNode(TreeNode parent, MOVE move) {
		this.parent = parent;
		this.move = move;
	}
	
	public MOVE getMove() {
		return move;
	}
	
	public HashMap<MOVE, TreeNode> getChildren() {
		return children;
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
}
