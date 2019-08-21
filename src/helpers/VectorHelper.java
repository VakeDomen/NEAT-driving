package helpers;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import game_objects.Car;

public class VectorHelper {

	public static VectorHelper vectorHelper = new VectorHelper();
	
	
	public static Vector<Double> line2dToVector(Line2D.Double line){
		Vector<Double> out = new Vector<Double>();
		out.add(line.getX2() - line.getX1());
		out.add(line.getY2() - line.getY1());
		return out;
	}
	public static Point vector2dToPoint(Vector<Double> v) {
		return new Point(
			(int) Math.round(v.get(0)),
			(int) Math.round(v.get(1))
		);
	}
	

	public static Point intersectionBetweenTwoLines2d(Line2D.Double l1, Line2D.Double l2) {
		double x1 = l1.getX1(); double y1 = l1.getY1();
		double x2 = l1.getX2(); double y2 = l1.getY2();
		double x3 = l2.getX1(); double y3 = l2.getY1();
		double x4 = l2.getX2(); double y4 = l2.getY2();
		double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);

		if (d == 0) return null;
		double xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
		double yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;
		
		return new Point(
				(int) Math.round(xi),
				(int) Math.round(yi)
		);
	}
	
	public static double angleBetweenVectors(Vector<Double> v1, Vector<Double> v2){
		return Math.toDegrees(Math.acos(scalarProduct(v1, v2)/(vectorSize(v1)*vectorSize(v2))));
	}
	public static double angleBetweenVectors2(Vector<Double> v1, Vector<Double> v2){
		return Math.toDegrees(Math.atan2( v1.get(0)*v2.get(1) - v1.get(1)*v2.get(0), v1.get(0)*v2.get(0) + v1.get(1)*v2.get(1)));
	}


	public static double vectorSize(Vector<Double> v){
		double size = 0.;
		for (int i = 0; i < v.size(); i++) {
			size += Math.pow(v.elementAt(i), 2);
		}
		return (double) Math.sqrt(size);
	}
	
	
	public static double scalarProduct(Vector<Double> v1, Vector<Double> v2){
		double prod = 0;
		for (int i = 0; i < v2.size(); i++) {
			prod += (v1.get(i) * v2.get(i));
		}
		return prod;
	}


	public static Vector<Double> resizeVector(Vector<Double> v, double size){
		return multiplyVectorWithSkalar(v, size / vectorSize(v));
	}


	public static Vector<Double> multiplyVectorWithSkalar(Vector<Double> v, double s){
		Vector<Double> v1 = new Vector<Double>(v.size());
		for (int i = 0; i < v.size(); i++) {
			v1.add((double) (v.get(i) * s));
		}		
		return v1;
	}
	
	/** 
	 * Plus: 
	 * Vector<Double> v1, v2
	 * return:
	 * Vector<Double> v3
	 * sum of v1 and v2
	 * **/
	public static Vector<Double> plus(Vector<Double> v1, Vector<Double> v2){
		Vector<Double> v3 = new Vector<>(v1.size());
		for (int i = 0; i < v1.size(); i++) {
			v3.add(v1.get(i) + v2.get(i));
		}
		return v3;
	}
	public static Double distBetweenTwoPoints2d(Point p1, Point p2) {
		int x = p1.x - p2.x;
		int y = p1.y - p2.y;
		return Math.sqrt(
				Math.pow(x, 2) + Math.pow(y, 2)
		);
	}
	public static Vector<Double> point2dToVector(Point p) {
		Vector<Double> out = new Vector<Double>();
		out.add((double) p.x);
		out.add((double) p.y);
		return out;
	}


	
	
	public static boolean randBool(double odds) {
		if(new Random().nextDouble() < odds) return true;
		return false;
	}
	public static ArrayList<Car> bubbleSortCarsScore(ArrayList<Car> cars) {
	    boolean sorted = false;
	    Car temp;
	    while(!sorted) {
	        sorted = true;
	        for (int i = 0; i < cars.size() - 1; i++) {
	            if (cars.get(i).getFitnessScore() > cars.get(i + 1).getFitnessScore()) {
	                temp = cars.get(i);
	                cars.set(i, cars.get(i + 1));
	                cars.set(i + 1, temp);
	                sorted = false;
	            }
	        }
	    }
	    return cars;
	}
	public static ArrayList<Car> bubbleSortCarsFitness(ArrayList<Car> cars) {
		boolean sorted = false;
		Car temp;
		while(!sorted) {
			sorted = true;
			for (int i = 0; i < cars.size() - 1; i++) {
				if (cars.get(i).getFitness() > cars.get(i + 1).getFitness()) {
					temp = cars.get(i);
					cars.set(i, cars.get(i + 1));
					cars.set(i + 1, temp);
					sorted = false;
				}
			}
		}
		return cars;
	}


}



