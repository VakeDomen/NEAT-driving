package helpers;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import game_objects.Car;

public class SimThread implements Callable {

	private ArrayList<Car> population;
	
	public SimThread(ArrayList<Car> popChunk) {
		this.population = popChunk;
	}
	
	
	
	
	public ArrayList<Car> getPopulation() {
		return this.population;
	}

	
	public void draw(Graphics2D g2d) {
		for(Car c : this.population)
			c.draw(g2d);
	}
	
	

	@Override
	public ArrayList<Car> call() throws Exception {
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
		return this.population;
	}
	
	
	
}
