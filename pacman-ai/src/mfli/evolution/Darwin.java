package mfli.evolution;

import static pacman.game.Constants.DELAY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import mfli.behaviortree.BehaviorTreePacmanV2;
import mfli.behaviortree.BehaviorTreeParameters;
import pacman.controllers.examples.Legacy;
import pacman.controllers.examples.Legacy2TheReckoning;
import pacman.controllers.examples.StarterGhosts;
import pacman.game.Game;

public class Darwin {
	private static final int POPULATION_SIZE = 40;		// 1000
	private static final int CHROMOSOME_SIZE = 3;		// 3
	private static final int EVALUATION_COUNT = 500;	// 1000
	private static final int RUN_COUNT = 4;				// 4
	private static final int TIME_LIMIT = 2000;
	
	/**
	 * Run the simulation.
	 * 1. Create a base population
	 * 2. Evaluate the current population
	 * 3. Select the best gene from the current population
	 * 4. If the best gene of the current population is the best across all evaluated populations, save it
	 * 5. Select the best half of the current population
	 * 6. Breed the best half of the current population to create a new population
	 * 7. Go to step 2, until @EVALUATION_COUNT has been reached
	 */
	public void runSimulation() {
		Gene bestOverallGene = null;
		int bestOverallFitness = 0;
		ArrayList<Gene> population = createBasePopulation();
		
		int i = 0;
		while(i < EVALUATION_COUNT) {
			System.out.println("Running evaluation " + i + " ...");
			population = evaluatePopulation(population);
			
			Gene bestGeneInPopulation = getBestGene(population);
			System.out.println("Best gene fitness: " + bestGeneInPopulation.getFitness());
			
			if(bestGeneInPopulation.getFitness() > bestOverallFitness) {
				bestOverallFitness = bestGeneInPopulation.getFitness();
				bestOverallGene = bestGeneInPopulation.copy();
				
				System.out.println("A superior gene configuration was discovered");
				System.out.println("Best overall gene fitness: " + bestOverallGene.getFitness());
				System.out.println("Best overall gene config : " + bestOverallGene);
			}
			
			ArrayList<Gene> bestGenesInPopulation = performNaturalSelection(population);
			
			population = breedBestGenes(bestGenesInPopulation);
			
			i++;
		}
		
		System.out.println("Simulation complete.");
		System.out.println("Best Gene: " + bestOverallGene);
		System.out.println("Best Gene Fitness: " + bestOverallGene.getFitness());
	}
	
	/**
	 * Return a population of size @POPULATION_SIZE with random chromosome values.
	 * @return The new population.
	 */
	public ArrayList<Gene> createBasePopulation() {
		Random random = new Random();
		ArrayList<Gene> population = new ArrayList<Gene>();
		
		for(int i = 0; i < POPULATION_SIZE; i++) {
			Gene gene = new Gene(CHROMOSOME_SIZE);
			
			for(int j = 0; j < CHROMOSOME_SIZE; j++) {
				gene.setChromosomeValue(j, random.nextInt(100));
			}
			
			population.add(gene);
		}
		
		return population;
	}
	
	/**
	 * Evaluates the given population.
	 * @param population The population to evaluate.
	 * @return A population with updated fitness scores.
	 */
	public ArrayList<Gene> evaluatePopulation(ArrayList<Gene> population) {
		ArrayList<Gene> newPopulation = new ArrayList<Gene>();
		
		for(int i = 0; i < population.size(); i++) {
			Gene gene = population.get(i);
			
			int score = simulateGame(gene, RUN_COUNT);
			
			gene.setFitness(score);
			
			newPopulation.add(gene);
		}
		
		return newPopulation;
	}
	
	/**
	 * Selects the best half of the given population according to current fitness score.
	 * @param population The population to improve.
	 * @return A population consisting of the best half of the input population.
	 */
	public ArrayList<Gene> performNaturalSelection(ArrayList<Gene> population) {
		ArrayList<Gene> newPopulation = new ArrayList<Gene>(POPULATION_SIZE / 2);
		Collections.sort(population);
		
		for(int i = 0; i < POPULATION_SIZE / 2; i++) {
			newPopulation.add(population.get(i));
		}
		
		return newPopulation;
	}
	
	/**
	 * Breeds every gene in the given population with its neighbour.
	 * @param population The population to breed.
	 * @return A population consisting of the input population and their offspring.
	 */
	public ArrayList<Gene> breedBestGenes(ArrayList<Gene> population) {
		ArrayList<Gene> newPopulation = new ArrayList<Gene>();
		ArrayList<Gene> offspring = new ArrayList<Gene>();
		
		for(int i = 0; i < population.size(); i += 2) {
			Gene gene = population.get(i);
			Gene[] geneOffspring = gene.reproduce(population.get(i + 1));
			
			offspring.add(geneOffspring[0]);
			offspring.add(geneOffspring[1]);
		}
		
		newPopulation.addAll(population);
		newPopulation.addAll(offspring);
		
		return newPopulation;
	}
	
	/**
	 * Simulates a game using the chromosome values from the given gene.
	 * @param gene The gene to evaluate.
	 * @param runCount Number of times the simulation should be run.
	 * @return The average end-of-game score.
	 */
	public int simulateGame(Gene gene, int runCount) {
		Random random = new Random();
		Game game;
		Legacy2TheReckoning ghostsController = new Legacy2TheReckoning();
		BehaviorTreeParameters params = new BehaviorTreeParameters(gene.getChromosome());
		BehaviorTreePacmanV2 pacManController = new BehaviorTreePacmanV2(params);
		int totalScore = 0;
		
		for(int i = 0; i < runCount; i++) {
			game = new Game(random.nextLong());
			
			while(!game.gameOver() && game.getTotalTime() < TIME_LIMIT) {
				game.advanceGame(
						pacManController.getMove(game.copy(), System.currentTimeMillis() + DELAY),
						ghostsController.getMove(game.copy(), System.currentTimeMillis() + DELAY));
			}
			
			int runScore = game.getScore();
			totalScore += runScore;
		}
		
		int averageScore = totalScore / runCount;
		return averageScore;
	}
	
	/**
	 * Gets the best gene in the given population according to best fitness score.
	 * @param population The population in which to find the best gene.
	 * @return The best gene in the population.
	 */
	public Gene getBestGene(ArrayList<Gene> population) {
		Collections.sort(population);
		return population.get(0);
	}
	
	/**
	 * Gets the average fitness score of a population.
	 * @param population The population of which to find the average fitness score.
	 * @return The average fitness score of the population.
	 */
	public int getAverageFitness(ArrayList<Gene> population) {
		int totalFitness = 0;
		
		for(int i = 0; i < population.size(); i++) {
			totalFitness += population.get(i).getFitness();
		}
		
		return totalFitness / population.size();
	}
	
	public static void main(String[] args) {
		Darwin darwin = new Darwin();
		darwin.runSimulation();
	}
}
