package game_objects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.List;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.sound.sampled.Line;

import helpers.VectorHelper;
import neural_network.NetworkHandler;
import neural_network.Node;
import neural_network.Node.NodeType;
import neural_network.Connection;
import neural_network.Genome;
import helpers.Config;
import resource_loader.ResourceLoader;

public class Car {
	
	
	
	/*
	 * vector that determines the direction in wich the car is facing
	 */
	private Vector<Double> direction;
	/*
	 * speed value 
	 * [-MAX_REVERSE_SPEED, MAX_SPEED]
	 */
	private int speed;
	/*
	 * vector that determines x and y position of the car 
	 */
	private Vector<Double> position;
	/*
	 * array of lines that represent the vision of the car
	 * the sight distance is determined by CAR_SIGHT_DISTANCE
	 */
	private Line2D.Double[] sightLines;
	/*
	 * array of double values that represent the distance to the wall if it's in range on the sight distance
	 */
	private Double[] sightValues;
	/*
	 * array of points that represent the point of the wall the car sees
	 */
	private Point[] intersectionPoints;
	/*
	 * genes of the car
	 * the neural network (set of nodes and connections) that is evolved over time
	 */
	private Genome genome;	
	/*
	 * the track object that represents the road, walls and checkpoints
	 */
	private Track track;
	/*
	 * the car image that is displayed on the car's position
	 */
	private Image img;
	/*
	 * value that represents the angle in which the car was facing last tick 
	 */
	private double lastAngle = 0.0;
	/*
	 * starting orientation (facing direction) of the car
	 */
	private Vector<Double> baseOrientation;
	/*
	 * boolean that represents if the car has hit the wall in the simulation
	 */
	private boolean colided;
	/*
	 * counter of how many checkpoints this car has reached
	 */
	private int passedCheckpoints;
	/*
	 * distance traveled this generation
	 */
	private int distanceTraveled;
	/*
	 * fitness value of the car
	 */
	private double fitness;
	/*
	 * fitness value that does not get normalized
	 */
	private double fitnessScore;
	/*
	 * counter of how many simulation ticks the object was alive
	 */
	private int ticks;
	/*
	 * evaluation of first few moves, to determine if standing still on the start, to end generation 
	 */
	private boolean still;
	/*
	 * value respresents weather the object has fallen to natural selection
	 */
	private boolean toKill;
	
	
	

	
	
	public Car(Track track, Genome genome) {
		this.genome = genome;
		//genome.printWeights();
		this.toKill = false;
		this.still = true;
		this.passedCheckpoints = 0;
		this.ticks = 0;
		this.track = track;
		this.position = new Vector<Double>();
		this.position.add((double) track.getStart().x);
		this.position.add((double) track.getStart().y);
		this.direction = new Vector<Double>();
		this.direction.add((double) Config.START_DIRECTION_X);
		this.direction.add((double) Config.START_DIRECTION_Y);
		this.baseOrientation = new Vector<Double>();
		this.baseOrientation.add((double) Config.IMAGE_BASE_ORIENTATION_X);
		this.baseOrientation.add((double) Config.IMAGE_BASE_ORIENTATION_Y);
		this.img = ResourceLoader.getImage("car.png").getScaledInstance(Config.CAR_LENGTH, Config.CAR_WIDTH, Image.SCALE_DEFAULT); 
		this.colided = false;
		this.sightLines = refreshSightLines();
		this.intersectionPoints = new Point[Config.CAR_SIGHT_LINES_COUNT];
	}
	
	
	


	public void draw(Graphics2D g2d) {
	
		if(Config.DISPLAY_CAR)
			drawCar(g2d);
	    
	    
	    if(Config.DISPLAY_CAR_SIGHT) {
	    	
	    	g2d.setColor(Color.RED);

	    	if(this.sightLines != null) {
	    		for(Line2D.Double line : this.sightLines) {
		    		g2d.drawLine(
		    				(int) line.getX1(),
		    				(int) line.getY1(),
		    				(int) line.getX2(),
		    				(int) line.getY2()
		    		);
		    	}
	    	}	
	    }
	    
	    
	    if(Config.DISPLAY_SIGHT_POINTS) {
	    	
	    	g2d.setColor(Color.RED);
	    	
	    	if(this.intersectionPoints != null) {
	    		for(Point p : this.intersectionPoints) {
	    			if(p != null)
	    			g2d.fillOval(
	    					p.x - Config.CAR_SIGHT_POINT_SIZE / 2, 
	    					p.y - Config.CAR_SIGHT_POINT_SIZE / 2, 
	    					Config.CAR_SIGHT_POINT_SIZE, 
	    					Config.CAR_SIGHT_POINT_SIZE
	    			);
		    	}
	    	}
	    }	    
	}
	
