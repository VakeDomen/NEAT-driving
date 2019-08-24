package game_objects;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.BufferedReader;
import java.util.ArrayList;

import helpers.Config;
import helpers.TrackLoader;

public class Track {

	/*
	 * starting/spawn point of the simulated objects
	 */
	private Point start;
	
	/*
	 * track bounds
	 */
	private ArrayList<Wall> walls;
	/*
	 * list of checkpoints (simulated objects get points on passing checkpoints)
	 */
	private ArrayList<Checkpoint> checkpoints;
	/*
	 * track area
	 */
	private Road road;
	
	private TrackLoader tl;

	private int toActivate;
	
	
	public Track(String trackFileName) {
		this.toActivate = 0;
		tl = new TrackLoader(trackFileName);
		tl.read();
		tl.normalize();
		
		this.start = tl.start;
		this.walls = initWalls(tl.walls);
		this.checkpoints = initCheckpointsFromPoints(tl.checkpoints);
		this.road = new Road(tl.walls);
	}

	private Track(Point start, ArrayList<Wall> walls, Road road, TrackLoader tl) {
		this.start = start;
		this.walls = walls;
		this.checkpoints = initCheckpointsFromPoints(tl.checkpoints);
		this.road = road;
		this.tl = tl;
	}
	
	/*
	 * creates checkpoint objects and activates the first set of checkpoints
	 */
	private ArrayList<Checkpoint> initCheckpointsFromPoints(ArrayList<ArrayList<Point>> checkpoints) {
		//ArrayList of checkpoints to return after initiation
		ArrayList<Checkpoint> out = new ArrayList<Checkpoint>();
		
		//crate all checkpoints
		for(ArrayList<Point> cp : checkpoints) 
			out.add(new Checkpoint(cp));

		return initCheckpoints(out);
	}
	private ArrayList<Checkpoint> initCheckpoints(ArrayList<Checkpoint> cps) {

		//deactivate all checkpoints
		for(Checkpoint cp : cps)
			cp.setActive(false);

		//activate the first X active checkpoints
		for (int i = 0; i < Config.ACTIVE_CHECKPOINTS; i++) {
			if(i >= cps.size()) break;

			cps.get(i).setActive(true);

			this.toActivate++;
			if(this.toActivate >= cps.size()){
				this.toActivate = 0;
			}

		}

		return cps;
	}

	private ArrayList<Checkpoint> cloneCheckpoints(ArrayList<Checkpoint> cps){
		ArrayList<Checkpoint> clone = new ArrayList<Checkpoint>();
		for(Checkpoint c : cps)
			clone.add(new Checkpoint(c.getPoints()));
		return clone;
	}

	/*
	 * creates the wall objects
	 */
	private ArrayList<Wall> initWalls(ArrayList<ArrayList<Point>> walls) {
		ArrayList<Wall> out = new ArrayList<Wall>();
		for(ArrayList<Point> w : walls) out.add(new Wall(w));
		return out;
	}



	public void draw(Graphics2D g2d) {
		

		//draw road
		if(Config.DISPLAY_ROAD) {
			g2d.setColor(Color.LIGHT_GRAY);
			road.draw(g2d);
		}
		
		//display walls
		if(Config.DISPLAY_WALLS) {
			g2d.setColor(Color.DARK_GRAY);		
			if(this.walls.size() > 0) for(Wall wall : walls) wall.draw(g2d);
		}
		
		
		
		//display start
		if(Config.DISPLAY_START) {
			g2d.setColor(Color.RED);
			if(start != null) 
				g2d.fillOval(
					this.start.x - Config.START_POINT_SIZE / 2, 
					this.start.y - Config.START_POINT_SIZE / 2, 
					Config.START_POINT_SIZE, 
					Config.START_POINT_SIZE
				);
		}
		
		
		//display all checkpoints
		if(Config.DISPLAY_ALL_CHECKPOINTS) {
			g2d.setColor(Color.YELLOW);
			if(this.walls.size() > 0) for( Checkpoint cp : checkpoints) cp.draw(g2d);
		}

		//display active checkpoints
		if(Config.DISPLAY_ACTIVE_CHECKPOINTS) {
			g2d.setColor(Color.GREEN);
			if(this.walls.size() > 0) for( Checkpoint cp : checkpoints) cp.drawActive(g2d);
		}
		
		
		
	}


	public Point getStart() {
		return this.start;
	}


	public Road getRoad() {
		return this.road;
	}

	public ArrayList<Wall> getWalls(){
		return this.walls;
	}


	public ArrayList<Checkpoint> getCheckpoints() {
		return this.checkpoints;
	}


	public boolean activateNextCheckpoint(Checkpoint c) {
		c.setActive(false);

		this.checkpoints.get(toActivate).setActive(true);
		this.toActivate++;

		if(this.toActivate >= this.checkpoints.size()){
			this.toActivate = 0;
		}

		if(this.checkpoints.indexOf(c) >= this.checkpoints.size() - 1){
			return true;
		}

		return false;



//		for(int i = this.checkpoints.size() - 1 ; i >= 0 ; i--) {
//			if(this.checkpoints.get(i).getActive()) {
//				if(
//					i == this.checkpoints.size() - 1
//					&& this.checkpoints.get(i).getActive()
//				) {
//					this.checkpoints.get(0).setActive(true);
//				} else if(
//					i != this.checkpoints.size() - 1
//					&& !this.checkpoints.get(i + 1).getActive()
//					&& this.checkpoints.get(i).getActive()
//				){
//					this.checkpoints.get(i + 1).setActive(true);
//				}
//			}
//		}
	}


	public void resetCheckpoints() {
		this.checkpoints = initCheckpoints(this.checkpoints);
	}
	

	public Track cloneTrack() {
		return new Track(this.start, this.walls, this.road, this.tl);
	}

	public void displayCheckpoints(Graphics2D g2d) {
		//display all checkpoints
		if(Config.DISPLAY_ALL_CHECKPOINTS) {
			g2d.setColor(Color.YELLOW);
			if(this.walls.size() > 0) for( Checkpoint cp : checkpoints) cp.draw(g2d);
		}

		//display active checkpoints
		g2d.setColor(Color.GREEN);
		if(this.walls.size() > 0) for( Checkpoint cp : checkpoints) cp.drawActive(g2d);
	

	}
	
	
}
