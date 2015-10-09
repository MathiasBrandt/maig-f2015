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
	private static final int POPULATION_SIZE = 30;		// 1000
	private static final int CHROMOSOME_SIZE = 3;		// 3
	private static final int EVALUATION_COUNT = 1000;	// 1000
	private static final int RUN_COUNT = 4;				// 4
	
	private ArrayList<Gene> population;
	int bestFitness = Integer.MIN_VALUE;
	private Gene bestGene = null;
	
	public Darwin(int populationSize, int chromosomeSize) {
		Random random = new Random();
		this.population = new ArrayList<Gene>(populationSize);
		
		for(int i = 0; i < populationSize; i++) {
			Gene gene = new Gene(chromosomeSize);
			
			for(int j = 0; j < chromosomeSize; j++) {
				gene.setChromosomeValue(j, random.nextInt(101));
			}
			
			this.population.add(gene);
		}
	}
	
	public void evaluatePopulation() {
		for(int i = 0; i < population.size(); i++) {
			Gene gene = population.get(i);
			
			BehaviorTreeParameters params = new BehaviorTreeParameters(gene.getChromosome());
			simulateGame(gene, params);
		}
	}
	
	public ArrayList<Gene> performNaturalSelection() {
		Collections.sort(population);

		ArrayList<Gene> bestGenes = new ArrayList<Gene>(POPULATION_SIZE / 2);
		
		for(int i = 0; i < POPULATION_SIZE / 2; i++) {
			bestGenes.add(population.get(i));
		}
		
		System.out.println("Best genes avg fitness: " + getAverageFitness(bestGenes));
		return bestGenes;
	}
	
	public void breedBestGenes(ArrayList<Gene> bestGenes) {
		ArrayList<Gene> allOffspring = new ArrayList<Gene>();
		
		for(int i = 0; i < bestGenes.size() - 1; i += 2) {
			Gene gene = bestGenes.get(i);
			Gene[] offspring = gene.reproduce(bestGenes.get(i+1));
			allOffspring.add(offspring[0]);
			allOffspring.add(offspring[1]);
		}
		
		population = new ArrayList<Gene>();
		population.addAll(bestGenes);
		population.addAll(allOffspring);
	}
	
	public void simulateGame(Gene gene, BehaviorTreeParameters params) {
		Random random = new Random();
		Game game;
//		StarterGhosts ghostsController = new StarterGhosts();
		Legacy2TheReckoning ghostsController = new Legacy2TheReckoning();
//		Legacy ghostsController = new Legacy();
		BehaviorTreePacmanV2 pacManController = new BehaviorTreePacmanV2(params);
		int simulationCount = 0;
		int totalScore = 0;
		
		for(int i = 0; i < RUN_COUNT; i++) {
			game = new Game(random.nextLong());
//			ghostsController = new RandomGhosts();
			
			while(!game.gameOver() && game.getTotalTime() < 2000) {
				game.advanceGame(
						pacManController.getMove(game.copy(), System.currentTimeMillis() + DELAY),
						ghostsController.getMove(game.copy(), System.currentTimeMillis() + DELAY));
				
				simulationCount++;
			}
			
			int runScore = game.getScore();
//			System.out.println("Run score: " + runScore);
//			System.out.println("GO: " + game.gameOver() + ", SC: " + simulationCount);
			totalScore += runScore;
		}
		
		int averageScore = totalScore / RUN_COUNT;
//		System.out.println(averageScore);
		gene.setFitness(averageScore);
	}
	
	public void saveBestGene() {
		for(int i = 0; i < population.size(); i++) {
			if(population.get(i).getFitness() > bestFitness) {
				bestGene = population.get(i);
				bestFitness = population.get(i).getFitness();
			}
		}
	}
	
	public ArrayList<Gene> getPopulation() {
		return population;
	}
	
	public int getPopulationAverageFitness() {
		return getAverageFitness(population);
	}
	
	public int getAverageFitness(ArrayList<Gene> list) {
		int totalFitness = 0;
		
		for(int i = 0; i < list.size(); i++) {
			totalFitness += list.get(i).getFitness();
		}
		
		return totalFitness / list.size();
	}
	
	public Gene getBestGene() {
		return bestGene;
	}
	
	public static void main(String[] args) {
		Darwin darwin = new Darwin(POPULATION_SIZE, CHROMOSOME_SIZE);
		
		for(int i = 0; i < EVALUATION_COUNT; i++) {
			System.out.println("Running evaluation " + i + " ...");
			darwin.evaluatePopulation();
			System.out.println("Average population fitness: " + darwin.getPopulationAverageFitness());
			darwin.saveBestGene();
			ArrayList<Gene> bestGenes = darwin.performNaturalSelection();
			darwin.breedBestGenes(bestGenes);
		}
		
		System.out.println("Final population average fitness: " + darwin.getPopulationAverageFitness());
		Gene bestGene = darwin.getBestGene();
		System.out.println("Best Gene: " + bestGene);
		System.out.println("Best Gene Fitness: " + bestGene.getFitness());
	}
}
