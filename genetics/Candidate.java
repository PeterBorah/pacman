package pacman.entries.genetics;

import static pacman.game.Constants.DELAY;

import java.util.Random;

import pacman.game.Game;
import pacman.game.Constants.MOVE;

import pacman.controllers.Controller;
import pacman.controllers.examples.BSPacMan;
import pacman.controllers.examples.BaconPacMan;
import pacman.controllers.examples.StarterPacMan;

public class Candidate implements Comparable<Candidate> {
	public int[] values;
	public double score;
	private GeneticGhosts controller;
	private static int TRIALS = 100;

	public Candidate(int[] values){
		this.values = values;
		this.controller = new GeneticGhosts(this.values);
		this.score = score();
	}

	private double score() {
    	double score=0;
    	score += runExperiment(new BaconPacMan(), TRIALS);
    	score += runExperiment(new BSPacMan(), TRIALS/2);
    	score += runExperiment(new StarterPacMan(), TRIALS);
    	return score;
	}
	
	private double runExperiment(Controller<MOVE> pacManController, int trials){
		double avgScore=0;
    	
    	Random rnd=new Random(0);
		Game game;
		
		for(int i=0;i<trials;i++)
		{
			game=new Game(rnd.nextLong());
			
			while(!game.gameOver())
			{
		        game.advanceGame(pacManController.getMove(game.copy(),System.currentTimeMillis()+DELAY),
		        		this.controller.getMove(game.copy(),System.currentTimeMillis()+DELAY));
			}
			
			avgScore+=game.getScore();
		}
    return avgScore/trials;
	}
	@Override
	public int compareTo(Candidate candidate) {
		return (int) (this.score - candidate.score);
	}
}
