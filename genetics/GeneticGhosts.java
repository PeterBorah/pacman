package pacman.entries.genetics;

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

public class GeneticGhosts extends Controller<EnumMap<GHOST,MOVE>>
{
	
	EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	int TOO_CLOSE;
	int BLUE_PACMAN_WEIGHT;
	
	int GHOST_COST;
	int BLUE_BONUS;
	
	int BLUE_BLUE_COST;
	int BLUE_GHOST_BONUS;
	
	int SCALE_GC;
	int SCALE_BB;
	int SCALE_BBC;
	int SCALE_BGB;
	
	Random rnd=new Random();
	private int[] values;
	
	public GeneticGhosts(int[] values){
		this.TOO_CLOSE = 2;
		this.BLUE_PACMAN_WEIGHT = 100;
		
		this.SCALE_GC = values[4];
		this.SCALE_BB = values[5];
		this.SCALE_BBC = values[6];
		this.SCALE_BGB = values[7];
		
		this.values = values;
	}

	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue){
		
		this.GHOST_COST = (int) (values[0] * scale(game, SCALE_GC));
		this.BLUE_BONUS = (int) (values[1] * scale(game, SCALE_BB));
		
		this.BLUE_BLUE_COST = (int) (values[2] * scale(game, SCALE_BBC));
		this.BLUE_GHOST_BONUS = (int) (values[3] * scale(game, SCALE_BGB));
		
		for(GHOST ghost : GHOST.values())
		{			
			if(game.doesGhostRequireAction(ghost))
			{
				MOVE[] possibilities = game.getPossibleMoves(game.getGhostCurrentNodeIndex(ghost), game.getGhostLastMoveMade(ghost));
				
				boolean sameLoc = false;
				
				for (GHOST ghostFriend : GHOST.values()){
					int distance = (game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getGhostCurrentNodeIndex(ghostFriend)));
					if ((ghostFriend != ghost) && distance < TOO_CLOSE && distance >= 0){
						if (game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex()) >= game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghostFriend), game.getPacmanCurrentNodeIndex())){
						sameLoc = true;
						}
					}
				}

				if (sameLoc){
					myMoves.put(ghost,possibilities[rnd.nextInt(possibilities.length)]);
				}
				
				else if(game.getGhostEdibleTime(ghost)>0){
					myMoves.put(ghost, getBlueMove(game, ghost, possibilities));
				}
				
				else{
					myMoves.put(ghost, getColorfulMove(game, ghost, possibilities));
				}
			}
		}
		return myMoves;
	}
	
	private float scale(Game game, int maxScore) {
		float score = game.getScore();
		if (score >= maxScore){
			return 1;
		}
		else{
			return score/maxScore;
		}
	}
	
	private MOVE getBlueMove(Game game, GHOST ghost, MOVE[] possibilities){
		Map<MOVE, Integer> weights = new HashMap<MOVE, Integer>(possibilities.length);
		int myLoc = game.getGhostCurrentNodeIndex(ghost);
		
		
		for (MOVE move : possibilities){
			int neighbor = game.getNeighbour(myLoc, move);
			int pacLoc = game.getPacmanCurrentNodeIndex();
			int score = game.getShortestPathDistance(pacLoc, neighbor)*(BLUE_PACMAN_WEIGHT);
			for (GHOST ghostFriend : GHOST.values()){
				int ghostFriendLoc = game.getGhostCurrentNodeIndex(ghostFriend);
				int distance = game.getShortestPathDistance(pacLoc, ghostFriendLoc);
				if (ghostFriend != ghost && distance > 0){
					int[] path = new int[distance];
					path = game.getShortestPath(pacLoc, ghostFriendLoc);
						for (int node : path){
							if (node == neighbor && game.getGhostEdibleTime(ghostFriend)>0){
								score += BLUE_BLUE_COST;
							}
							else if (node == neighbor){
								score += BLUE_GHOST_BONUS;
						}
					}
				}
			}
			weights.put(move, score);
		}
		
		int bestScore = Integer.MIN_VALUE;
		MOVE best = null;
		for(MOVE move : possibilities){
			if (weights.get(move) > bestScore){
				bestScore = weights.get(move);
				best = move;
			}
		}
		
		return best;
		
	}
		
	private MOVE getColorfulMove(Game game, GHOST ghost, MOVE[] possibilities){

		Map<MOVE, Integer> weights = new HashMap<MOVE, Integer>(possibilities.length);
		int myLoc = game.getGhostCurrentNodeIndex(ghost);
		
		
		for (MOVE move : possibilities){
			int neighbor = game.getNeighbour(myLoc, move);
			int[] path = game.getShortestPath(neighbor, game.getPacmanCurrentNodeIndex(), move);
			int score = path.length;
			for (GHOST ghostFriend : GHOST.values()){
				if (ghostFriend != ghost){
					int ghostFriendLoc = game.getGhostCurrentNodeIndex(ghostFriend);
						for (int node : path){
							if (node == ghostFriendLoc && game.getGhostEdibleTime(ghostFriend)>0){
								score -= BLUE_BONUS;
							}
							else if (node == ghostFriendLoc){
								score -= GHOST_COST;
						}
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