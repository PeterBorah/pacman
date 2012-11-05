package pacman.entries.genetics;

import java.util.Random;

public class Ecosystem {

	static final boolean debug = false;
	static int[] seed = {0, 3, -65, 10, 10, -1, 2};
	
	static Random rnd=new Random();
	
	public static void main(String[] args) {
		Candidate[] candidates = new Candidate[10];
		Candidate seedCandidate = new Candidate(seed);
		candidates[0] = seedCandidate;
		for (int i=1; i<10; i++){
			candidates[i] = mutate(candidates[0]);
		}
		int i = 0;
		while(true){
			System.out.println(i++);
			Generation currentGen = new Generation(candidates);
			candidates = getNewCandidates(currentGen, seedCandidate);
		}
	}

	private static Candidate[] getNewCandidates(Generation currentGen, Candidate seedCandidate) {
		Candidate[] newCandidates = new Candidate[10];
		newCandidates[0] = currentGen.top[0];
		for (int i=1; i<3; i++){
			newCandidates[i] = babyAverage(currentGen);
		}
		for (int i=3; i<6; i++){
			newCandidates[i] = babyCross(currentGen);
		}
		for (int i=0; i<3; i++){
			newCandidates[i + 6] = mutate(currentGen.top[i]);
		}
		newCandidates[9] = mutate(seedCandidate);
		return newCandidates;
	}

	private static Candidate babyCross(Generation currentGen) {
		if (debug){System.out.println("cross");}
		Candidate parent1 = currentGen.top[rnd.nextInt(4)];
		Candidate parent2 = parent1;
		int[] baby = new int[7];
		while (parent2 == parent1){
			parent2 = currentGen.top[rnd.nextInt(4)];
		}
		for (int i = 0; i < 7; i++){
			if (rnd.nextInt(2) == 1){
				baby[i] = parent1.values[i];
			}
			else{
				baby[i] = parent2.values[i];
			}
		}
		return new Candidate(baby);
	}

	private static Candidate babyAverage(Generation currentGen) {
		if (debug){System.out.println("average");}
		Candidate parent1 = currentGen.top[rnd.nextInt(4)];
		Candidate parent2 = parent1;
		while (parent2 == parent1){
			parent2 = currentGen.top[rnd.nextInt(4)];
		}
		int[] baby = new int[7];
		for (int i = 0; i < 7; i++){
			baby[i] = (parent1.values[i] + parent2.values[i])/2;
		}
		return new Candidate(baby);
	}

	private static Candidate mutate(Candidate candidate) {
		if (debug){System.out.println("mutate");}
		int[] baby = new int[7];
		for (int i = 0; i < 7; i++){
			if (rnd.nextFloat() > .75){
				baby[i] = candidate.values[i];
			}
			else{
				int randNum = rnd.nextInt(20);
				int step;
				if (i < 4 && i > 1){
					step = 5;
				}
				else{
					step = 1;
				}
				switch (randNum){
				case 0: case 1: case 2: case 3: case 4: case 5: case 6:
					baby[i] = (candidate.values[i] + step);
					break;
				case 7: case 8: case 9: case 10: case 11: case 12: case 13:
					baby[i] = (candidate.values[i] - step);
					break;
				case 14: case 15:
					baby[i] = (candidate.values[i] + (2*step));
					break;
				case 16: case 17:
					baby[i] = (candidate.values[i] - (2*step));
					break;
				case 18:
					baby[i] = (candidate.values[i] + (3*step));
					break;
				case 19:
					baby[i] = (candidate.values[i] - (3*step));
					break;
				}
			}
		}
		return new Candidate(baby);
	}

}
