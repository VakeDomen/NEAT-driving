package neural_network;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

import game_objects.Car;
import game_objects.Specie;
import helpers.Config;
import neural_network.Node.ActivationFunction;
import neural_network.Node.NodeType;

public class NetworkHandler {
	
	static NetworkHandler networkHandler = new NetworkHandler();
	/*
	 * array list of all crated nodes
	 */
	private ArrayList<Node> allNodes;
	/*
	 * list of all crated connections
	 */
	private ArrayList<Connection> allConnections;
	/*
	 * inovation number for nodes
	 */
	private int nodeIdCounter;
	/*
	 * inovation number for conections
	 */
	private int connectionIdCounter;
	/*
	 * input + output layer size
	 */
	private int baseNodeCount;
	
	/*
	 * random neural network with only input and output layer
	 */
	private Genome baseNetwork;
	/*
	 * fittest network for display purposes
	 */
	private Genome fittest;
	/*
	 * ArrayList of all species
	 */
	private ArrayList<Specie> species;
	/*
	 * specie counter
	 */
	private int specieCounter;
	/*
	 * lock fittest genome value
	 */
	private Object genomeLock = new Object();
	
	
	
	public NetworkHandler() {
		this.connectionIdCounter = 0;
		this.nodeIdCounter = 0;
		this.specieCounter = 0;
		this.allNodes = new ArrayList<Node>();
		this.allConnections = new ArrayList<Connection>();
		this.species = new ArrayList<Specie>();
		this.baseNodeCount = Config.NETWORK_INPUT_LAYER_SIZE + Config.NETWORK_OUTPUT_LAYER_SIZE;
		generateBaseNeuralNetwork();
	}
	
	
	public void draw(Graphics2D g2d) {
		synchronized(genomeLock) {
			if(this.fittest != null) fittest.draw(g2d);
		}
		
	}
	
	public Node createNewNode(ActivationFunction activationFunction, NodeType type, int connectionInovationNubmer) {
		System.out.println("creating new " + type +" node on connection " + connectionInovationNubmer);

		Node node = getNodeMadeOnConnection(connectionInovationNubmer);
		if(node == null)
			node = new Node(this.nodeIdCounter, activationFunction, type, connectionInovationNubmer);
		else
			return node;

		System.out.println("New node created: " + node.getInovationNumber());
		this.allNodes.add(node);
		this.nodeIdCounter++;
		return node;
	}

	Connection createNewConnectionWithRandomWeight(Node from, Node to) {
		System.out.println("creating from: " + from.getInovationNumber() + "\tto: " + to.getInovationNumber());

		Connection c = getExistingConneciton(from, to);
		if(c != null){
			c = c.clone();
			System.out.println("Exists: " + c.getInovationNumber());
			c.setEndNode(to);
			c.setStartNode(from);
			c.randomizeWeight();
			c.setActive(true);
			return c;
		}
		System.out.println("creating connection -> new: " + this.connectionIdCounter);
		Connection conn = new Connection(this.connectionIdCounter, from, to, randomWeight(), true);
		this.allConnections.add(conn);
		this.connectionIdCounter++;
		return conn;
	}



	public Connection createNewConnectionWithSetWeight(Node from, Node to, double weight) {
		System.out.println("creating from: " + from.getInovationNumber() + "\tto: " + to.getInovationNumber());

		Connection c = getExistingConneciton(from, to);
		if(c != null){
			c = c.clone();
			System.out.println("Exists: " + c.getInovationNumber());
			c.setEndNode(to);
			c.setStartNode(from);
			c.setWeight(weight);
			c.setActive(true);
			return c;
		}
		System.out.println("creating connection -> new: " + this.connectionIdCounter);
		Connection conn = new Connection(this.connectionIdCounter, from, to, weight, true);
		this.allConnections.add(conn);
		this.connectionIdCounter++;
		return conn;
	}
	
	
	private double randomWeight() {
		return new Random().nextDouble() * 4 - 2;
	}




