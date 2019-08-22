import game_objects.Population;
import game_objects.Track;
import helpers.Config;

public class Main {


	
	public static void main(String[] args) {

		String[] tracks = {
				"medium",
				"medium",
				"hard",
				"hardest"
		};

		//-------------------------------------- track one ------------------------------------

		for(String trackName : tracks){

			Config.TRACK_FILE_NAME = trackName;

			Track track = new Track(Config.TRACK_FILE_NAME);
			SimulationHandler sh = new SimulationHandler(track);

			if(Config.RUN_GUI) {
				Gfx gfx = new Gfx(sh);
			}

			//normal test
			for(int i = 0 ; i < Config.PER_TEST_SIMULATION_COUNT ; i++){
				sh.initSimulation(Population.SimMode.NORMAL);
				sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
			}

			//no explicit fitness sharing test
			sh.resetSimCount();
			for(int i = 0 ; i < Config.PER_TEST_SIMULATION_COUNT ; i++){
				sh.initSimulation(Population.SimMode.NO_EFS);
				sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
			}

			//no speciation test
			sh.resetSimCount();
			for(int i = 0 ; i < Config.PER_TEST_SIMULATION_COUNT ; i++){
				sh.initSimulation(Population.SimMode.NO_SPECIATION);
				sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
			}


		}



		
		
	}
}
