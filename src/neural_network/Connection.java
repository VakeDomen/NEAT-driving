package neural_network;

import helpers.Config;
import helpers.VectorHelper;

import java.util.Random;

public class Connection {
	
	private int inovation;
	private Node start;
	private Node end;
	private double weight;
	private boolean active;
	private Random r;
	
	public Connection (int inovation, Node start, Node end, double weight, boolean active) {

		try{
			end.getInovationNumber();
		}catch(Exception e){
			System.out.print("ERROR on connection " + inovation + " connecting " + start.getInovationNumber() + " to end ");
			System.out.println(end.getInovationNumber());

			e.printStackTrace();
			System.exit(5);
		}

		this.inovation = inovation;
		this.start = start;
		this.end = end;
		this.weight = weight;
		this.active = active;
		this.r = new Random();
	}


	public void linkToNodes(){
		start.addOutputConnection(this);
		end.addInputConnection(this);
	}

	public void randomizeWeight() {
		this.weight = randomWeight();
	}
	
	private double randomWeight() {
		double d = 1.;
		if(VectorHelper.randBool(0.5)){
			d = -1.;
		}
		return r.nextDouble() * 2 * d;
	}

	public double getWeightedOutput() {
		if(!this.active) return 0.;
		return this.weight * start.calculateNeuron();
	}

	public Node getStartingNode() {
		return this.start;
	}

	public Node getEndNode() {
		return this.end;
	}

	public double getWeight() {
		return this.weight;
	}

	public boolean isActive() {
		return this.active;
	}

	public int getInovationNumber() {
		return this.inovation;
	}

	public void setActive(boolean b) {
		this.active = b;
	}

	public void setWeight(double d) {
		if(d > Config.BIGGEST_CONNECTION_WEIGHT)
			d = Config.BIGGEST_CONNECTION_WEIGHT;
		if(d < Config.SMALLEST_CONNECTION_WEIGHT)
			d = Config.SMALLEST_CONNECTION_WEIGHT;
		this.weight = d;
	}
	
	public Connection clone() {
		return new Connection(
			this.inovation,
			this.start.clone(),
			this.end.clone(),
			this.weight,
			this.active
		);
	}

	public void setEndNode(Node n) {
		this.end = n;
	}
	
	public void setStartNode(Node n) {
		this.start = n;
	}
}
