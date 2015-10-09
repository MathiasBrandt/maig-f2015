package mfli.evolution;

public class Gene implements Comparable<Gene> {
	private int chromosomeSize;
	private int[] chromosome;
	private int fitness;
	
	public Gene(int chromosomeSize) {
		this.chromosomeSize = chromosomeSize;
		this.chromosome = new int[chromosomeSize];
		this.fitness = 0;
	}
	
	public Gene[] reproduce(Gene that) {
		Gene[] offspring = new Gene[2];
		
		// single point cross over
		int crossoverPoint = chromosomeSize / 2;
		
		for(int i = 0; i < crossoverPoint; i++) {
			int temp = getChromosomeValue(i);
			setChromosomeValue(i, that.getChromosomeValue(i));
			that.setChromosomeValue(i, temp);
		}
		
		offspring[0] = this;
		offspring[1] = that;
		
		return offspring;
	}
	
	public void setChromosomeValue(int index, int value) {
		chromosome[index] = value;
	}
	
	public int getChromosomeValue(int index) {
		return chromosome[index];
	}
	
	public void setFitness(int fitness) {
		this.fitness = fitness;
	}
	
	public int getFitness() {
		return fitness;
	}
	
	public int getChromosomeSize() {
		return chromosomeSize;
	}
	
	public int[] getChromosome() {
		return chromosome;
	}
	
	@Override
	public String toString() {
		String s = "[";
		
		for(int i = 0; i < chromosomeSize; i++) {
			s += getChromosomeValue(i) + ";";
		}
		
		s += "]";
		
		return s;
	}

	@Override
	public int compareTo(Gene that) {
		if(this.getFitness() > that.getFitness()) { return -1; }
		if(this.getFitness() < that.getFitness()) { return 2; }
		
		return 0;
	}
	
	
}
