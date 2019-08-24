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
		this.track = track.cloneTrack();
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
		this.pop = new Population(this.track, this.nh, mode, this.simNum);
	}


	public void simulate(int generations, int cyclesPerSimulation) {
		for (int i = 0; i < generations; i++) {
			this.pop.simulateGeneration();
			this.pop.resetPopulation();
		}
	}


	public void resetSimCount() {
		this.simNum = 0;
	}
}
