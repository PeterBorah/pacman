package pacman.entries.genetics;

import java.util.Arrays;

public class Generation {

	public Candidate[] top;
	private Candidate[] candidates;

	public Generation(Candidate[] candidates) {
		this.candidates = candidates;
		this.top = candidates.clone();
		Arrays.sort(this.top);
		if (this.top[0] != this.candidates[0]){
			System.out.print(this.top[0].score);
			System.out.print(": ");
			System.out.println(Arrays.toString(this.top[0].values));
		}
	}

}
