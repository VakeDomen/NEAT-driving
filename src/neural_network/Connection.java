package neural_network;

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
		return r.nextDouble() * 4 - 2;
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
