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
public class Upload2 extends Controller<EnumMap<GHOST,MOVE>>
{
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	int PILL_PROXIMITY = 0;
	int GHOST_COST = 65;
	int BLUE_BONUS = 35;
	Random rnd=new Random();
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue){
		for(GHOST ghost : GHOST.values())	//for each ghost
		{			
			if(game.doesGhostRequireAction(ghost))		//if ghost requires an action
			{
				MOVE[] possibilities = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
				
				boolean sameLoc = false;
				
				for (GHOST ghostFriend : GHOST.values()){
					if (ghostFriend != ghost && game.getGhostCurrentNodeIndex(ghostFriend) == game.getGhostCurrentNodeIndex(ghost)){
						sameLoc = true;
					}
				}

				if (sameLoc){
					myMoves.put(ghost,possibilities[rnd.nextInt(possibilities.length)]);
				}
				
				else if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game)){
					myMoves.put(ghost, getBlueMove(game, ghost, possibilities));
				}
				
				else{
					myMoves.put(ghost, getColorfulMove(game, ghost, possibilities));
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
	
	private MOVE getBlueMove(Game game, GHOST ghost, MOVE[] possibilites){
		return game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH);
	}
	
	private MOVE getColorfulMove(Game game, GHOST ghost, MOVE[] possibilities){

		Map<MOVE, Integer> weights = new HashMap<MOVE, Integer>(possibilities.length);
		int myLoc = game.getGhostCurrentNodeIndex(ghost);
		
		
		for (MOVE move : possibilities){
			int neighbor = game.getNeighbour(myLoc, move);
			int[] path = game.getShortestPath(neighbor, game.getPacmanCurrentNodeIndex(), move);
			int score = path.length;
			for (GHOST ghostFriend : GHOST.values()){
				int ghostFriendLoc = game.getGhostCurrentNodeIndex(ghostFriend);
					for (int node : path){
						if (node == ghostFriendLoc && game.getGhostEdibleTime(ghostFriend)>0){
							score += BLUE_BONUS;
						}
						else if (node == ghostFriendLoc){
							score += GHOST_COST;
					}
				}
			}
			weights.put(move, score);
		}
		
		int bestScore = Integer.MAX_VALUE;
		MOVE best = null;
		for(MOVE move : possibilities){
			if (weights.get(move) < bestScore){
				bestScore = weights.get(move);
				best = move;
			}
		}
		
		return best;
	}
}