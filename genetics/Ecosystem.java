package pacman.entries.genetics;

import java.util.Random;

public class Ecosystem {

	static final boolean debug = false;
	
	static Random rnd=new Random();
	
	public static void main(String[] args) {
		Candidate[] candidates = new Candidate[20];
		for (int i=0; i<20; i++){
			candidates[i] = randomCandidate();
		}
		int i = 0;
		while(true){
			System.out.println(i++);
			Generation currentGen = new Generation(candidates);
			candidates = getNewCandidates(currentGen);
		}
	}

	private static Candidate[] getNewCandidates(Generation currentGen) {
		Candidate[] newCandidates = new Candidate[20];
		newCandidates[0] = currentGen.top[0];
		newCandidates[1] = currentGen.top[1];
		for (int i=2; i<5; i++){
			newCandidates[i] = babyAverage(currentGen);
		}
		for (int i=5; i<13; i++){
			newCandidates[i] = babyCross(currentGen);
		}
		for (int i=0; i<5; i++){
			newCandidates[i + 12] = mutate(currentGen.top[i]);
		}
		for (int i=17; i<20; i++){
			newCandidates[i] = randomCandidate();
		}
		return newCandidates;
	}
	
	private static Candidate randomCandidate() {
		int[] baby = new int[8];
		for (int i = 0; i < 8; i++){
			if (i<4){
				baby[i] = rnd.nextInt(200) - 100;
			}
			else{
				baby[i] = rnd.nextInt(2000);
			}
		}
		return new Candidate(baby);
	}

	private static Candidate babyCross(Generation currentGen) {
		if (debug){System.out.println("cross");}
		Candidate parent1 = currentGen.top[rnd.nextInt(7)];
		Candidate parent2 = parent1;
		int[] baby = new int[8];
		while (parent2 == parent1){
			parent2 = currentGen.top[rnd.nextInt(7)];
		}
		for (int i = 0; i < 8; i++){
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
		Candidate parent1 = currentGen.top[rnd.nextInt(7)];
		Candidate parent2 = parent1;
		while (parent2 == parent1){
			parent2 = currentGen.top[rnd.nextInt(7)];
		}
		int[] baby = new int[8];
		for (int i = 0; i < 8; i++){
			baby[i] = (parent1.values[i] + parent2.values[i])/2;
		}
		return new Candidate(baby);
	}

	private static Candidate mutate(Candidate candidate) {
		if (debug){System.out.println("mutate");}
		int[] baby = new int[8];
		for (int i = 0; i < 8; i++){
			if (rnd.nextFloat() > .75){
				baby[i] = candidate.values[i];
			}
			else{
				int randNum = rnd.nextInt(20);
				int step;
				if (i < 4){
					step = 5;
				}
				else{
					step = 100;
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
