package game_objects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import helpers.Config;
import helpers.Logger;
import helpers.Logger.Data;
import helpers.SimThread;
import helpers.VectorHelper;
import neural_network.Genome;
import neural_network.NetworkHandler;

public class Population {

	private ArrayList<Car> population;
	private NetworkHandler nh;
	private double totalFitness = 0.0;
	private Random r;
	
	private int generation = 0;
	private SimMode mode;

	private Object popLock = new Object();
	private FutureTask<ArrayList<Car>>[] threads;
	private SimThread[] simThreads;
	private boolean threadsRunning = false;
	private Logger popLogger;
	private Logger specieLogger;
	private Track track;
	private Car fittest;


	private double avgFitness;
	private double middleFitness;
	private double standardDiviation;



	public enum SimMode {
		NORMAL,
		NO_SPECIATION,
		NO_EFS
	}


	public Population(Track track, NetworkHandler nh, SimMode mode, int simNum) {
		this.track = track;
		this.popLogger = new Logger(Data.POP, Config.TRACK_FILE_NAME + "_" + mode + "_" + simNum + "_");
		this.specieLogger = new Logger(Data.SPECIE, Config.TRACK_FILE_NAME + "_" + mode + simNum + "_" );
		this.fittest = null;
		this.avgFitness = 0.;
		this.middleFitness = 0.;
		this.standardDiviation = 0.;
		this.nh = nh;
		this.population = new ArrayList<Car>();
		this.r = new Random();
		for (int i = 0; i < Config.POPULATION_SIZE; i++) {
			this.population.add(new Car(track, nh.getBaseGenomeWithRandomizedWeights()));
		}
		this.mode = mode;


		if (mode == SimMode.NO_SPECIATION)
			Config.COMPATIBILITY_THRESHOLD = 10000;
	}

	public void draw(Graphics2D g2d) {
		synchronized(popLock) {
			//draw all the cars in the population
			
			if(Config.RUN_THREADED) {
				if(threadsRunning) {
					for(SimThread task: simThreads)
						task.draw(g2d);
					
					if(Config.DISPLAY_ONE_CAR_CHECKPOINTS) 
						simThreads[0].getPopulation().get(0).getTrack().displayCheckpoints(g2d);
				}
				
				
			
				
			}else
				for(Car car : population) 
					car.draw(g2d);
			
			
			
		}
		//draw the fittest genome
		nh.draw(g2d);
	}
	

	public void simulateGeneration() {
		this.generation++;
		if(Config.LOG_SIM_STATE)
			System.out.println("---------- GENERATION " + this.generation + " -----------------");
		nh.selectSpeciesRepresentativesAndClearSpecies();
		if(Config.RUN_THREADED) simulationStep_threaded();
		else simulationStep();
		evaluatePopulation();
		speciate();
		if (this.mode != SimMode.NO_EFS) explicitFitnessSharing();
		normalizeEvaluations();	
		selection();
		breed();
		mutate();
		
		
		
		
		nh.setFittest(this.fittest.getGenome());
		if(Config.LOG_SIM_STATE){
			System.out.println("Max fitness: " + this.fittest.getFitnessScore());
		}

		if(Config.LOG_TO_FILE) {
			popLog();
			specieLog();
		}

			
	}


