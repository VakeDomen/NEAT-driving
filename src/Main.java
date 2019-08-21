import game_objects.Population;
import game_objects.Track;
import helpers.Config;

public class Main {
	
	
	public static void main(String[] args) {
		
		
		Track track = new Track(Config.TRACK_FILE_NAME);
		
		SimulationHandler sh = new SimulationHandler(track);
		
		Gfx gfx;
		if(Config.RUN_GUI)
			gfx = new Gfx(sh);

		for(int i = 0 ; i < 10 ; i++){
			sh.initSimulation(Population.SimMode.NORMAL);
			sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
		}



		
		
	}
}
