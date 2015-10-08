package mfli.behaviortree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

@SuppressWarnings("unchecked")
public class BehaviorTreePacmanV2 extends Controller<MOVE> {
	private BehaviorTreeParameters params;
	private HashMap<String, Object> context;
	private Node tree;
	private Game game;

	public BehaviorTreePacmanV2(BehaviorTreeParameters params) {
		this.params = params;
		this.context = new HashMap<String, Object>();

		tree = buildTree();
	}

	public MOVE getMove(Game game, long timeDue) {
		this.game = game;
		context.put(Constants.NEXT_MOVE, MOVE.NEUTRAL);

		tree.execute();

		return (MOVE) context.get(Constants.NEXT_MOVE);
	}

	public Node buildTree() {
		Leaf findThreatsLeaf = new Leaf(() -> findThreats());
		Leaf areThreatsEdibleLeaf = new Leaf(() -> areThreatsEdible());
		InverterNode areThreatsEdibleInverter = new InverterNode(areThreatsEdibleLeaf);
		Leaf findEscapeRouteLeaf = new Leaf(() -> findEscapeRoute());
		SequenceNode fleeSequence = new SequenceNode(findThreatsLeaf, areThreatsEdibleInverter, findEscapeRouteLeaf);

		Leaf findVictimsLeaf = new Leaf(() -> findVictims());
		Leaf areVictimsEdibleLeaf = new Leaf(() -> areVictimsEdible());
		Leaf attackNearestVictimLeaf = new Leaf(() -> attackNearestVictim());
		SequenceNode attackSequence = new SequenceNode(findVictimsLeaf, areVictimsEdibleLeaf, attackNearestVictimLeaf);

		Leaf findNearestPowerPillLeaf = new Leaf(() -> findNearestPowerPill());
		Leaf isPowerPillWithinRangeLeaf = new Leaf(() -> isPowerPillWithinRange());
		Leaf eatPowerPillLeaf = new Leaf(() -> eatPowerPill());
		SequenceNode powerPillsSequence = new SequenceNode(findNearestPowerPillLeaf, isPowerPillWithinRangeLeaf, eatPowerPillLeaf);
		
		Leaf findNearestPillLeaf = new Leaf(() -> findNearestPill());
		Leaf eatPillLeaf = new Leaf(() -> eatPill());
		SequenceNode pillsSequence = new SequenceNode(findNearestPillLeaf, eatPillLeaf);
		
		SelectorNode eatSelector = new SelectorNode(powerPillsSequence, pillsSequence);
		
		SelectorNode rootSelector = new SelectorNode(fleeSequence, attackSequence, eatSelector);

		return rootSelector;
	}

	private boolean findThreats() {
		ArrayList<GHOST> threateningGhosts = findNearbyGhosts(params.distance_flee);

		context.put(Constants.THREATENING_GHOSTS, threateningGhosts);

		return threateningGhosts.size() > 0;
	}

	private boolean areThreatsEdible() {
		ArrayList<GHOST> threateningGhosts = (ArrayList<GHOST>) context.get(Constants.THREATENING_GHOSTS);
		// boolean allEdible = false;

		for(GHOST ghost : threateningGhosts) {
			if(!game.isGhostEdible(ghost)) {
				// allEdible = false;
				// break;
				return false;
			}
		}

		// return allEdible;
		return true;
	}

	private boolean findEscapeRoute() {
		ArrayList<GHOST> threateningGhosts = (ArrayList<GHOST>) context.get(Constants.THREATENING_GHOSTS);
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		MOVE[] possibleMoves = game.getPossibleMoves(pacManPosition);

		for(GHOST ghost : threateningGhosts) {
			int ghostPosition = game.getGhostCurrentNodeIndex(ghost);
			MOVE blockedMove = game.getNextMoveTowardsTarget(pacManPosition, ghostPosition, DM.PATH);

			for(int i = 0; i < possibleMoves.length; i++) {
				if(possibleMoves[i] == blockedMove) {
					possibleMoves[i] = null;
				}
			}
		}

		MOVE nextMove = MOVE.NEUTRAL;

		for(MOVE move : possibleMoves) {
			if(move != null) {
				nextMove = move;
				break;
			}
		}

		context.put(Constants.NEXT_MOVE, nextMove);
		return true;
	}

