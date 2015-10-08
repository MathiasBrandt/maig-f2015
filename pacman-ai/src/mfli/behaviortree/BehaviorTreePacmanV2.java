package mfli.behaviortree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer.ConditionObject;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

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
		MOVE nextMove = MOVE.NEUTRAL;
		
		tree.execute();
		
		nextMove = (MOVE) context.get(Constants.NEXT_MOVE);
		
		return nextMove;
	}
	
	public Node buildTree() {
		Leaf findThreatsLeaf = new Leaf(() -> findThreats());
		Leaf areGhostsEdibleLeaf = new Leaf(() -> areGhostsEdible());
		InverterNode areGhostsEdibleInverter = new InverterNode(areGhostsEdibleLeaf);
		Leaf findEscapeRouteLeaf = new Leaf(() -> findEscapeRoute());
		
		SequenceNode fleeSequence = new SequenceNode(findThreatsLeaf, areGhostsEdibleInverter, findEscapeRouteLeaf);
		
		SelectorNode rootSelector = new SelectorNode(fleeSequence);
		
		return rootSelector;
	}
	
	private boolean findThreats() {
		int pacManPosition = game.getPacmanCurrentNodeIndex();
		ArrayList<GHOST> threateningGhosts = new ArrayList<GHOST>();
		
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostLairTime(ghost) <= 0) {
				int ghostPosition = game.getGhostCurrentNodeIndex(ghost);
				int distanceToGhost = game.getShortestPathDistance(pacManPosition, ghostPosition);
				if(distanceToGhost < params.distance_flee) {
					threateningGhosts.add(ghost);
				}
			}
		}
		
		context.put(Constants.THREATENING_GHOSTS, threateningGhosts);
		
		return threateningGhosts.size() > 0;
	}
	
	private boolean areGhostsEdible() {
		ArrayList<GHOST> threateningGhosts = (ArrayList<GHOST>) context.get(Constants.THREATENING_GHOSTS);
		boolean allEdible = false;
		
		for(GHOST ghost : threateningGhosts) {
			if(!game.isGhostEdible(ghost)) {
				allEdible = false;
				break;
			}
		}
		
		return allEdible;
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
}
