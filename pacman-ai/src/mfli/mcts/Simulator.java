package mfli.mcts;

import java.util.ArrayList;
import java.util.EnumMap;

import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Simulator {
	private Game game;
	private Game savedGame;
	private Controller<EnumMap<GHOST, MOVE>> ghostController;
	private Controller<MOVE> pacManController;
	private TreeNode root;
	
	public Simulator(Game game) {
		ghostController = new StarterGhosts();
		pacManController = new StarterPacMan();
		this.game = game;
		root = new TreeNode(null, MOVE.NEUTRAL);
	}
	
	public void updateGameState(Game game) {
		this.game = game;
	}
	
	public void simulate() {
		// keep track of which nodes we visit so we can backpropagate score later
		ArrayList<TreeNode> visitedNodes = new ArrayList<TreeNode>();
		
		// save the current game state
		saveGameState();
		
		TreeNode node = root;
		
		// find the next decision point
		playUntilDecisionPoint();
		
		// find out what we should do at this decision point
		// if we're not at a leaf, get the best child and play its move. Then, simulate until next decision point.
		while(!node.isLeaf()) {
			node = node.getBestChild();
			
			playMove(node.getMove());
			playUntilDecisionPoint();
		}
		
		// if we have reached a leaf, we don't know what to do -- so we expand the node to find out
		node.expand(game);
		
		// select the best child of the newly expanded node
		node = node.getBestChild();
		
		// play the next move
		playMove(node.getMove());
		
		// simulate to the end of the game, to determine the score of this move
		double score = simulateEndGame();
		
		// backpropagate the score
		for(TreeNode visitedNode : visitedNodes) {
			visitedNode.updateScore(score);
		}
		
		// reset the game state to what it was before the simulation
		loadGameState();
	}
	
	/**
	 * Simulates a move in the game for both Pac-Man and the ghosts.
	 * @param move The desired move.
	 */
	public void playMove(MOVE move) {
		game.advanceGame(move, ghostController.getMove(game, 0));
	}
	
	/**
	 * Simulate game moves until we reach a decision point.
	 * That is, make Pac-Man go straight ahead.
	 */
	public void playUntilDecisionPoint() {
		MOVE lastMove = game.getPacmanLastMoveMade();
		
		while(!isAtDecisionPoint()) {
			playMove(lastMove);
		}
	}
	
	/**
	 * Checks if Pac-Man is at a decision point in the game.
	 * @return True if Pac-Man is at a junction or going into a wall.
	 */
	public boolean isAtDecisionPoint() {
		int currentPosition = game.getPacmanCurrentNodeIndex();
		
		return game.isJunction(currentPosition) || isAtWall();
	}
	
	/**
	 * Checks if Pac-Man is currently going into a wall.
	 * @return True if Pac-Man will go into a wall by repeating his last move, false otherwise.
	 */
	public boolean isAtWall() {
		int currentPosition = game.getPacmanCurrentNodeIndex();
		MOVE lastMove = game.getPacmanLastMoveMade();
		MOVE[] possibleMoves = game.getPossibleMoves(currentPosition);
		
		// if the last move pac-man made is not in the list of possible moves,
		// we are going into a wall
		for(MOVE move : possibleMoves) {
			if(move == lastMove) {
				return false;
			}
		}
		
		return true;
	}
	
	public TreeNode getBestMove() {
		TreeNode bestChild = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		
		for(TreeNode child : root.getChildren().values()) {
			if(child.getScore() > bestValue) {
				bestChild = child;
				bestValue = child.getScore();
			}
		}
		
		return bestChild;
	}
	
	public double simulateEndGame() {
		int currentLevel = game.getCurrentLevel();
		
		// simulate game until the level changes or pac-man has no lives left
		while(game.getCurrentLevel() == currentLevel && !game.gameOver()) {
			game.advanceGame(pacManController.getMove(game, 0), ghostController.getMove(game, 0));
		}
		
		return game.getScore();
	}
	
	public void saveGameState() {
		savedGame = game;
		game = game.copy();
	}
	
	public void loadGameState() {
		game = savedGame;
	}
	
	public void setRoot(TreeNode root) {
		this.root = root;
	}
}