	private boolean findVictims() {
		ArrayList<GHOST> victims = findNearbyGhosts(params.distance_attack);

		context.put(Constants.VICTIM_GHOSTS, victims);

		return victims.size() > 0;
	}

	private boolean areVictimsEdible() {
		ArrayList<GHOST> victims = (ArrayList<GHOST>) context.get(Constants.VICTIM_GHOSTS);
		
		// boolean allEdible = false;

		for(GHOST ghost : victims) {
			if(!game.isGhostEdible(ghost)) {
				// allEdible = false;
				// break;
				return false;
			}
		}

		// return allEdible;
		return true;
	}

	private boolean attackNearestVictim() {
		ArrayList<GHOST> victims = (ArrayList<GHOST>) context.get(Constants.VICTIM_GHOSTS);
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int distanceToNearestVictim = Integer.MAX_VALUE;
		int nearestGhostPosition = 0;

		for(GHOST ghost : victims) {
			int ghostPosition = game.getGhostCurrentNodeIndex(ghost);
			int distanceToGhost = game.getShortestPathDistance(pacManPosition, ghostPosition);

			if(distanceToGhost < distanceToNearestVictim) {
				distanceToNearestVictim = distanceToGhost;
				nearestGhostPosition = ghostPosition;
			}
		}

		MOVE nextMove = game.getNextMoveTowardsTarget(pacManPosition, nearestGhostPosition, DM.PATH);
		context.put(Constants.NEXT_MOVE, nextMove);
		return true;
	}
	
	private boolean findNearestPowerPill() {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int[] powerPillPositions = game.getActivePowerPillsIndices();
		
		if(game.getNumberOfActivePowerPills() <= 0) {
			return false;
		}
		
		int closestPowerPillPosition = game.getClosestNodeIndexFromNodeIndex(pacManPosition, powerPillPositions, DM.PATH);
		
		context.put(Constants.CLOSEST_POWER_PILL_POSITION, closestPowerPillPosition);
		return true;
	}
	
	private boolean isPowerPillWithinRange() {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int closestPowerPillPosition = (int) context.get(Constants.CLOSEST_POWER_PILL_POSITION);
		
		int distanceToPowerPill = game.getShortestPathDistance(pacManPosition, closestPowerPillPosition);
		
		return distanceToPowerPill < params.distance_eat_power_pill;
	}
	
	private boolean eatPowerPill() {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int closestPowerPillPosition = (int) context.get(Constants.CLOSEST_POWER_PILL_POSITION);
		
		MOVE nextMove = game.getNextMoveTowardsTarget(pacManPosition, closestPowerPillPosition, DM.PATH);
		context.put(Constants.NEXT_MOVE, nextMove);
		return true;
	}
	
	private boolean findNearestPill() {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int[] pills = game.getActivePillsIndices();
		
		if(game.getNumberOfActivePills() <= 0) {
			return false;
		}
		
		int closestPillPosition = game.getClosestNodeIndexFromNodeIndex(pacManPosition, pills, DM.PATH);
		
		context.put(Constants.CLOSEST_PILL_POSITION, closestPillPosition);
		return true;
	}
	
	private boolean eatPill() {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		int closestPillPosition = (int) context.get(Constants.CLOSEST_PILL_POSITION);
		
		MOVE nextMove = game.getNextMoveTowardsTarget(pacManPosition, closestPillPosition, DM.PATH);
		context.put(Constants.NEXT_MOVE, nextMove);
		return true;
	}

	private ArrayList<GHOST> findNearbyGhosts(int minimumDistance) {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		ArrayList<GHOST> nearbyGhosts = new ArrayList<GHOST>();

		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostLairTime(ghost) <= 0) {
				int ghostPosition = game.getGhostCurrentNodeIndex(ghost);
				int distanceToGhost = game.getShortestPathDistance(pacManPosition, ghostPosition);
					
				if(distanceToGhost < minimumDistance) {
					nearbyGhosts.add(ghost);
				}
			}
		}

		return nearbyGhosts;
	}
}