	private void drawCar(Graphics2D g2d) {
		//calculate the angle between the car's last direction and current direction 
		double newAngle = findAngle();
		
		if(newAngle == 0.0 ) newAngle = this.lastAngle;
		else this.lastAngle += newAngle;
		
		//save the current transformation for reset
		AffineTransform backup = g2d.getTransform();
		//define new transformation
	    AffineTransform a = AffineTransform.getRotateInstance(
	    		newAngle, 
	    		(int)(this.position.get(0) + 0), 
	    		(int)(this.position.get(1) + 0)
	    );
	    //draw rotated image with new transformation
	    g2d.setTransform(a);
	    g2d.drawImage(
	    		this.img, 
	    		(int) Math.round(this.position.get(0)) - Config.CAR_LENGTH / 2, 
	    		(int) Math.round(this.position.get(1)) - Config.CAR_WIDTH / 2, 
	    		null
	    );
	    //reset transformation
	    g2d.setTransform(backup);
		
	}
	
	private double findAngle() {
		Vector<Double> a = this.baseOrientation;
		Vector<Double> b = this.direction;
		//drive reverse
		if(this.speed < 0) b = VectorHelper.resizeVector(this.direction, this.speed * -1.); 
		
		if(b != null && !a.equals(b)) {
			double angle = VectorHelper.angleBetweenVectors2(a, b);
			return Math.toRadians(angle);
		}else {
			return 0.0;
		}
	}


	public void makeAction() {
		//do nothing if car already collided into the wall
		if(colided) return;
		
		//refresh the sight values
		this.sightLines = refreshSightLines();
		this.sightValues = calcSightValues();
		
		//from data collected, generate the input vector for the genome neural network
		Double[] inputVector = generateInputVector();
				
		//calculate the move values with the neural network and choose the most dominant move 
		int action = argMax( genome.predict(inputVector));

		
		if(Config.LOG_INPUT_AND_ACTION) {
			DecimalFormat df2 = new DecimalFormat("#.##");
			for(Double d : inputVector) System.out.print(df2.format(d) + "\t ");
			
			System.out.println("\t --> " + action);
		}
		
		
		//perform action based on the networks prediction 
		switch (action) {
		case 0:
			break;
		case 1:
			turnLeft();
			break;
		
		case 2:
			turnRight();
			break;
			
		case 3:
			accelerate();
			break;
		
		case 4:
			reverse();
			break;

		default:
			//do nothing
			//includes the action 0
			break;
		}
		
	}
	
	
	private int argMax(Double[] a) {
		int index = 0;
		Double max = Double.MIN_VALUE;
		for(int i = 0 ; i < a.length ; i++) {
			//System.out.print(i + " ->  " + a[i] + "\t");
			if(a[i] > max) {
				max = a[i];
				index = i;
			}
		}
		//System.out.println();
		return index;
	}


	private Double[] generateInputVector() {
		Double[] inputVector = new Double[Config.NETWORK_INPUT_LAYER_SIZE];
		int index = 0;

		//first argument in vector is the speed of car, normalized with the MAX_SPEED value
		inputVector[index] = (double) (this.speed * 1.0/ Config.MAX_SPEED);
		index++;
		
		//other arguments are car sight (distance to walls) values
		for(int i = 0 ; i < this.sightValues.length ; i++) {
			//sight values are normalized by max sight distance
			inputVector[i + index] = this.sightValues[i] * 1.0 / Config.CAR_SIGHT_DISTANCE;
		}
		
		
		return inputVector;
	}





