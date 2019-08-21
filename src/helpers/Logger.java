package helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	private BufferedWriter bw;
	private Data data;
	private String identifier;
	
	public enum Data {
			POP,
			SPECIE
	}
	
	
	public Logger(Data d, String identifier) {
		this.data = d;
		this.identifier = identifier;
		initFile();
	}


	//TODO: standarnda diviacija
	
	private void initFile() {
		if(data == Data.POP) {
			try {
				this.bw = new BufferedWriter(new FileWriter(this.identifier + "_" + Config.LOG_POP_DATA_FILE_NAME ));
			}catch(Exception e) {
				e.printStackTrace();
			}
			log("GENERATION;MAX_FITNESS;AVG_FITNESS;MIDDLE_FITNESS;STD_DIVIATION;SURVIVED_TICKS_FITTEST;SPECIES;MAX_NODE_INOVATION_NUMBER;MAX_CONNECTION_INNOVATION_NUMBER\n");
		}else if(data == Data.SPECIE) {
			try {
				this.bw = new BufferedWriter(new FileWriter(this.identifier + "_" + Config.LOG_SPECIE_DATA_FILE_NAME));
			}catch(Exception e) {
				e.printStackTrace();
			}
			//log("GENERATION;NUM_OF_SPECIES")
		}
		
	}
	
	
	public void log(String s) {
		try {
			this.bw.write(s);
			this.bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