	private void simulationStep_threaded() {
		int popChunkSize = Config.POPULATION_SIZE / Config.THREADS;
		ArrayList<ArrayList<Car>> populationChunks = new ArrayList<>();
		threads = new FutureTask[Config.THREADS];
		simThreads = new SimThread[Config.THREADS];
		
		
		for(int i = 0 ; i < Config.THREADS ; i++)
			populationChunks.add(new ArrayList<Car>());
		
		int threadIndex = -1;
		for(int i = 0 ; i < this.population.size() ; i++) {
			if(i % popChunkSize == 0)
				threadIndex++;
			
			populationChunks.get(threadIndex).add(this.population.get(i));
		}
		
		
		for(int i = 0 ; i < Config.THREADS ; i++) {
			simThreads[i] = new SimThread(populationChunks.get(i));
			threads[i] = new FutureTask(simThreads[i]);
		}
		threadsRunning = true;
		for(int i = 0 ; i < Config.THREADS ; i++)
			new Thread(threads[i]).start();
		
		for(int i = 0 ; i < Config.THREADS ; i++){
			try {
				populationChunks.set(i, threads[i].get());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		threadsRunning = false;
		
		ArrayList<Car> newPop = new ArrayList<Car>();
		for(ArrayList<Car> al : populationChunks) 
			newPop.addAll(al);
		
		this.population = newPop;
		
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

			
			//if cycle cap is reached, stop simulation
			if(cycle++ >= Config.CYCLES_PER_GENERATION) simulating = false;			
		}
	}
	
	private void normalizeEvaluations() {
		if (Config.LOG_SIM_STATE)
			System.out.println("normalizing evaluations");

		for(Car c : this.population) System.out.print(c.getFitness() + "\t|\t");
		System.out.println("normalized");

		double lowest = Double.MAX_VALUE;
		for(Car c : this.population)
			if(c.getFitness() < lowest)
				lowest = c.getFitness();

		if(lowest < 0.)
			for(Car c : this.population)
				c.offsetFitness(lowest * -1.);


		//Normalize whole population by fittest (0->1)
		double f = Double.MIN_VALUE;
		for(Car c : this.population){
			if(c.getFitness() > f){
				f = c.getFitness();
			}
		}

		for (Car c : this.population){
			c.setFitness( c.getFitness() / f);
		}


		this.fittest.setFitness(1.);
	}

	private void evaluatePopulation() {
		double fitness = 0.0;
		for(Car car : this.population) fitness += car.evaluate();

		this.population = VectorHelper.bubbleSortCarsFitness(this.population);

		this.middleFitness = this.population.get((int) Math.floor(this.population.size() / 2)).getFitnessScore();
		this.avgFitness = fitness / this.population.size();
		this.fittest = this.population.get(this.population.size() - 1);
		this.standardDiviation = calcStandardDiviation();

		if((this.fittest.getFitnessScore() / this.population.get(this.population.size() - 2).getFitnessScore()) > 1.) {
			this.fittest.setFitness(this.fittest.getFitness() * ((this.fittest.getFitnessScore() / this.population.get(this.population.size() - 2).getFitnessScore())));
		}
	}

	
	private void speciate() {
		if(Config.LOG_SIM_STATE)
			System.out.println("spiciating");
		for(Car c : this.population) 
			nh.speciate(c);
	}

	
	private void breed() {
		if(Config.LOG_SIM_STATE)
			System.out.println("breeding");
		this.population = generateOffsprings();
	}
	
	private void mutate() {
		if(Config.LOG_SIM_STATE)
			System.out.println("mutating");
		for(Car c : this.population)
			if(!c.isFitest())
				c.mutate();
	}
	
	
	public ArrayList<Car> generateOffsprings() {
		if(Config.LOG_SIM_STATE)
			System.out.println("generating offsprings");
		
		ArrayList<Car> offsprings = new ArrayList<Car>();
		offsprings.add(this.fittest.clone().refresh());

		for(int i = 0 ; i < Config.POPULATION_SIZE - 1 - Config.RANDOM_POPULATION_IN_GENERATION ; i++)
			offsprings.add(crossover(getParent(), getParent()));


		for(int i = 0 ; i < Config.RANDOM_POPULATION_IN_GENERATION ; i++){
			offsprings.add(new Car(this.track, this.nh.getBaseGenomeWithRandomizedWeights()));
		}



		return offsprings;
	}

	private Car crossover(Car parent1, Car parent2) {
		if(parent1.getFitness() >= parent2.getFitness())
			return parent1.crossover(parent2);
		else
			return parent2.crossover(parent1);
	}

	private Car getParent() {
		double totalFitness = calculateTotalFitness();
		double guess = this.r.nextDouble() * totalFitness;
		double tmp = guess;
		
		for(Car c : this.population) {
			if(c.getFitness() >= tmp) return c;
			else {
				tmp -= c.getFitness();
			}
		}
		System.out.println("no parent found");
		return this.population.get(this.population.size() - 1);
	}
	
	private void selection() {
		if(Config.LOG_SIM_STATE)
			System.out.println("selection");
		nh.selection();
		killSelected();
	}
	
	private void killSelected() {
		ArrayList<Car> toDel = new ArrayList<Car>();
		for(Car c : this.population) {
			if(c.getToKill())
				toDel.add(c);
		}

		for(Car c : toDel)
			this.population.remove(c);

		nh.removeExtinctSpecies();
		
	}
	
	private double calculateTotalFitness() {
		double total = 0;
		for(Car c : this.population) {
			total += c.getFitness();
		}
		return total;
	}
	
	private void popLog() {
		String sep = ";";
		this.popLogger.log(this.generation + sep + this.fittest.getFitnessScore() + sep + this.avgFitness + sep + this.middleFitness + sep + this.standardDiviation + sep + this.fittest.getTicksSurvived() + sep + this.nh.getSpecies().size() + sep + this.nh.getConnectionInovation() + sep + this.nh.getNodeInovation() + "\n");
	}
	
	private void specieLog() {
		if(this.generation % Config.LOG_SPECIE_PER_X_GENERATIONS == 0){
			String sep = ";";
			this.specieLogger.log("GEN " + this.generation + "\n");
			this.specieLogger.log("ID;SIZE;REPRESENTATIVE_FITNESS;AVG_FITNESS\n");
			for(Specie s : this.nh.getSpecies())
				this.specieLogger.log(s.getStatDataString());
		}


		
	}

	public void resetCheckpoints() {
		for(Car c : this.population)
			c.getTrack().resetCheckpoints();
	}
	
	private void explicitFitnessSharing() {
		for(Car c : this.population)
			c.setFitness(computeSharedFitnessValue(c));
	}
	
	
	public double computeSharedFitnessValue(Car c){
		double denominator = 1;
	
		for(int j = 0; j < this.population.size(); j++){
			final double dist = c.compatibilityDistance(this.population.get(j));
			if (dist < Config.SHARE_FITNESS_DIST){
				denominator += (1 - (dist / Config.SHARE_FITNESS_VALUE));
			}
		}
		return c.getFitness() / denominator;
	}

	private double calcStandardDiviation(){
		double[] diviations = new double[this.population.size()];
		for(int i = 0 ; i < this.population.size() ; i++){
			diviations[i] = Math.abs(this.fittest.getFitnessScore() - this.population.get(i).getFitnessScore());
		}
		double stdDiv = 0;
		for(double d : diviations)
			stdDiv += d;
		return stdDiv / this.population.size();
	}
}
