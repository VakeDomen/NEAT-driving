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

	private Random r;
	private int genomeId;
	
	
	public NetworkHandler() {
		this.connectionIdCounter = 0;
		this.nodeIdCounter = 0;
		this.specieCounter = 0;
		this.genomeId = 1;
		this.r = new Random();
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
		if(Config.LOG_NODE_AND_CONNECTION_CREATION)
			System.out.println("creating new " + type +" node on connection " + connectionInovationNubmer);

		Node node = getNodeMadeOnConnection(connectionInovationNubmer);
		if(node != null) {
			return node;
		}

		node = new Node(this.nodeIdCounter, activationFunction, type, connectionInovationNubmer);

		if(Config.LOG_NODE_AND_CONNECTION_CREATION) {
			System.out.println("New node created: " + node.getInovationNumber());
		}


		this.allNodes.add(node);
		this.nodeIdCounter++;
		return node;
	}

	Connection createNewConnectionWithRandomWeight(Node from, Node to) {
		if(Config.LOG_NODE_AND_CONNECTION_CREATION)
			System.out.println("creating from: " + from.getInovationNumber() + "\tto: " + to.getInovationNumber());

		Connection c = getExistingConneciton(from, to);
		if(c != null){

			if(Config.LOG_NODE_AND_CONNECTION_CREATION) {
				System.out.println("Exists: " + c.getInovationNumber());
			}

			c = c.cloneConncection();
			c.setEndNode(to);
			c.setStartNode(from);
			c.randomizeWeight();
			c.setActive(true);
			return c;
		}

		if(Config.LOG_NODE_AND_CONNECTION_CREATION) {
			System.out.println("creating connection -> new: " + this.connectionIdCounter);
		}

		Connection conn = new Connection(
				this.connectionIdCounter,
				from,
				to,
				randomWeight(),
				true
		);

		this.allConnections.add(conn);
		this.connectionIdCounter++;

		conn = conn.cloneConncection();
		conn.setStartNode(from);
		conn.setEndNode(to);
		conn.randomizeWeight();

		return conn;
	}



	public Connection createNewConnectionWithSetWeight(Node from, Node to, double weight) {
		if(Config.LOG_NODE_AND_CONNECTION_CREATION)
			System.out.println("creating from: " + from.getInovationNumber() + "\tto: " + to.getInovationNumber());

		Connection c = getExistingConneciton(from, to);
		if(c != null){

			if(Config.LOG_NODE_AND_CONNECTION_CREATION) {
				System.out.println("Exists: " + c.getInovationNumber());
			}

			c = c.cloneConncection();
			c.setEndNode(to);
			c.setStartNode(from);
			c.setWeight(weight);
			c.setActive(true);
			return c;
		}
		if(Config.LOG_NODE_AND_CONNECTION_CREATION) {
			System.out.println("creating connection -> new: " + this.connectionIdCounter);
		}

		Connection conn = new Connection(
				this.connectionIdCounter,
				from,
				to,
				randomWeight(),
				true
		);

		this.allConnections.add(conn);
		this.connectionIdCounter++;

		conn = conn.cloneConncection();
		conn.setStartNode(from);
		conn.setEndNode(to);
		conn.randomizeWeight();

		return conn;
	}
	
	
	private double randomWeight() {
		return this.r.nextDouble() * 4 - 2;
	}




	private void generateBaseNeuralNetwork() {
		//if the base network has not been initialized

		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Connection> connections = new ArrayList<Connection>();

		//create input nodes
		for (int i = 0; i < Config.NETWORK_INPUT_LAYER_SIZE ; i++) {
			nodes.add(createNewNode(ActivationFunction.SIGMOID, NodeType.INPUT, -1).cloneNode());
		}

		//create output nodes
		for (int i = 0; i < Config.NETWORK_OUTPUT_LAYER_SIZE ; i++) {
			nodes.add(createNewNode(ActivationFunction.RELU, NodeType.OUTPUT, -1).cloneNode());
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

//		for(Connection c : connections) c.linkToNodes();

		this.baseNetwork = new Genome(nodes, connections, this);

		
		
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
		if (!inserted) {
			Specie s = new Specie(this.specieCounter, c);
			this.specieCounter++;
			this.species.add(s);
			s.addToSpecie(c);
		}
			
	}




	public void selectSpeciesRepresentativesAndClearSpecies() {
		
		if(this.species.size() > 0) {

			for(Specie s : this.species) {
				if(s.size() > 0) {
					s.selectRepresentativeAndClearSpecie();
				}
			}

			if (Config.LOG_SPECIES) {
				System.out.println("Species: " + this.species.size());
			}
			
		} else {
			if (Config.LOG_SPECIES) {
				System.out.println("Species: no spicies");
			}
		}
		
		
	}


	public Node getNodeMadeOnConnection(int inovationNumber) {
		//starting input and output nodes
		if(inovationNumber == -1) {
			return null;
		}

		for(Node n : allNodes) {
			if(n.getMadeOnConnection() == inovationNumber){
				return n;
			}
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
	}

	public int getConnectionInovation() {
		return this.connectionIdCounter - 1;
	}

	public int getNodeInovation() {
		return this.nodeIdCounter - 1;
	}
	
	public ArrayList<Specie> getSpecies() {
		return this.species;
	}


	public void removeExtinctSpecies() {
		ArrayList<Specie> toDel = new ArrayList<Specie>();
		for(Specie s : this.species){
			s.removeDead();
			if(s.isEmpty())
				toDel.add(s);
		}


		for(Specie s : toDel){
			this.species.remove(s);

		}
	}

    public int generateGenomeId() {
		this.genomeId++;
		return this.genomeId;
    }
}
