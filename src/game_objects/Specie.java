package game_objects;

import java.util.ArrayList;
import java.util.Random;

import helpers.Config;
import helpers.VectorHelper;

public class Specie {

	
	private Car representative;
	private ArrayList<Car> cars;
	
	
	public Specie(Car representative) {
		this.representative = representative;
		this.cars = new ArrayList<>();
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
		this.cars.add(this.representative);
	}


	public void selection() {
		
		int toKillOff = 0;
		if(VectorHelper.randBool(Config.CHANCE_TO_KILL_IF_SPECIES_ODD)) toKillOff = (int) Math.floor(this.cars.size() * Config.SELECTION_RATIO);
		else toKillOff = (int) Math.ceil(this.cars.size() * Config.SELECTION_RATIO);
		
		this.cars = VectorHelper.bubbleSortCars(this.cars);
		
		if(Config.LOG_SPECIES)
			System.out.println("to kill: " + toKillOff + "\t from: " + this.cars.size());
		
		if(toKillOff > 0) {
			for(int i = 0 ; i < toKillOff ; i++) {
				this.cars.get(i).setToKill(true);
			}
		}
		
	}


	public int size() {
		return this.cars.size();
	}
	
	
	
	
	
	
	
}
