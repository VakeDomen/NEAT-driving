package game_objects;

import java.util.ArrayList;
import java.util.Random;

public class Specie {

	
	private Car representative;
	private ArrayList<Car> cars;
	
	
	public Specie(Car representative) {
		this.representative = representative;
		this.cars = new ArrayList<>();
		this.cars.add(representative);
	}
	
	
	public Car getRepresentative() {
		return this.representative;
	}


	public void addToSpecie(Car c) {
		this.cars.add(c);
	}


	public void selectRepresentativeAndClearSpecie() {
		this.representative = this.cars.get(new Random().nextInt(this.cars.size()));
		this.cars.clear();
		this.cars.add(this.representative);
	}
	
}
