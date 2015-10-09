/**
 * Mathias Flink Brandt
 * mfli@itu.dk
 */

package mfli.mcts;

import pacman.controllers.Controller;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class MCTSPacman extends Controller<MOVE> {
	private boolean firstNode = true;
	private Simulator simulator;
	private int timeBuffer = 2;
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		MOVE move = mcts(game, timeDue);

		return move;
	}

	/**
	 * Run a Monte Carlo simulation to determine the next move.
	 * @param game The current game state.
	 * @param timeDue Time stamp before which the next move must be returned.
	 * @return Returns the next move.
	 */
	public MOVE mcts(Game game, long timeDue) {
		int simulationCount = 0;
		MOVE nextMove = MOVE.NEUTRAL;

		// game just started, decide on a move
		if (firstNode) {
			firstNode = false;
			
			simulator = new Simulator(game);

			int playerPosition = game.getPacmanCurrentNodeIndex();
			MOVE[] possibleMoves = game.getPossibleMoves(playerPosition);
			nextMove = possibleMoves[1];

			 simulator.playMove(nextMove);
		} else {
			 simulator.updateGameState(game);
		}

		// while we still have time left, run the simulation 
		while (System.currentTimeMillis() < (timeDue - timeBuffer)) {
			simulationCount++;
			simulator.simulate();
		}

		// if we need to make a decision at the current point, get the best move from the simulator
		// else, just keep going
		if(simulator.isAtDecisionPoint()) {
//			System.out.println("At Decision point");
			TreeNode bestMove = simulator.getBestMove();
			nextMove = bestMove.getMove();
			simulator.setRoot(bestMove);
//			System.out.println("Next move: " + nextMove);
		}

//		System.out.println("Simulation count: " + simulationCount);
		
		return nextMove;
	}
}
