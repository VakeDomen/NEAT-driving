package game_objects;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import helpers.VectorHelper;

public class Wall {

	private ArrayList<Point> wall;
	
	public Wall(ArrayList<Point> w) {
		this.wall = w;
	}

	public void draw(Graphics2D g2d) {
		
		Stroke tmp = g2d.getStroke();
		g2d.setStroke(new BasicStroke(4));
		for(int i = 1 ; i <=this.wall.size() ; i++) {
			if(i ==this.wall.size()) 
				g2d.drawLine(wall.get(i-1).x,this.wall.get(i-1).y,this.wall.get(0).x,this.wall.get(0).y);
			else
				g2d.drawLine(wall.get(i-1).x,this.wall.get(i-1).y,this.wall.get(i).x,this.wall.get(i).y);
			
		}
		
		g2d.setStroke(tmp);
	}

	public Point getIntersecion(Line2D.Double line){
		Point p = null;
		
		for (int i = 1; i < wall.size(); i++) {
			Line2D.Double l = null;
			if(i == wall.size() - 1) {
				l = new Line2D.Double(
						wall.get(i),
						wall.get(0)
				);
			}else {
				l = new Line2D.Double(
						wall.get(i - 1),
						wall.get(i)
				);
			}
			
			
			if(line.intersectsLine(l)) {
				
				p = VectorHelper.intersectionBetweenTwoLines2d(
						line, 
						l
				);
				break;
			}
			
			
			
		}
		return p;
		
	}
}