	private void turn(double x, double y) {
		//create direction vector of the turn
		Vector<Double> steerForce = new Vector<Double>();
		steerForce.add(x);
		steerForce.add(y);
		
		//directionDevider is a multiplier, that determines the size of the turning force vector
		//higher speed -> stronger turn
		//speed 0 -> no turn
		int directionDivider = Config.MAX_SPEED;
		if(this.speed < 0) directionDivider = Config.MAX_REVERSE_SPEED;
		//resize the force
		steerForce = VectorHelper.resizeVector(
				steerForce, 
				Config.STEER_FORCE_SIZE * (this.speed * 1.0 / directionDivider * 1.0)
		);
		
		//apply the force to the direction vector
		this.direction = VectorHelper.plus(
				VectorHelper.resizeVector(this.direction, Math.abs(this.speed)), 
				steerForce
		);		
	}
	
	
	private void turnRight() {
		//calculate x and y of right-perpendicular vector to the current direction 
		double x = this.direction.get(1) * -1;
		double y = this.direction.get(0);
		//if standing still, dont turn
		if(this.speed != 0) turn(x, y);
	}


	private void turnLeft() {
		//calculate x and y of left-perpendicular vector to the current direction
		double x = this.direction.get(1);
		double y = this.direction.get(0) * -1;
		//if standing still, dont turn
		if(this.speed != 0) turn(x, y);
	}


	private void accelerate() {
		if(this.speed < Config.MAX_SPEED) this.speed = this.speed + Config.SPEED_INCREMENT;
	}
	private void reverse() {
		if(this.speed > Config.MAX_REVERSE_SPEED * -1.0) this.speed = this.speed - Config.SPEED_INCREMENT;
	}


	private int getRandomAction() {
		return (int)Math.round(new Random().nextInt(5));
	}


