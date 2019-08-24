package neural_network;

import java.util.ArrayList;


public class Node {
	
	
	private int inovation;
	private ActivationFunction activationFunction;
	private Double weightedSum;
	private NodeType type;
	
	
	private ArrayList<Connection> inputConnections;
	private ArrayList<Connection> outputConnections;
	
	private int madeOnConnection;
	private int owner; //genome id



	public enum NodeType{
		INPUT,
		OUTPUT,
		HIDDEN,
	}

	public enum ActivationFunction {
		SIGMOID,
		RELU
	}
	
	public Node(int inovation, ActivationFunction activationFunction, NodeType type, int connectionInovationNumber) {
		this.inputConnections = new ArrayList<Connection>();
		this.outputConnections = new ArrayList<Connection>();	
		this.inovation = inovation;
		this.activationFunction = activationFunction;
		this.type = type;
		this.madeOnConnection = connectionInovationNumber;
	}

	public Node cloneNode() {
		return new Node(
			this.inovation,
			this.activationFunction,
			this.type,
			this.madeOnConnection
		);
	}
	
	public void addInputConnection(Connection con) {
		boolean found = false;
		for(Connection c : this.inputConnections){
			if(c.getInovationNumber() == con.getInovationNumber()){
				found = true;
				if(c != con){
					System.out.println("INVALID POINTERS");
				}
			}
		}
		if(!found){
			if(this.owner == con.getOwner()){
				this.inputConnections.add(con);
			}else{
				System.out.println("WRONG OWNERSHIP FOR INPUT CONNECTION " + this.inovation + " (" + con.getOwner() + ") should be: "+ this.owner);
				throwStack();
			}
		}
	}

	public void addOutputConnection(Connection con) {
		boolean found = false;
		for(Connection c : this.outputConnections){
			if(c.getInovationNumber() == con.getInovationNumber()){
				found = true;
				if(c != con){
					System.out.println("INVALID POINTERS");
				}
			}
		}
		if(!found){
			if(this.owner == con.getOwner()){
				this.outputConnections.add(con);
			}else{
				System.out.println("WRONG OWNERSHIP FOR OUTPUT CONNECTION " + this.inovation + "  (" + con.getOwner() + ") should be: "+ this.owner);
				throwStack();
			}
		}
	}

	private void throwStack(){
		System.out.println("Printing stack trace:");
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < elements.length; i++) {
			StackTraceElement s = elements[i];
			System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
					+ "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
		}
		System.exit(1);
	}

	public double calculateNeuron(int owner) {
		if(this.owner != owner){
			System.out.println(owner + " NOT OWNER OF " + this.type + " NODE!!!!!!!!!!!!!!! owner is: " + this.owner);
		}
		for(Connection c : this.inputConnections){
			if(c.getOwner() != owner){
				System.out.println(owner + " NOT OWNER OF " + this.getInovationNumber() + " -> " + this.getInovationNumber() + " CONNECTION!!!!!!!!!!!!!!! owner is: " + this.owner);
			}
		}
		for(Connection c : this.outputConnections){
			if(c.getOwner() != owner){
				System.out.println(owner + " NOT OWNER OF " + this.getInovationNumber() + " -> " + c.getEndNode().getInovationNumber() + " CONNECTION!!!!!!!!!!!!!!! owner is: " + c.getOwner());
			}
		}

		if(this.weightedSum == null) this.weightedSum =  calculateWeightedSum(owner);
		return activationFunction(this.weightedSum);
	}

	private double calculateWeightedSum(int owner) {
		double sum = 0;
		for(Connection c : this.inputConnections) {
			sum += c.getWeightedOutput(owner);
		}
		return sum;
	}

	public int getInovationNumber() {
		return this.inovation;
	}

	public NodeType getType() {
		return this.type;
	}

	public ActivationFunction getActivationFunction() {
		return this.activationFunction;
	}

	public void setWeightedSum(Double d) {
		this.weightedSum = d;
	}

	private double activationFunction(double x) {
		switch(this.activationFunction){
		case SIGMOID:
			return sigmoid(x);
		case RELU:
			return rectifiedLinearUnit(x);
		}
		return (Double) null;
	}

	private double sigmoid(double x) {
		return (1 / (1 + Math.pow(Math.E, (-1 * x))));
	}

	private double rectifiedLinearUnit(double x) {
		return Math.max(0, x);
	}

	public ArrayList<Integer> getOutputNodeKeys() {
		ArrayList<Integer> out = new ArrayList<Integer>();
		for(Connection c : this.outputConnections) out.add(c.getEndNode().getInovationNumber());
		return out;
	}

	public ArrayList<Connection> getOutputConnections() {
		return this.outputConnections;
	}

	public ArrayList<Connection> getInputConnections() {
		return this.inputConnections;
	}

	public int getMadeOnConnection() {
		return this.madeOnConnection;
	}

	public Node clearConnections() {
		this.inputConnections = new ArrayList<>();
		this.outputConnections = new ArrayList<>();
		return this;
	}

	public void setOwner(int id) {
		this.owner = id;
	}
	public int getOwner(){
		return this.owner;
	}
}

