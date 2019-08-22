import java.awt.Graphics2D;
import java.util.ArrayList;

import game_objects.Car;
import game_objects.Population;
import game_objects.Track;
import helpers.Config;
import neural_network.NetworkHandler;

public class SimulationHandler {

	
	private Track track;
	private Population pop;
	private NetworkHandler nh;
	private int simNum;
	
	public SimulationHandler(Track track) {
		this.simNum = 0;
		this.track = track;
		this.nh = new NetworkHandler();
	}

	public void draw(Graphics2D g2d) {	
		this.track.draw(g2d);
		if(this.pop != null) this.pop.draw(g2d);
	}
	
	public void initSimulation(Population.SimMode mode) {
		if(Config.LOG_SIM_STATE) {
			System.out.println("Initializing simulation...");
		}
		this.simNum++;
		this.nh = new NetworkHandler();
		this.pop = new Population(track, nh, mode, this.simNum);

	}

	
	private void simulateGeneration() {
		this.pop.simulateGeneration();
	}
	
	private void resetSimulation() {
		//resetPopulation();
		resetTrack();
	}
	
	private void resetTrack() {
		
		this.pop.resetCheckpoints();
	}

	public void simulate(int generations, int cyclesPerSimulation) {
		for (int i = 0; i < generations; i++) {
			simulateGeneration();
			resetSimulation();
		}
	}


	public void resetSimCount() {
		this.simNum = 0;
	}
}
