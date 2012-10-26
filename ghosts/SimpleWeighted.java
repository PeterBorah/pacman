package pacman.entries.ghosts;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getActions() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.ghosts.mypackage).
 */
public class SimpleWeighted extends Controller<EnumMap<GHOST,MOVE>>
{
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	int PILL_PROXIMITY = 0;
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue){
		for(GHOST ghost : GHOST.values())	//for each ghost
		{			
			if(game.doesGhostRequireAction(ghost))		//if ghost requires an action
			{
				MOVE[] possibilities = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
				Map<MOVE, Integer> weights = new HashMap<MOVE, Integer>(possibilities.length);
				for (MOVE move : possibilities){
					weights.put(move, 0);
				}
				if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game)){
					myMoves.put(ghost, getBlueMove(game, ghost, possibilities, weights));
				}
				
				else{
					myMoves.put(ghost, getColorfulMove(game, ghost, possibilities, weights));
				}
			}
		}
		return myMoves;
	}
	
	private boolean closeToPower(Game game)
    {
    	int[] powerPills=game.getPowerPillIndices();
    	
    	for(int i=0;i<powerPills.length;i++)
    		if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(powerPills[i],game.getPacmanCurrentNodeIndex())<PILL_PROXIMITY)
    			return true;

        return false;
    }
	
	private MOVE getBlueMove(Game game, GHOST ghost, MOVE[] possibilites, Map<MOVE, Integer> weights){
		MOVE pacMove = game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		weights.put(pacMove, weights.get(pacMove) - 100);
		
		MOVE pacFlee = game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		weights.put(pacFlee, weights.get(pacMove) + 30);
		
		for(GHOST ghostFriend : GHOST.values()){
			if (ghostFriend != ghost){
				MOVE friendMove = game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getGhostCurrentNodeIndex(ghostFriend), game.getGhostLastMoveMade(ghost), DM.PATH);
				if (game.getGhostEdibleTime(ghostFriend)>0){
					weights.put(friendMove, weights.get(friendMove) - 30);
				}
				else{
					weights.put(friendMove, weights.get(friendMove) + 20);
				}
			}
		}
		int bestScore = Integer.MIN_VALUE;
		MOVE best = null;
		for(MOVE move : possibilites){
			if (weights.get(move) > bestScore){
				bestScore = weights.get(move);
				best = move;
			}
		}
		
		return best;
	}
	
	private MOVE getColorfulMove(Game game, GHOST ghost, MOVE[] possibilites, Map<MOVE, Integer> weights){
		MOVE pacMove = game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(ghost), DM.PATH);
		weights.put(pacMove, weights.get(pacMove) + 30);
		
		for(GHOST ghostFriend : GHOST.values()){
			if (ghostFriend != ghost){
				MOVE friendMove = game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), game.getGhostCurrentNodeIndex(ghostFriend), game.getGhostLastMoveMade(ghost), DM.PATH);
				if (game.getGhostEdibleTime(ghostFriend)>0){
					weights.put(friendMove, weights.get(friendMove) + 0);
				}
				else{
					weights.put(friendMove, weights.get(friendMove) - 20);
				}
			}
		}
		int bestScore = Integer.MIN_VALUE;
		MOVE best = null;
		for(MOVE move : possibilites){
			if (weights.get(move) > bestScore){
				bestScore = weights.get(move);
				best = move;
			}
		}
		
		return best;
	}
}