/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.mcts;

import java.util.Collection;
import java.util.HashMap;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class TreeNode {
	private TreeNode parent;
	private HashMap<MOVE, TreeNode> children;
	private MOVE move;
	private double totalScore;
	private int visitCount;
	
	public TreeNode(TreeNode parent, MOVE move) {
		this.parent = parent;
		this.move = move;
		this.visitCount = 1;
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
	
	/**
	 * Expands the node by adding all possible moves from the current node as children.
	 * @param game The current game state.
	 */
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
	 * Determine the best child based on UCB value.
	 * @return The best child.
	 */
	public TreeNode getBestChild() {
		TreeNode bestChild = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		
		for(TreeNode child : children.values()) {
			double childScore = child.getUCBScore();
			
//			System.out.println(childScore);
			
			if(childScore > bestValue) {
				bestChild = child;
				bestValue = childScore;
			}
		}
		
		return bestChild;
	}
	
	public void updateScore(double score) {
		this.totalScore += score;
		
//		System.out.println(totalScore);
	}
	
	public double getTotalScore() {
		return totalScore;
	}
	
	public double getAverageScore() {
		return totalScore / visitCount;
	}
	
	/**
	 * Calculate the UCB score of the current node.
	 * @return Returns the UCB score.
	 */
	public double getUCBScore() {
		return getAverageScore() + Simulator.C_VALUE * Math.sqrt(2 * Math.log(parent.getVisitCount()) / visitCount);
	}
	
	public void incrementVisitCount() {
		visitCount++;
	}
	
	public int getVisitCount() {
		return visitCount;
	}
}