	private void generateBaseNeuralNetwork() {
		//if the base network has not been initialized
		
		if(this.baseNetwork == null) {
			
			ArrayList<Node> nodes = new ArrayList<Node>();
			ArrayList<Connection> connections = new ArrayList<Connection>();
			
			//create input nodes
			for (int i = 0; i < Config.NETWORK_INPUT_LAYER_SIZE ; i++) {
				nodes.add(createNewNode(ActivationFunction.SIGMOID, NodeType.INPUT, -1));
			}
			
			//create output nodes
			for (int i = 0; i < Config.NETWORK_OUTPUT_LAYER_SIZE ; i++) {
				nodes.add(createNewNode(ActivationFunction.RELU, NodeType.OUTPUT, -1));
			}
			
			for(int i = 0 ; i < nodes.size() ; i ++) {
				
				//if node is an input node, we will connect it to all output nodes
				if(nodes.get(i).getType() == NodeType.INPUT) {
					
					for(int j = 0 ; j < nodes.size() ; j++) {
						
						//if node is an output node, create connection from input to output
						if(nodes.get(j).getType() == NodeType.OUTPUT) {
							
							connections.add(createNewConnectionWithRandomWeight(nodes.get(i), nodes.get(j)));
							
						}
					}
				}
			}

			for(Connection c : connections) c.linkToNodes();

			this.baseNetwork = new Genome(nodes, connections);
		}
		
		
	}
	
	
	public Genome getBaseGenomeWithRandomizedWeights() {
		//randomize weights of all connections in network
		this.baseNetwork.randomizeAllWeights();
		
		//return new genome with randomized weights
		return this.baseNetwork.cloneGenome();
		
	}


	public void setFittest(Genome genome) {
		synchronized(genomeLock) {
			this.fittest = genome.cloneGenome();
		}
		
	}


	public void speciate(Car c) {
		boolean inserted = false;
		for(Specie s : this.species) {
			if(s.getRepresentative().compatibilityDistance(c) < Config.COMPATIBILITY_THRESHOLD) {
				s.addToSpecie(c);
				inserted = true;
				break;
			}
		}
		if(!inserted) {
			Specie s = new Specie(this.specieCounter, c);
			this.specieCounter++;
			this.species.add(s);
			s.addToSpecie(c);
		}
			
	}




	public void selectSpeciesRepresentativesAndClearSpecies() {
		
		if(this.species.size() > 0) {

			for(Specie s : this.species) {
				if(s.size() > 0)
					s.selectRepresentativeAndClearSpecie();
			}
			
		
			if(Config.LOG_SPECIES)
				System.out.println("Species: " + this.species.size());
			
		}else {
			if(Config.LOG_SPECIES)
				System.out.println("Species: no spicies");
		}
		
		
	}


	public Node getNodeMadeOnConnection(int inovationNumber) {
		//starting input and output nodes
		if(inovationNumber == -1) return null;
		for(Node n : allNodes) {
			if(n.getMadeOnConnection() == inovationNumber)
				return n;
		}
		return null;
	}


	private Connection getExistingConneciton(Node from, Node to) {
		for(Connection c : this.allConnections){
			if (c.getStartingNode().getInovationNumber() == from.getInovationNumber() && c.getEndNode().getInovationNumber() == to.getInovationNumber() ||
					c.getStartingNode().getInovationNumber() == to.getInovationNumber() && c.getEndNode().getInovationNumber() == from.getInovationNumber()){
				return c;
			}
		}
		return null;
	}


	public void selection() {
		for(Specie s : this.species) {
			s.selection();
		}
		ArrayList<Specie> toDel = new ArrayList<Specie>();
		for(Specie s : this.species)
			if(s.isEmpty())
				toDel.add(s);
			
		for(Specie s : toDel)
			this.species.remove(s);
	}

	public int getConnectionInovation() {
		return this.connectionIdCounter -1;
	}

	public int getNodeInovation() {
		return this.nodeIdCounter -1;
	}
	
	public ArrayList<Specie> getSpecies() {
		return this.species;
	}




	


	
	

}
