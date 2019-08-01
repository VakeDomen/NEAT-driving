package game_objects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import helpers.Config;
import neural_network.Genome;
import neural_network.NetworkHandler;

public class Population {

	private ArrayList<Car> population;
	private NetworkHandler nh;
	private double totalFitness = 0.0;
	private Random r;
	
	public Population(Track track, NetworkHandler nh ) {
		this.nh = nh;
		this.population = new ArrayList<Car>();
		this.r = new Random();
		for (int i = 0; i < Config.POPULATION_SIZE; i++) {
			this.population.add(new Car(track, nh.getBaseGenomeWithRandomizedWeights()));
		}
	}

	public void draw(Graphics2D g2d) {
		//draw all the cars in the population
		for(Car car : population) car.draw(g2d);	
		//draw the fittest genome
		nh.draw(g2d);
	}
	

	public void simulateGeneration() {
		nh.selectSpeciesRepresentativesAndClearSpecies();
		simulationStep();
		evaluatePopulation();
		normalizeEvaluations();	
		speciate();
		
		
		nh.listValues();
	}

	
	
	private void simulationStep() {
		
		boolean simulating = true;

		//cycle in generation counter
		int cycle = 0;
		
		while(simulating) {
			simulating = false;

			//determines action for the car
			for(Car car : this.population) car.makeAction();

			//execute that action
			for(Car car : this.population) car.update();

			//check if at least one car still driving
			//if no car is driving proceed to next generation
			for(Car car : this.population) if(car.checkDriving()) simulating = true; 


			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//if cycle cap is reached, stop simulation
			if(cycle++ >= Config.CYCLES_PER_GENERATION) simulating = false;			
		}
	}
	
	private void normalizeEvaluations() {
		double fittest = 0;
		for(Car c : this.population) 
			if(c.getFitnessScore() > fittest) 
				fittest = c.getFitnessScore();
		
		for(Car c : this.population) 
			c.normalizeFitness(fittest);
		
		double fitness = Double.MIN_VALUE;
		for(Car c : this.population) 
			if(c.getFitnessScore() >= fitness) 
				nh.setFittest(c.getGenome());
	}

	private void evaluatePopulation() {
		for(Car car : this.population) this.totalFitness += car.evaluate();
	}

	public void resetTest() {
		for(Car c : this.population) c.testReset();
	}
	
	
	
	private void speciate() {
		for(Car c : this.population) 
			nh.speciate(c);
	}

	public ArrayList<Car> generateOffsprings() {
		ArrayList<Car> offsprings = new ArrayList<>();
		
		offsprings.add(crossover(getParent(), getParent()));
		
		return offsprings;
	}

	private Car crossover(Car parent1, Car parent2) {
		if(parent1.getFitnessScore() > parent2.getFitnessScore())
			return parent1.crossover(parent2);
		else
			return parent2.crossover(parent1);
	}

	private Car getParent() {
		double guess = this.r.nextDouble() * this.totalFitness;
		double tmp = guess;
		for(Car c : this.population) {
			if(c.getFitnessScore() <= tmp) return c;
			else {
				tmp -= c.getFitnessScore();
			}
		}
		return null;
	}
}
