package neural_network;

import java.util.ArrayList;


public class Node {
	
	
	private int inovation;
	private ActivationFunction activationFunction;
	private Double weightedSum;
	private NodeType type;
	
	
	private ArrayList<Connection> inputConnections;
	private ArrayList<Connection> outputConnections;
	
	
	
	public enum NodeType{
		INPUT,
		OUTPUT,
		HIDDEN,
	}
	
	
	public enum ActivationFunction {
		SIGMOID,
		RELU
	}
	
	
	public Node(int inovation, ActivationFunction activationFunction, NodeType type) {
		this.inputConnections = new ArrayList<Connection>();
		this.outputConnections = new ArrayList<Connection>();	
		this.inovation = inovation;
		this.activationFunction = activationFunction;
		this.type = type;
	}
	
	
	public void addInputConnection(Connection c) {
		this.inputConnections.add(c);
	}
	
	
	public void addOutputConnection(Connection c) {
		this.outputConnections.add(c);
	}
	
	
	public double calculateNeuron() {
		if(this.weightedSum == null) this.weightedSum =  calculateWeightedSum();
		return activationFunction(this.weightedSum);
	}
	
	
	private double calculateWeightedSum() {
		double sum = 0;
		for(Connection c : this.inputConnections) {
			sum += c.getWeightedOutput();
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
		//return Math.log(1 + Math.pow(Math.E, x));
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
}
