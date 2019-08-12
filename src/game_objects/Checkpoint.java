package game_objects;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.util.ArrayList;

public class Checkpoint implements Cloneable {

	private ArrayList<Point> checkpoint;
	private boolean active;
	
	public Checkpoint(ArrayList<Point> cp) {
		this.checkpoint = cp;
		this.active = false;
	}

	public void drawActive(Graphics2D g2d) {
		if(active) g2d.drawLine(checkpoint.get(0).x, checkpoint.get(0).y, checkpoint.get(1).x, checkpoint.get(1).y);
	}
	

	public void draw(Graphics2D g2d) {
		g2d.drawLine(checkpoint.get(0).x, checkpoint.get(0).y, checkpoint.get(1).x, checkpoint.get(1).y);
	}

	
	
	public void setActive(boolean b) {
		this.active = b;
	}
	public boolean getActive() {
		return this.active;
	}

	public boolean intersects(Line2D.Double move) {
		if(!this.active) return false;
		
		Line2D.Double cp = new Line2D.Double(
				checkpoint.get(0),
				checkpoint.get(1)
		);
		if(cp.contains(move.getP1())) return true;
		if(cp.contains(move.getP2())) return true;
		
		return cp.intersectsLine(move);
	}

	
	public Checkpoint clone() {
		return new Checkpoint(this.checkpoint);
	}
}
