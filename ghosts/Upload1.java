package pacman.entries.ghosts;

import java.awt.Color;
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
public class Upload1 extends Controller<EnumMap<GHOST,MOVE>>
{
	private static final float JITTER = .00f;

	Random rnd=new Random();
	
	Map<GHOST, Integer> goals = new HashMap<GHOST, Integer>(4);
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue){
		Map<GHOST, Integer> ghostLocs = new HashMap<GHOST, Integer>(4);
		for(GHOST ghost : GHOST.values()){
			goals.put(ghost, -3);
			ghostLocs.put(ghost, game.getGhostCurrentNodeIndex(ghost));
		}
		getGoals(game, ghostLocs);
		for(GHOST ghost : GHOST.values()){
			if (goals.get(ghost) == -2){
				myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost),
						game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
			}
			else if (goals.get(ghost) == -1){
				myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
						game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
			}
			else{
				if(rnd.nextFloat()<JITTER){
				MOVE[] possibleMoves=game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost),game.getGhostLastMoveMade(ghost));
				myMoves.put(ghost,possibleMoves[rnd.nextInt(possibleMoves.length)]);
				}
				else{
				myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
						goals.get(ghost),game.getGhostLastMoveMade(ghost),DM.PATH));
				}
			}
		}
		//highlightTargets(game);
		return myMoves;
	}
	
	private void highlightTargets(Game game){
		for(GHOST ghost : GHOST.values()){
			int node = goals.get(ghost);
			if (node > -1){
				GameView.addPoints(game, Color.cyan, node);
			}
			else if (node == -1){
				GameView.addPoints(game, Color.cyan, game.getPacmanCurrentNodeIndex());
			}
			
		}
	}
	
	private boolean closeToPower(Game game)
    {
    	int[] powerPills=game.getPowerPillIndices();
    	
    	for(int i=0;i<powerPills.length;i++)
    		if(game.isPowerPillStillAvailable(i) && game.getShortestPathDistance(powerPills[i],game.getPacmanCurrentNodeIndex())<20)
    			return true;

        return false;
    }
	
	private void getGoals(Game game, Map<GHOST, Integer> ghostLocs){
		int unassigned = 4;
		int pacLoc = game.getPacmanCurrentNodeIndex();
		int[] junctions = game.getJunctionIndices();
		
		for(GHOST ghost : GHOST.values()){
			if (game.getGhostEdibleTime(ghost)>0 || closeToPower(game)){
				goals.put(ghost, -2);
				unassigned--;
			}
			if (unassigned == 0){
				return;
			}
		}
		
		for(GHOST ghost : GHOST.values()){
			boolean close = true;
			if (goals.get(ghost) == -3){
				MOVE lastMove = game.getGhostLastMoveMade(ghost);
				int[] pathToPac = game.getShortestPath(ghostLocs.get(ghost), pacLoc, lastMove);
				int ghosts = 0;
				for (int i = 1; i<pathToPac.length; i++){
					if (ghostLocs.containsValue(pathToPac[i])){
						close = false;
						ghosts += 1;
					}
					else{
						for (int j = 0; j<junctions.length; j++){
							if (pathToPac[i] == junctions[j]){
								close = false;
							}
						}
					}
				}
				if (close){
					goals.put(ghost, -1);
					unassigned--;
				}
			}
		}
		if (unassigned == 0){
			return;
		}
		
		int[] closeJunctions = new int[unassigned];
		int[] cjValues = new int[unassigned];
		
		for (int i = 0; i<unassigned; i++){
			cjValues[i] = Integer.MAX_VALUE;
		}
		
		for (int i = 0; i<junctions.length; i++){
			int distance = game.getShortestPathDistance(pacLoc, junctions[i]);
			int flag = -1;
			for (int j = 0; j<unassigned; j++){
				if (distance < cjValues[j]){
					flag = j;
					break;
				}
			}
			if (flag != -1){
				for (int j = unassigned - 1; j>=0; j--){
					if (j == flag){
						cjValues[j] = distance;
						closeJunctions[j] = junctions[i];
						break;
					}
					else{
						cjValues[j] = cjValues[j-1];
						closeJunctions[j] = closeJunctions[j-1];
					}
				}
			}
		}
		
		for (int i = 0; i<closeJunctions.length; i++){
			GHOST best = null;
			int bestScore = Integer.MAX_VALUE;
			for(GHOST ghost : GHOST.values()){
				if (goals.get(ghost) == -3){
					MOVE lastMove = game.getGhostLastMoveMade(ghost);
					int distance = game.getShortestPathDistance(ghostLocs.get(ghost), closeJunctions[i], lastMove);
					if (distance<bestScore){
						bestScore = distance;
						best = ghost;
					}
				}
				goals.put(best, closeJunctions[i]);
			}
		}
	}	
}
