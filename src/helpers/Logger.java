package helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

	private BufferedWriter bw;
	
	public Logger() {
		try {
			this.bw = new BufferedWriter(new FileWriter(Config.LOG_FILE_NAME));
		}catch(Exception e) {
			e.printStackTrace();
		}
		log("GENERATION;MAX_FITNESS;MEAN_FITNESS;SURVIVED_TICKS_FITTEST;SPECIES;MAX_NODE_INOVATION_NUMBER;MAX_CONNECTION_INNOVATION_NUMBER\n");
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