	public void update() {
		//do nothing if colided
		if(this.colided) return;
		
		
		//if is moving set still variable to false (used to go to next generation if car just standing still on spawn)
		if(this.speed != 0) this.still = false;
		
		
		//check if car will cross checkpoint with current move
		checkCheckpointCrossing();
		
		//move the car in the facing direction 
		this.position = VectorHelper.plus(
				position, 
				VectorHelper.resizeVector(direction, speed)
		);
		
		//add distance traveled
		this.distanceTraveled += speed;
		this.ticks++;
		
		
	}
	
	
	private void checkCheckpointCrossing() {
		//initiate movement line
		//point A = current position
		//point B = position after move
		Line2D.Double move = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(
								position, 
								VectorHelper.resizeVector(
										direction,
										// + 1 = due to double->int rounding error
										speed + 1
								)
						)
				)
		);
		
		//check all checkpoints if any intersect with movement line
		for(Checkpoint c : this.track.getCheckpoints()) {
			//if crossed checkpoint
			if(c.intersects(move)) {
				//deactivate this checkpoint and activate next checkpoint in queue
				this.track.activateNextCheckpoint(c);

				//count the checkpoint
				this.passedCheckpoints++;
			}
		}
	}





	private Line2D.Double[] refreshSightLines() {
		//array of all lines of sight
		Line2D.Double[] lines = new Line2D.Double[Config.CAR_SIGHT_LINES_COUNT];
		//helper vector for left and right lines
		Vector<Double> tmp = new Vector<Double>();
		
		//line forward
		lines[0] = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(
								this.position,
								VectorHelper.resizeVector(this.direction, Config.CAR_SIGHT_DISTANCE))
						)
		);
		
		//line back
		lines[1] = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(
								this.position,
								VectorHelper.resizeVector(this.direction, Config.CAR_SIGHT_DISTANCE * -1))
						)
		);
		
		//line left
		tmp.add(this.direction.get(1));
		tmp.add(this.direction.get(0) * -1);
		lines[2] = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(
								VectorHelper.resizeVector(
										tmp,
										Config.CAR_SIGHT_DISTANCE
								),
								this.position
						)
				)
		);
		tmp.clear();
		
		//line right
		tmp.add(this.direction.get(1) * -1);
		tmp.add(this.direction.get(0));
		lines[3] = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(
								VectorHelper.resizeVector(
										tmp,
										Config.CAR_SIGHT_DISTANCE
								),
								this.position
						)
				)
				
		);
		tmp.clear();
		
		//line front-left
		lines[4] = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(	
								VectorHelper.resizeVector(
										VectorHelper.plus(
												VectorHelper.line2dToVector(lines[0]), 
												VectorHelper.line2dToVector(lines[2])
										),
										Config.CAR_SIGHT_DISTANCE
								),
								this.position
						)
				)
		);
		
		//line front-right
		lines[5] = new Line2D.Double(
				VectorHelper.vector2dToPoint(this.position),
				VectorHelper.vector2dToPoint(
						VectorHelper.plus(
								VectorHelper.resizeVector(
								
										VectorHelper.plus(
											VectorHelper.line2dToVector(lines[0]), 
											VectorHelper.line2dToVector(lines[3])
										),
										Config.CAR_SIGHT_DISTANCE
								),
								this.position
						)
				)
		);
		
		return lines;
	}
	
	
	
	
	private Double[] calcSightValues() {
		//array of all values of distances to the wall 
		Double[] out = new Double[this.sightLines.length];
		
		//calculate distance to wall (if car can see the wall) for every sight line 
		int i = 0;
		for(Line2D.Double line : sightLines) {
			
			//intersection point
			Point inter = null;
			
			//check for intersection with every wall
			for(Wall w : this.track.getWalls()) {
				
				//check for intersection between line and wall
				inter = w.getIntersecion(line);
						
				
				
				
				//if found intersection for line -> break
				//one sight line, can not see multiple walls
				if(inter != null) break;
				
			}
			
			
			//if sight line has intersection with a wall calculate distance to it
			if(inter != null) out[i] = VectorHelper.distBetweenTwoPoints2d(
				VectorHelper.vector2dToPoint(this.position),
				inter
			);
			
			//if no intersection insert max sight distance
			else out[i] = (double) Config.CAR_SIGHT_DISTANCE;
			
			
			//save the point of intersection to array for display purposes
			this.intersectionPoints[i] = inter;
			
			//loop increment
			i++;	
		}
		return out;
	}
	
	
	public Point getPositionPoint() {
		return new Point(
			(int) Math.round(this.position.get(0)),
			(int) Math.round(this.position.get(1))
		);
	}


	public void setTrack(Track track) {
		this.track = track;
	}
	public boolean checkCollision() {
		//check for collision
		if(!this.track.getRoad().contains(getPositionPoint())) this.colided = true;
		//collision state
		return this.colided;
	}





	public boolean checkDriving() {
		//if colided or standing still
		if(checkCollision() || this.still) return false;
		else return true;
	}

	
	
	
	public double evaluate() {
		this.fitness = this.distanceTraveled * 1.0 * (this.passedCheckpoints * 1.0 + 1) + 1;
		this.fitnessScore = this.fitness;
		return this.fitness;
	}





	public double getFitness() {
		return this.fitness;
	}

	public double getFitnessScore() {
		return this.fitnessScore;
	}

	public void offsetFitness(double smallestFitness) {
		this.fitness += smallestFitness;
	}


	public void normalizeFitness(double fittest) {
		this.fitness /= fittest;
	}


	public void mutate() {
		if(VectorHelper.randBool(Config.MUTATION_NEW_NODE_ODDS)) genome.newNodeMutation();
		if(VectorHelper.randBool(Config.MUTATOIN_ADUJST_CONNECTION_ODDS)) genome.weightAdjustmentMutation();
		if(VectorHelper.randBool(Config.MUTATION_RANDOM_CONNECTION_ODDS)) genome.weightRandomizeMutation();
		if(VectorHelper.randBool(Config.MUTATION_DEACTIVATE_CONNECTION_ODDS)) genome.connectionActivationMutation();
	}


	public double compatibilityDistance(Car c) {
		return ((Config.COMPATIBILITY_DISTANCE_WEIGHT_1 * this.excessGenes(c)) / this.genomeSizeFactor(c)) + 
				((Config.COMPATIBILITY_DISTANCE_WEIGHT_2 * this.disjointGenes(c)) / this.genomeSizeFactor(c)) + 
				(Config.COMPATIBILITY_DISTANCE_WEIGHT_3 * this.averageMatchingGeneWeightDifference(c))
		;
	}

	
	private int excessGenes(Car c) {
		return this.genome.excessGenes(c.genome);
	}

	private int disjointGenes(Car c) {
		return 1;
	}

	private int genomeSizeFactor(Car c) {
		return 1;
	}
	
	private double averageMatchingGeneWeightDifference(Car c) {
		return this.genome.averageMatchingGeneWeightDifference(c.getGenome());
	}
	
	public Genome getGenome() {
		return this.genome;
	}




	//TODO: make this faster and nicer
	public Car crossover(Car parent2) {
		
		ArrayList<Connection> connections = new ArrayList<Connection>();
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		
		
		int nodeLength = this.genome.biggestNodeInovationNumber();
		int parent2NodeLength = parent2.genome.biggestNodeInovationNumber();
		if( parent2NodeLength > nodeLength )
			nodeLength = parent2NodeLength;
		
		for(int i = 0 ; i <= nodeLength ; i++) {
			if(this.genome.getNodes().get(i) == null && parent2.genome.getNodes().get(i) == null) {
				continue;
			}else if(this.genome.getNodes().get(i) != null) {
				nodes.add(this.genome.getNodes().get(i).clone());
			}else if(parent2.genome.getNodes().get(i) != null) {
				nodes.add(parent2.genome.getNodes().get(i).clone());
			}
		}
		
		
		
		
		int genomeLength = this.genome.biggestConnectionInovationNumber();
		int otherGenomeLength = parent2.genome.biggestConnectionInovationNumber();
		if(genomeLength < otherGenomeLength)
			genomeLength = otherGenomeLength;

		int excessStartIndex = 0;
		int lastParent = 0;
		for(int i = 0 ; i <= genomeLength ; i++) {
			if(this.genome.getConnections().get(i) == null && parent2.genome.getConnections().get(i) == null)
				continue;
			else if(this.genome.getConnections().get(i) == null && parent2.genome.getConnections().get(i) != null) {
				if(lastParent != 1) {
					excessStartIndex = i;
					lastParent = 1;
				}				
				connections.add(refreshConnectionPointers(parent2.genome.getConnections().get(i).clone(), nodes));
				
			} else if(this.genome.getConnections().get(i) != null && parent2.genome.getConnections().get(i) == null) {
				if(lastParent != 2) {
					excessStartIndex = i;
					lastParent = 1;
				}
				connections.add(refreshConnectionPointers(this.genome.getConnections().get(i).clone(), nodes));
			} else {
				lastParent = 0;
				if(VectorHelper.randBool(0.5)) {
					connections.add(refreshConnectionPointers(this.genome.getConnections().get(i).clone(), nodes));
					
				}
				else connections.add(refreshConnectionPointers(parent2.genome.getConnections().get(i).clone(), nodes));
			}
		}
		
		
		ArrayList<Connection> toDel = new ArrayList<Connection>();
		if(lastParent == 2) {
			for(Connection c : connections) {
				if(c.getInovationNumber() >= excessStartIndex)
					toDel.add(c);
			}
		}
		for(Connection c : toDel) {
			connections.remove(c);
		}
		
//		ArrayList<Node> toDelNode = new ArrayList<Node>();
//		for(Node n : nodes) {
//			if(n.getInputConnections().size() == 0 && 
//				n.getOutputConnections().size() == 0 &&
//				n.getType() == NodeType.HIDDEN)
//				toDelNode.add(n);
//		}
//		for(Node n : toDelNode) {
//			nodes.remove(n);
//			System.out.println("Node to remove!");
//		}
//			
		
		
		if(Config.LOG_OFFSPRING_GENOME) {
			System.out.println("offspring--------------------------");
			DecimalFormat df2 = new DecimalFormat("#.##");
			for(Node n : nodes) {
				System.out.print("i: " + n.getInovationNumber() + " |");
			}
			System.out.println();
			for(Connection c : connections) {
				System.out.print("i: " + c.getInovationNumber() + " (" + c.getStartingNode().getInovationNumber() + " -> " + c.getEndNode().getInovationNumber() + ") w: " + df2.format(c.getWeight()) + "   ||");
			}
			System.out.println();
		}
		
		return new Car(this.track, new Genome(nodes, connections));
	}



	private Connection refreshConnectionPointers(Connection c, ArrayList<Node> nodes) {
		boolean foundEnd = false;
		boolean foundStart = false;
		for(Node n : nodes) {
			if(n.getInovationNumber() == c.getEndNode().getInovationNumber()) {
				c.setEndNode(n);
				foundEnd = true;
			}
				
			if(n.getInovationNumber() == c.getStartingNode().getInovationNumber()) {
				c.setStartNode(n);
				foundStart = true;
			}
				
		}
		if(!foundEnd && !foundStart)
			System.out.println("Didn't find endpoints for connection");
		return c;
	}




	public boolean getToKill() {
		return this.toKill;
	}
	public void setToKill(boolean b) {
		this.toKill = b;
	}
	public int getTicksSurvived() {
		return this.ticks;
	}






	
}

