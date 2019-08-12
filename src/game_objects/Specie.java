package game_objects;

import java.util.ArrayList;
import java.util.Random;

import helpers.Config;
import helpers.VectorHelper;

public class Specie {

	private int id;
	private Car representative;
	private ArrayList<Car> cars;
	private boolean empty;
	
	public Specie(int id, Car representative) {
		this.id = id;
		this.representative = representative;
		this.cars = new ArrayList<>();
		this.empty = false;
	}
	
	
	public Car getRepresentative() {
		return this.representative;
	}


	public void addToSpecie(Car c) {
		this.cars.add(c);
	}


	public void selectRepresentativeAndClearSpecie() {	
		if(this.cars.size() == 1)
			this.representative = this.cars.get(0);
		else
			this.representative = this.cars.get(new Random().nextInt(this.cars.size() - 1));
		this.cars.clear();
	}


	public void selection() {
		
		int toKillOff = 0;
		
		if(VectorHelper.randBool(Config.CHANCE_TO_KILL_IF_SPECIES_ODD)) toKillOff = (int) Math.floor(this.cars.size() * Config.SELECTION_RATIO);
		else toKillOff = (int) Math.ceil(this.cars.size() * Config.SELECTION_RATIO);
		
		this.cars = VectorHelper.bubbleSortCars(this.cars);
		
		if(Config.LOG_SPECIES)
			System.out.println("s: " + this.id + " to kill: " + toKillOff + "\t from: " + this.cars.size());
		
		if(toKillOff > 0) {
			for(int i = 0 ; i < toKillOff ; i++) {
				this.cars.get(i).setToKill(true);
			}
		}
		if(toKillOff >= this.cars.size())
			this.empty = true; 
	}


	public int size() {
		return this.cars.size();
	}


	public String getStatDataString() {
		return this.id + ";" + this.size() + ";" + this.representative.getFitnessScore() + ";" + this.avgFitness() + "\n";
	}
	
	
	private double avgFitness() {
		double avg = 0.;
		for(Car c : this.cars) avg += c.getFitnessScore();
		return avg / this.cars.size();
	}


	public boolean isEmpty() {
		return this.empty;
	}
	
	
	
	
	
	
	
	
	
}
