package helpers;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TrackLoader {

	public Point start;
	public ArrayList<ArrayList<Point>> walls;
	public ArrayList<ArrayList<Point>> checkpoints;

	private String trackFileName;
	private BufferedReader br;
	
	
	public TrackLoader(String trackFileName) {
		this.trackFileName = Config.PATH_TRACKS + trackFileName + ".track";
		this.walls = new ArrayList<ArrayList<Point>>();
		this.checkpoints = new ArrayList<ArrayList<Point>>();
	}

	/*
	 * read and parse the track *.txt file
	 */
	public void read() {
		try {

			
			this.br = new BufferedReader(new FileReader(this.trackFileName));
		
			String line = "";
			String section = "";
			
			while((line = br.readLine()) != null) 
			{
				

	            if(line.equals("ST") || line.equals("WL") || line.equals("CP")){
	            	section = line;
	            	continue;
	            }
	            
	            
	            if(section.equals("ST")) {
	            
	            	
	            	this.start = new Point(
	            		Integer.parseInt(line.split(",")[0]),
	            		Integer.parseInt(line.split(",")[1])
	            	);
	            	
	            
	            
	            }else if(section.equals("WL")) {
	            
	            	
	            	String[] wallpoints = line.split(";");
	            	ArrayList<Point> tmp = new ArrayList<Point>(); 
	            	for(String point : wallpoints) {
	            		tmp.add(new Point(
	            			Integer.parseInt(point.split(",")[0]),
	            			Integer.parseInt(point.split(",")[1])
	            		));
	            	}
	            	walls.add(tmp);
	            	
	            	
	            }else if(section.equals("CP")) {
	            	
	            	
	            	String[] checkpointpoints = line.split(";");
	            	ArrayList<Point> tmp = new ArrayList<Point>(); 
	            	for(String point : checkpointpoints) {
	            		tmp.add(new Point(
	            			Integer.parseInt(point.split(",")[0]),
	            			Integer.parseInt(point.split(",")[1])
	            		));
	            	}
	            	checkpoints.add(tmp);
	            	
	            	
	            }
	            
	        }   

	       
	        br.close();   
			

		
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
				
		
		
	}

	/*
	 * adjusting the size (coordinates of walls/checkpoints/starting point) of track to fit the frame
	 */
	public void normalize() {	
		
		int sx = Integer.MAX_VALUE, sy = Integer.MAX_VALUE;
		int mx = Integer.MIN_VALUE, my = Integer.MIN_VALUE;
		//calculate bounds of track
		for(ArrayList<Point> wall : walls) {
			for(Point p : wall) {
				if(p.x < sx) sx = p.x;
				if(p.y < sy) sy = p.y;
				if(p.x > mx) mx = p.x;
				if(p.y > my) my = p.y;
			}
		}
		
		
		//calculate inflater value
		//take the smaller value so the track id not out of bounds (width/height wise)
//		double inflater = (Config.FRAME_WIDTH - (Config.FRAME_TRACK_PADDING * 2)) * 1.0 / (mx - sx) * 1.0;
//		if((my - sy) <= (mx -sx)) {
			System.out.println("Readjusting track inflater...");
			double inflater = (Config.FRAME_HEIGHT  - Config.FRAME_BAR_HEIGHT - (Config.FRAME_TRACK_PADDING * 2)) * 1.0 / (my - sy) * 1.0;
//		}

		
		//adjust start
		this.start.x = (int) (( this.start.x - sx ) * inflater + Config.FRAME_TRACK_PADDING);
		this.start.y = (int) (( this.start.y - sy ) * inflater + Config.FRAME_TRACK_PADDING);
		
		//adjust walls
		for(ArrayList<Point> wall : walls) {
			for(Point p : wall) {
				p.x = (int)(((p.x - sx) * inflater) + Config.FRAME_TRACK_PADDING);
				p.y = (int)(((p.y - sy) * inflater) + Config.FRAME_TRACK_PADDING);
			}
		}
		
		//adjust checkpoints
		for(ArrayList<Point> checkpoint : checkpoints) {
			
			for(Point p : checkpoint) {
				p.x = (int)(((p.x - sx) * inflater) + Config.FRAME_TRACK_PADDING);
				p.y = (int)(((p.y - sy) * inflater) + Config.FRAME_TRACK_PADDING);
			}
		}
	}

}
