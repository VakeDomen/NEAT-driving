package game_objects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;

/*
 * just for display purposes
 *  - color the road for better visibility 
 */
public class Road extends Area{
	
	
	/*
	 * on the first index in walls shoud be the outter-most edge od the polygon
	 */
	public Road(ArrayList<ArrayList<Point>> walls) {
		/*
		 * init outer edge
		 */
		
		Polygon outerEdge = new Polygon();
		outerEdge.npoints = walls.get(0).size();
		outerEdge.xpoints = new int[walls.get(0).size()];
		outerEdge.ypoints = new int[walls.get(0).size()];
		for(int i = 0 ; i < walls.get(0).size() ; i++) {
			outerEdge.xpoints[i] = walls.get(0).get(i).x;
			outerEdge.ypoints[i] = walls.get(0).get(i).y;
		}
		this.add(new Area(outerEdge));
		for(int i = 1 ; i < walls.size() ; i++) {
		
			Polygon tmp = new Polygon();
			tmp.npoints = walls.get(i).size();
			tmp.xpoints = new int[walls.get(i).size()];
			tmp.ypoints = new int[walls.get(i).size()];
			for(int j = 0 ; j < walls.get(i).size() ; j++) {
				tmp.xpoints[j] = walls.get(i).get(j).x;
				tmp.ypoints[j] = walls.get(i).get(j).y;
			}
			this.subtract(new Area(tmp));
			
		}
	}
	

	
	
	public void draw(Graphics2D g2d) {
		g2d.fill(this);
	}
}
