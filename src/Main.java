import game_objects.Population;
import game_objects.Track;
import helpers.Config;

public class Main {
	
	
	public static void main(String[] args) {



		//-------------------------------------- track one ------------------------------------

		Config.TRACK_FILE_NAME = "track_2.txt";
		Track track = new Track(Config.TRACK_FILE_NAME);
		SimulationHandler sh = new SimulationHandler(track);

		if(Config.RUN_GUI) {
			Gfx gfx = new Gfx(sh);
		}

		for(int i = 0 ; i < Config.SIMULATION_COUNT ; i++){
			sh.initSimulation(Population.SimMode.NORMAL);
			sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
		}
		//normal test
		for(int i = 0 ; i < Config.SIMULATION_COUNT ; i++){
			sh.initSimulation(Population.SimMode.NORMAL);
			sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
		}
		//no explicit fitness sharing test

		//no speciation test
		for(int i = 0 ; i < Config.SIMULATION_COUNT ; i++){
			sh.initSimulation(Population.SimMode.NO_SPECIATION);
			sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
		}


		
		
	}
}
