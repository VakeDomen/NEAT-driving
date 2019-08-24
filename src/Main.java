import game_objects.Checkpoint;
import game_objects.Population;
import game_objects.Track;
import helpers.Config;

public class Main {


	
	public static void main(String[] args) {

		if(args.length != 0){
			Config.PATH_LOG = "";
			Config.PATH_TRACKS = "";

			String trackName = null;
			Population.SimMode mode = null;
			Integer simCount = null;
			for(int i = 0 ; i < 3 ; i++){
				if(i == 0) {
					trackName = args[i];
				}else if(i == 1){
					switch (args[i]){
						case "0":
							mode = Population.SimMode.NORMAL;
							break;
						case "1":
							mode = Population.SimMode.NO_EFS;
							break;
						case "2":
							mode = Population.SimMode.NO_SPECIATION;
							break;
						case "3":
							mode = Population.SimMode.NO_SPECIATION_AND_EFS;
							break;
					}


				}else if(i == 2){
					simCount = Integer.parseInt(args[i]);
				}
			}


			if(trackName == null || mode == null || simCount == null) {
				System.out.println("invalid args....running all tests");
				runAllTests();
				System.exit(0);
			}

			Config.TRACK_FILE_NAME = trackName;
			Config.PER_TEST_SIMULATION_COUNT = simCount;


			Track track = new Track(Config.TRACK_FILE_NAME);
			SimulationHandler sh = new SimulationHandler(track);


			for(int i = 0 ; i < Config.PER_TEST_SIMULATION_COUNT ; i++){
				sh.initSimulation(mode);
				sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
			}


		}else{
			runAllTests();
		}
	}





	public static void runAllTests(){
		String[] tracks = {
				"medium",
				"medium",
				"hard",
				"hardest"
		};

		for(String trackName : tracks){

			Config.TRACK_FILE_NAME = trackName;

			Track track = new Track(Config.TRACK_FILE_NAME);
			SimulationHandler sh = new SimulationHandler(track);

			if(Config.RUN_GUI) {
				Gfx gfx = new Gfx(sh);
			}

			//normal test
			for(int i = 0 ; i < Config.PER_TEST_SIMULATION_COUNT ; i++){
				sh.initSimulation(Population.SimMode.NO_EFS);
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

			//no speciation and efs test
			sh.resetSimCount();
			for(int i = 0 ; i < Config.PER_TEST_SIMULATION_COUNT ; i++){
				sh.initSimulation(Population.SimMode.NO_SPECIATION_AND_EFS);
				sh.simulate(Config.GENERATIONS_PER_SIMULATION, Config.CYCLES_PER_GENERATION);
			}


		}

	}
}
