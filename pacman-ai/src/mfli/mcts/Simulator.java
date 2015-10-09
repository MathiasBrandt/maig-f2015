/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.mcts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Stack;

import pacman.controllers.Controller;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Simulator {
	public static final double C_VALUE = 3;
	public static final int PILL_EATEN_BONUS = 20;
	public static final int POWER_PILL_EATEN_BONUS = 100;
	public static final int BLANK_PATH_PENALTY = 100;
	public static final int GHOST_EATEN_BONUS = 500;
	public static final int DEATH_PENALTY = 1000;
	public static final int MIN_GHOST_DISTANCE = 10;
	public static final int GHOST_DISTANCE_PENALTY = 300;
	public static final int LEVEL_COMPLETED_BONUS = 5000;
	
	private Game game;
	private Stack<Game> savedGameStates;
	private Controller<EnumMap<GHOST, MOVE>> ghostController;
	private Controller<MOVE> pacManController;
	private TreeNode root;
	private int maxSimulationCount;
	
	public Simulator(Game game) {
		ghostController = new StarterGhosts();
		pacManController = new StarterPacMan();
		this.game = game;
		root = new TreeNode(null, MOVE.NEUTRAL);
		maxSimulationCount = 1000;
		savedGameStates = new Stack<Game>();
	}
	
	public void updateGameState(Game game) {
		this.game = game;
	}
	
	/**
	 * Run the Monte Carlo simulation.
	 */
	public void simulate() {
		// keep track of which nodes we visit so we can backpropagate score later
		ArrayList<TreeNode> visitedNodes = new ArrayList<TreeNode>();
		
		int lifeCount = game.getPacmanNumberOfLivesRemaining();
		int level = game.getCurrentLevel();
		
		// save the current game state
		saveGameState();
		
		TreeNode node = root;
		
		visitedNodes.add(node);
		
		// find the next decision point
		playUntilDecisionPoint();
		
		// find out what we should do at this decision point
		// if we're not at a leaf, get the best child and play its move. Then, simulate until next decision point.
		while(!node.isLeaf()) {
			node = node.getBestChild();
			
			visitedNodes.add(node);
			
			playMove(node.getMove());
			playUntilDecisionPoint();
		}
		
		// if we have reached a leaf, we don't know what to do -- so we expand the node to find out
		node.expand(game);
		
		// select the best child of the newly expanded node
		node = node.getBestChild();
		
		visitedNodes.add(node);
		
		// play the next move
		playMove(node.getMove());
		
		// simulate to the end of the game, to determine the score of this move
		double score = simulateEndGame(lifeCount, level);
		
		// backpropagate the score
		for(TreeNode visitedNode : visitedNodes) {
			visitedNode.incrementVisitCount();
			visitedNode.updateScore(score);
		}
		
		// reset the game state to what it was before the simulation
		loadGameState();
	}
	
	/**
	 * Simulate the game until it ends by changing level, no more lives remaining of the simulation count is reached.
	 * @param lifeCount Amount of lives left.
	 * @param currentLevel The current level number.
	 * @return Returns the end game score.
	 */
	public double simulateEndGame(int lifeCount, int currentLevel) {		
		int simulationCount = 0;
		int bonus = 0;
		
		// simulate game until the level changes or pac-man has no lives left
		while(simulationCount < maxSimulationCount && game.getCurrentLevel() == currentLevel && !game.gameOver()) {
			game.advanceGame(pacManController.getMove(game, 0), ghostController.getMove(game, 0));
			
			bonus += getStepBonus(lifeCount, currentLevel);
			
			simulationCount++;
		}
		
		double score = game.getScore();
		score += bonus;
		
		return score;
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
	 * That is, make Pac-Man go straight ahead until that's no longer possible.
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
	
	/**
	 * Get the best move from the current root node.
	 * @return Returns the best move.
	 */
	public TreeNode getBestMove() {
		TreeNode bestChild = null;
		double bestValue = Double.NEGATIVE_INFINITY;
		
		for(TreeNode child : root.getChildren()) {
			double childScore = child.getAverageScore();
			
//			System.out.println("Move: " + child.getMove() + ", score: " + childScore);
			
			if(childScore > bestValue) {	// use average instead?
				bestChild = child;
				bestValue = childScore;
			}
		}
		
//		System.out.println("Best move: " + bestChild.getMove());
		return bestChild;
	}
	
	public void saveGameState() {
		savedGameStates.push(game);
		game = game.copy();
	}
	
	public void loadGameState() {
		game = savedGameStates.pop();
	}
	
	public void setRoot(TreeNode root) {
		this.root = root;
	}
	
	/**
	 * Calculates the distance to the specified ghost.
	 */
	public int distanceToGhost(GHOST ghost) {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int ghostPosition = game.getGhostCurrentNodeIndex(ghost);
		
		int distance = game.getShortestPathDistance(pacManPosition, ghostPosition);
		
		return distance;
	}
	
	/**
	 * Calculates the bonus of the most recent step in-game.
	 * @param lifeCount Amount of lives remaining.
	 * @param currentLevel The current level number.
	 * @return Returns the bonus of the most recent step.
	 */
	public int getStepBonus(int lifeCount, int currentLevel) {
		int pathBonus = 0;
		int pillBonus = 0;
		
		if(game.wasPillEaten()) {
			pillBonus = PILL_EATEN_BONUS;
		} else if (game.wasPowerPillEaten()) {
			pillBonus = POWER_PILL_EATEN_BONUS;
		} else {
			pillBonus = BLANK_PATH_PENALTY;
		}
		
		pathBonus += pillBonus;
		
		for(GHOST ghost : GHOST.values()) {
			if(game.wasGhostEaten(ghost)) {
				pathBonus += GHOST_EATEN_BONUS;
			}
			
			if(game.getGhostLairTime(ghost) <= 0) {
				int distanceToGhost = distanceToGhost(ghost);
				if(distanceToGhost < MIN_GHOST_DISTANCE) {
					if(!game.isGhostEdible(ghost)) {
						pathBonus -= GHOST_DISTANCE_PENALTY;
					} else {
						pathBonus += GHOST_DISTANCE_PENALTY;
					}
				}
			}
		}
		
		if(lifeCount > game.getPacmanNumberOfLivesRemaining()) {
			pathBonus -= DEATH_PENALTY;
		}
		
		if(currentLevel < game.getCurrentLevel()) {
			pathBonus += LEVEL_COMPLETED_BONUS;
		}
		
//		System.out.println("STEP BONUS: " + pathBonus);
		
		return pathBonus;
	}
}
