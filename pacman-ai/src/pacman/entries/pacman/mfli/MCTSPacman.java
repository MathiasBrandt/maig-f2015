package pacman.entries.pacman.mfli;

import mfli.mcts.Tree;
import mfli.mcts.TreeNode;
import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MCTSPacman extends Controller<MOVE> {

	@Override
	public MOVE getMove(Game game, long timeDue) {
		

		return null;
	}
	
	public void mctsSearch(Game gameState) {
		/**
		 *   1.    Select node to expand
		 *   2.    Expand node
		 *   2.1    Foreach possible direction
		 *   2.1.1   Simulate game until next junction by copying game state and stuff
		 *   2.1.2   Create new TreeNode representing the junction
		 *   2.1.3   Add TreeNode as child
		 *   3     Simulate game from randomly chosen child
		 *   4     Backpropagate result to root
		 */
		
		
//		TreeNode root = new TreeNode(null);
		
		// while there is time left
//		while(true) {
//			TreeNode nodeToExpand = treePolicy(root);
//			nodeToExpand.expand();
//			
//			double reward = defaultPolicy();
//			nodeToExpand.backpropagate();
//		}
	}

}
