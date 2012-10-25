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
public class MyGhosts1 extends Controller<EnumMap<GHOST,MOVE>>
{
	private final static float CONSISTENCY=1.1f;	//attack Ms Pac-Man with this probability
	private final static int PILL_PROXIMITY=15;
	Map<GHOST, Integer> goals = new HashMap<GHOST, Integer>(4);
	Random rnd=new Random();
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	
	public MyGhosts1() {
		for(GHOST ghost : GHOST.values()){
			goals.put(ghost, -2);
	}
		
	}
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{
		int pacmanLoc = game.getPacmanCurrentNodeIndex();
		for(GHOST ghost : GHOST.values()){
			if (game.getGhostEdibleTime(ghost)>0){
				goals.put(ghost, -2);
			}
		}
		for(GHOST ghost : GHOST.values())	//for each ghost
		{			
			int myLoc = game.getGhostCurrentNodeIndex(ghost);
			if(game.doesGhostRequireAction(ghost))		//if ghost requires an action
			{
				if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game)) {	//retreat from Ms Pac-Man if edible or if Ms Pac-Man is close to power pill
					myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost),
							game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
				}
				else 
				{
					if(rnd.nextFloat()<CONSISTENCY)	{		
						myMoves.put(ghost, blockJunction(game, ghost, pacmanLoc, myLoc));
					}
					else									//else take a random legal action (to be less predictable)
					{					
						MOVE[] possibleMoves=game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost),game.getGhostLastMoveMade(ghost));
						myMoves.put(ghost,possibleMoves[rnd.nextInt(possibleMoves.length)]);
					}
				}
			}
		}
		highlightTargets(game);
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
	private MOVE blockJunction(Game game, GHOST ghost, int pacmanLoc, int myLoc){
		MOVE lastMove = game.getGhostLastMoveMade(ghost);
		int[] pathToPac = game.getShortestPath(myLoc, pacmanLoc, lastMove);
		int[] junctions = game.getJunctionIndices();
		int[] ghostLocs = new int[4];
		int num = 0;
		for(GHOST ghostCheck : GHOST.values()){
			ghostLocs[num] = game.getGhostCurrentNodeIndex(ghostCheck);
			num++;
		}
		boolean close = true;
		search:
			for (int i=1;i<pathToPac.length;i++){
				for (int k=0;k<ghostLocs.length;k++){
					if (pathToPac[i] == ghostLocs[k]){
						close = false;
						break search;
				}
				for (int j=0;j<junctions.length;j++){
					if (pathToPac[i] == junctions[j]){
						close = false;
						break search;
					}
				}
			}
		}
		if (close){
			goals.put(ghost, -1);
			return game.getApproximateNextMoveTowardsTarget(myLoc, pacmanLoc, lastMove, DM.PATH);
		}
		int bestTarget = -1;
		int bestValue = Integer.MAX_VALUE;
		
		int myDistance;
		int pacDistance;
		int node = -1;
		for (int i=0;i<junctions.length;i++){
			node = junctions[i];
			myDistance = game.getShortestPathDistance(myLoc, node, lastMove);
			pacDistance = game.getShortestPathDistance(pacmanLoc, node);
			boolean taken = false;
			if (myDistance >= pacDistance && pacDistance < bestValue && !goals.containsValue(node)){
				int[] path = game.getShortestPath(myLoc, node, lastMove);
				for (i=1; i<path.length; i++){
					if (goals.containsValue(path[i])){
						taken = true;
					}
				}
				if (!taken){
					bestValue = pacDistance;
					bestTarget = node;
				}
				
			}
		}
		if (bestTarget >= 0){
		goals.put(ghost, bestTarget);
		return game.getApproximateNextMoveTowardsTarget(myLoc, bestTarget, lastMove, DM.PATH);
			}
		MOVE[] possibleMoves=game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost),game.getGhostLastMoveMade(ghost));
		goals.put(ghost, -2);
		return possibleMoves[rnd.nextInt(possibleMoves.length)];
	}
}