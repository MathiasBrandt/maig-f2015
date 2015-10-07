package mfli.behaviortree;

import java.util.HashMap;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class BehaviorTreePacman extends Controller<MOVE> {

	public static HashMap<String, Integer> CONTEXT = new HashMap<String, Integer>();
	public static MOVE NEXT_MOVE = MOVE.NEUTRAL;
	public static GHOST CLOSEST_GHOST = null;
	private Node root;
	private Game gameState;
	
	public BehaviorTreePacman() {
		BehaviorTreePacman.CONTEXT.put(Constants.MIN_GHOST_DISTANCE_BEFORE_FLEE, 40);
		BehaviorTreePacman.CONTEXT.put(Constants.MIN_POWER_PILL_DISTANCE_BEFORE_EAT, 60);
		BehaviorTreePacman.CONTEXT.put(Constants.MIN_GHOST_DISTANCE_BEFORE_ATTACK, 150);
		
		this.root = buildTree();
	}
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		this.gameState = game;
		BehaviorTreePacman.CONTEXT.put(Constants.CURRENT_PLAYER_POSITION, gameState.getPacmanCurrentNodeIndex());
		root.execute();
		
		System.out.println("Next move: " + BehaviorTreePacman.NEXT_MOVE);
		
		return BehaviorTreePacman.NEXT_MOVE;
	}
	
	/**
	 * Builds the behavior tree.
	 * @return The root node of the tree
	 */
	private Node buildTree() {
		Leaf findNearestGhostLeaf = new Leaf(() -> findNearestGhost() >= 0);
		
		Leaf isNearestGhostAThreat = new Leaf(() -> isNearestGhostAThreat());
		Leaf fleeFromNearestGhostLeaf = new Leaf(() -> fleeFromNearestGhost());
		SequenceNode fleeFromNearestGhostSequence = new SequenceNode(isNearestGhostAThreat, fleeFromNearestGhostLeaf);
		
		Leaf isNearestGhostWithinEatingRangeLeaf = new Leaf(() -> isNearestGhostWithinEatingRange());
		Leaf isNearestGhostEdibleLeaf = new Leaf(() -> isNearestGhostEdible());
		Leaf goToNearestGhostLeaf = new Leaf(() -> goToNearestGhost());
		SequenceNode canEatNearestGhostSequence = new SequenceNode(isNearestGhostWithinEatingRangeLeaf, isNearestGhostEdibleLeaf, goToNearestGhostLeaf);
		
		Leaf findLocationOfNearestPowerPillLeaf = new Leaf(() -> findNearestPowerPillLocation() >= 0);
		Leaf findDistanceToNearestPowerPillLeaf = new Leaf(() -> findNearestPowerPillDistance() >= 0);
		Leaf isNearestPowerPillWithinRangeLeaf = new Leaf(() -> isNearestPowerPillWithinRange());
		Leaf goToNearestPowerPillLeaf = new Leaf(() -> goToNearestPowerPill());
		SequenceNode powerPillSequence = new SequenceNode(findLocationOfNearestPowerPillLeaf, findDistanceToNearestPowerPillLeaf, isNearestPowerPillWithinRangeLeaf, goToNearestPowerPillLeaf);
		
		Leaf findLocationOfNearestPillLeaf = new Leaf(() -> findNearestPillLocation() >= 0);
		Leaf findDistanceToNearestPillLeaf = new Leaf(() -> findNearestPillDistance() >= 0);
		Leaf goToNearestPillLeaf = new Leaf(() -> goToNearestPill());
		SequenceNode pillSequence = new SequenceNode(findLocationOfNearestPillLeaf, findDistanceToNearestPillLeaf, goToNearestPillLeaf);
		
		SelectorNode foodTypeSelector = new SelectorNode(powerPillSequence, pillSequence);
		
		SelectorNode fleeAttackOrEatSelector = new SelectorNode(fleeFromNearestGhostSequence, canEatNearestGhostSequence, foodTypeSelector);
		
		SequenceNode root = new SequenceNode(findNearestGhostLeaf, fleeAttackOrEatSelector);
		
		return root;
	}
	
	private boolean isNearestGhostAThreat() {
		System.out.println("IsNearestGhostAThreat");
		
		return BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_GHOST_DISTANCE) <=
				BehaviorTreePacman.CONTEXT.get(Constants.MIN_GHOST_DISTANCE_BEFORE_FLEE);
	}
	
	private boolean isNearestGhostEdible() {
		System.out.println("IsNearestGhostEdible");
		
		return gameState.isGhostEdible(BehaviorTreePacman.CLOSEST_GHOST);
	}
	
	private boolean isNearestGhostWithinEatingRange() {
		return BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_GHOST_DISTANCE) <=
				BehaviorTreePacman.CONTEXT.get(Constants.MIN_GHOST_DISTANCE_BEFORE_ATTACK);
	}
	
	private boolean goToNearestGhost() {
		System.out.println("GoToNearestGhost");
		
		MOVE nextMove = gameState.getNextMoveTowardsTarget(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_GHOST_LOCATION), 
				DM.PATH);
		
		if(nextMove != null) {
			BehaviorTreePacman.NEXT_MOVE = nextMove;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Determine the distance to and location of the nearest out-of-lair ghost.
	 * @return Distance to the nearest ghost. If no ghosts are out of the lair, returns Integer.MAX_VALUE
	 */
	private int findNearestGhost() {
		System.out.println("FindNearestGhost");
		
		int closestDistance = Integer.MAX_VALUE;
		int closestLocation = 0;
		int playerPosition = BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION);
		
		for(GHOST ghost : GHOST.values()) {
			if(gameState.getGhostLairTime(ghost) <= 0) {
				int ghostPosition = gameState.getGhostCurrentNodeIndex(ghost);
				
				int distanceToGhost = gameState.getShortestPathDistance(playerPosition, ghostPosition);
				
				if(distanceToGhost < closestDistance) {
					closestDistance = distanceToGhost;
					closestLocation = ghostPosition;
					BehaviorTreePacman.CLOSEST_GHOST = ghost;
				}
			}
		}
		
		BehaviorTreePacman.CONTEXT.put(Constants.NEAREST_GHOST_DISTANCE, closestDistance);
		BehaviorTreePacman.CONTEXT.put(Constants.NEAREST_GHOST_LOCATION, closestLocation);
		return closestDistance;
	}
	
	/**
	 * Determine the location of the nearest power pill.
	 * @return Location of the nearest power pill
	 */
	private int findNearestPowerPillLocation() {
		System.out.println("FindNearestPowerPillLocation");
		
		int[] activePowerPills = gameState.getActivePowerPillsIndices();
		
		int closestPowerPill = gameState.getClosestNodeIndexFromNodeIndex(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				activePowerPills, 
				DM.PATH);
		
		BehaviorTreePacman.CONTEXT.put(Constants.NEAREST_POWER_PILL_LOCATION, closestPowerPill);
		return closestPowerPill;
	}
	
	/**
	 * Determine the distance to the nearest power pill.
	 * @return Distance to the nearest power pill
	 */
	private int findNearestPowerPillDistance() {
		System.out.println("FindNearestPowerPillDistance");
		
		int distance = gameState.getShortestPathDistance(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_POWER_PILL_LOCATION));
		
		BehaviorTreePacman.CONTEXT.put(Constants.NEAREST_POWER_PILL_DISTANCE, distance);
		return distance;
	}
	
	private boolean isNearestPowerPillWithinRange() {
		return BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_POWER_PILL_DISTANCE) <=
				BehaviorTreePacman.CONTEXT.get(Constants.MIN_POWER_PILL_DISTANCE_BEFORE_EAT);
	}
	
	/**
	 * Determine the path to the nearest power pill.
	 * @return True if a path was found, false otherwise
	 */
	private boolean goToNearestPowerPill() {
		System.out.println("GoToNearestPowerPill");
		
		MOVE nextMove = gameState.getNextMoveTowardsTarget(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_POWER_PILL_LOCATION), 
				DM.PATH);
		
		if(nextMove != null) {
			BehaviorTreePacman.NEXT_MOVE = nextMove;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Determine the location of the nearest pill.
	 * @return The location of the nearest pill
	 */
	private int findNearestPillLocation() {
		System.out.println("FindNearestPillLocation");
		
		int[] activePills = gameState.getActivePillsIndices();
		
		int closestPill = gameState.getClosestNodeIndexFromNodeIndex(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				activePills, 
				DM.PATH);
		
		BehaviorTreePacman.CONTEXT.put(Constants.NEAREST_PILL_LOCATION, closestPill);
		return closestPill;
	}
	
	/**
	 * Determine the distance to the nearest pill.
	 * @return Distance to the nearest pill
	 */
	private int findNearestPillDistance() {
		System.out.println("FindNearestPillDistance");
		
		int distance = gameState.getShortestPathDistance(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_PILL_LOCATION));
		
		BehaviorTreePacman.CONTEXT.put(Constants.NEAREST_PILL_DISTANCE, distance);
		return distance;
	}
	
	/**
	 * Determine the path to the nearest pill.
	 * @return True if a path was found, false otherwise
	 */
	private boolean goToNearestPill() {
		System.out.println("GoToNearestPill");
		
		MOVE nextMove = gameState.getNextMoveTowardsTarget(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_PILL_LOCATION), 
				DM.PATH);
		
		if(nextMove != null) {
			BehaviorTreePacman.NEXT_MOVE = nextMove;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Find an escape route away from the nearest ghost
	 * @return True if an escape route was found, false otherwise
	 */
	private boolean fleeFromNearestGhost() {
		System.out.println("FleeFromNearestGhost");
		
		MOVE nextMove = gameState.getNextMoveAwayFromTarget(
				BehaviorTreePacman.CONTEXT.get(Constants.CURRENT_PLAYER_POSITION), 
				BehaviorTreePacman.CONTEXT.get(Constants.NEAREST_GHOST_LOCATION), 
				DM.PATH);
		
		if(nextMove != null) {
			BehaviorTreePacman.NEXT_MOVE = nextMove;
			return true;
		} else {
			return false;
		}
	}
}
