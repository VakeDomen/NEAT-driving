package neural_network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import game_objects.Car;
import helpers.Config;
import neural_network.Node.ActivationFunction;
import neural_network.Node.NodeType;

public class Genome {
	
	
	
	private Map<Integer, Node> nodes;
	private Map<Integer, Connection> connections;
	
	
	//-------------------------------------------------------------------------INITIATION------------------------------------------------------------------------
	
	
	public Genome(ArrayList<Node> nodes, ArrayList<Connection> connections) {
		this.nodes = initNodes(nodes);
		this.connections = initConnections(connections);
		
		for(Connection c : connections) {
			c.getStartingNode().addOutputConnection(c);
			c.getEndNode().addInputConnection(c);
		}
	}
	
	public Genome(Map<Integer, Node> nodes, Map<Integer, Connection> connections) {
		this.nodes = nodes;
		this.connections = connections;
	}

	private HashMap<Integer, Connection> initConnections(ArrayList<Connection> connections) {
		HashMap<Integer, Connection> hm = new HashMap<Integer, Connection>();
		for(Connection c : connections) hm.put(c.getInovationNumber(), c);
		return hm;
	}

	private HashMap<Integer, Node> initNodes(ArrayList<Node> nodes) {
		HashMap<Integer, Node> hm = new HashMap<Integer, Node>();
		for(Node n : nodes) hm.put(n.getInovationNumber(), n);
		return hm;
	}


	//-------------------------------------------------------------------------PREDICTIONS AND ADJUSTMENTS------------------------------------------------------------------------
	
	public Double[] predict(Double[] inputVector) {
		
		
		
		Double[] outputVector = new Double[Config.NETWORK_OUTPUT_LAYER_SIZE];
		resetNetwork();
		
		
		if(inputVector.length != Config.NETWORK_INPUT_LAYER_SIZE) 
			System.out.println("wrong input vector size");

		//set all the input values to corresponding input node
		for(int i = 0 ; i < inputVector.length ; i++) 
			nodes.get(i).setWeightedSum(inputVector[i]);
		
		
		
		int index = 0;
		for(Integer key : this.nodes.keySet()) {
			if(nodes.get(key).getType() == NodeType.OUTPUT) {
				outputVector[index] = nodes.get(key).calculateNeuron();
				index++;
			}
		}
		//print(outputVector);
		
		return outputVector;
	}
	

	
	
	
	//-------------------------------------------------------------------------MUTATIONS------------------------------------------------------------------------
	
	
	public void newNodeMutation() {
		//System.out.println("NEW NODE MUTATION");
		
		//take random connection to insert a node
		Connection con = randomConnection();
				
		//create new node to be inserted
		if(!con.isActive()) {
			System.out.println("active");
			//check if similar connection was already created in the past
			Node node = NetworkHandler.networkHandler.getNodeMadeOnConnection(con.getInovationNumber());
			Node newNode;
			//if node doesn't exist yet, create a new one
			if(node == null) {
				newNode = NetworkHandler.networkHandler.createNewNode(
						ActivationFunction.SIGMOID, 
						NodeType.HIDDEN,
						con.getInovationNumber()
				);
				
			//if already exists, transfer the inovation nubmer
			} else {
				newNode = new Node(
						node.getInovationNumber(),
						ActivationFunction.SIGMOID, 
						NodeType.HIDDEN,
						con.getInovationNumber()
				);
			}
	
			//create connection from beginning of the former connection to the new node 
			Connection newConnection1 = NetworkHandler.networkHandler.createNewConnectionWithSetWeight(
					con.getStartingNode(), 
					newNode, 
					1.0
			);

			//create connection from the new node to the end of former connection 
			Connection newConnection2 = NetworkHandler.networkHandler.createNewConnectionWithSetWeight(
					newNode, 
					con.getEndNode(),
					con.getWeight()
			);
			
			//add new node and connections to the genome
			this.nodes.put(newNode.getInovationNumber(), newNode);
			this.connections.put(newConnection1.getInovationNumber(), newConnection1);
			this.connections.put(newConnection2.getInovationNumber(), newConnection2);

						
			//deactivate former connection
			con.setActive(false);
		}
	}
	

	public void weightAdjustmentMutation() {
		//take random connection to insert a node
		Connection con = randomConnection();
		con.setWeight(con.getWeight() * (new Random().nextDouble() * 2));		
	}
	
	public void weightRandomizeMutation() {
		//take random connection to insert a node
		Connection con = randomConnection();
		con.randomizeWeight();		
	}
	
	public void connectionActivationMutation() {
		Connection con = randomConnection();
		con.setActive(!con.isActive());
	}
	
	
	//-------------------------------------------------------------------------HELPER FUNCTIONS------------------------------------------------------------------------
	

	private void print(int[] a) {
		for(int i = 0  ; i < a.length ; i++) {
			System.out.print(a[i] + ", ");
		}
	}
	
	
	
	private void resetNetwork() {
		//set sums of all nodes in network to 0
		for(Integer key : this.nodes.keySet()) {
			this.nodes.get(key).setWeightedSum(null);
		}
	}
	
	public void randomizeAllWeights() {
		
		for(Integer key : this.connections.keySet()) {
			this.connections.get(key).randomizeWeight();
		}
	}
	
	public void printWeights() {
		for(Integer key : this.connections.keySet())
			System.out.print("\t key:" + key + "  \tval: " + this.connections.get(key).getWeight());
		System.out.println();
	}
	
	public Genome cloneGenome() {	
		//create new node and connection hash maps
		Map<Integer, Node> outN = new HashMap<Integer, Node>();
		Map<Integer, Connection> outC = new HashMap<Integer, Connection>();
			
		//for each node create new node with same values and add it to the tmp map
		for(Integer key : this.nodes.keySet()) {
			
			outN.put(key, new Node(
				this.nodes.get(key).getInovationNumber(),
				this.nodes.get(key).getActivationFunction(),
				this.nodes.get(key).getType(),
				this.nodes.get(key).getMadeOnConnection()
			));
		
		}
		
		//for each connection create new connection with same values but link them to the just created nodes and add it to the tmp map
		for(Integer key : this.connections.keySet()) {
			if(this.connections.get(key).getEndNode() == null) {
				System.out.println("Null end node");
				System.out.println("start node: " + this.connections.get(key).getStartingNode() + "\t| " + this.connections.get(key).getStartingNode().getType() + "\t|  " + this.connections.get(key).getStartingNode().getInovationNumber());
			}
			outC.put(key, new Connection(
				this.connections.get(key).getInovationNumber(),
				outN.get(this.connections.get(key).getStartingNode().getInovationNumber()),
				outN.get(this.connections.get(key).getEndNode().getInovationNumber()),
				this.connections.get(key).getWeight(),
				this.connections.get(key).isActive()		
			));
		
		}
	
		return new Genome(outN, outC);
	}
	
	private Connection randomConnection() {
		//take array of all keys
		Object[] crunchifyKeys = this.connections.keySet().toArray();
		
		//select random key from the array of keys and pull the connection with corresponding key
		return this.connections.get(crunchifyKeys[new Random().nextInt(crunchifyKeys.length)]);
	}
	
	
	private int[] topologicalSortKeys() {
		//number of nodes
		int N = this.nodes.keySet().size();
		
		//array of sorted keys
		int[] sorted = new int[N];
		
		//visited nodes
		HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();
		//initialize all the nodes as unvisited
		for(Integer key : this.nodes.keySet()) visited.put(key, false);
		
		//index for ordered array
		int index = N - 1;
		
		//temporary variable to store the depth-first search reverse path
		ArrayList<Integer> path = new ArrayList<Integer>();
		
		//go through all nodes
		for(Integer key : visited.keySet()) {
			
			//if node not yet visited, do the depth fist search
			if(visited.get(key) == false) {
				
				//clear path in case it's not the first iteration
				path.clear();
				
				//perform the depth first search for node
				depthFirstSearch(key, visited, path);
				
				//store the path in sorted array in reverse order
				for(Integer pathKey : path) {
					
					//store key of node
					sorted[index] = pathKey;
					
					//decrement iterator
					index--;
				
				}
				
			}
		}
//		print(sorted);
//		System.out.println();
		return sorted;
	}
	
	public Map<Integer, Connection> getConnections(){
		return this.connections;
	}
	
	public Map<Integer, Node> getNodes() {
		return this.nodes;
	}

	public boolean containsNode(int inovationNumber) {
		for(Integer key : this.nodes.keySet()) {
			if(inovationNumber == key) return true;
		}
		return false;
	}
	
	private void depthFirstSearch(Integer key, HashMap<Integer, Boolean> visited, ArrayList<Integer> path) {
		
		//set the given key node as visited
		visited.put(key, true);
		
		//check all forward connected nodes
		for(Integer neighbour : this.nodes.get(key).getOutputNodeKeys()) {
			
			//check if node has not been visited
			if(visited.get(neighbour) == false) 
				
				//if the node was not visited make a recursive call for the DFS on that node
				depthFirstSearch(neighbour, visited, path);
		
		}
		
		//add the node to path of visited 
		path.add(key);
	}

	
	//-------------------------------------------------------------------------DISPLAY METHODS------------------------------------------------------------------------
	
	public void draw(Graphics2D g2d) {
		//temporary map of node point locations
		HashMap<Integer, Point> points = new HashMap<Integer, Point>();
		
		//count how many input nodes were iterated
		int inputLayerCounter = 0;
	
		//count how many output nodes were iterated
		int outputLayerCounter = 0;
		
		//count how many hidden nodes were iterated
		int hiddenLayerCounter = 0;
	
		//vertical margin between input nodes 
		int inputNodeMargin = Config.NETWORK_DISPLAY_HEIGHT / Config.NETWORK_INPUT_LAYER_SIZE;
		
		//vertical margin between output nodes
		int outputNodeMargin = Config.NETWORK_DISPLAY_HEIGHT / Config.NETWORK_OUTPUT_LAYER_SIZE;
		
		//vertical margin between hidden nodes
		int hiddenNodeMargin = Config.NETWORK_DISPLAY_HEIGHT / (this.nodes.size() - (Config.NETWORK_INPUT_LAYER_SIZE + Config.NETWORK_OUTPUT_LAYER_SIZE) + 1);

		
		//horizontal margin between hidden nodes (width / hidden nodes)
		int horizontalMargin = (Config.NETWORK_DISPLAY_WIDTH - Config.NETWORK_DISPLAY_MARGIN) 
				/ (this.nodes.size() - (Config.NETWORK_INPUT_LAYER_SIZE + Config.NETWORK_OUTPUT_LAYER_SIZE) + 1); 
		
		//System.out.println(Config.NETWORK_DISPLAY_HEIGHT / ((Config.NETWORK_INPUT_LAYER_SIZE + Config.NETWORK_OUTPUT_LAYER_SIZE) + 1));
		int[] sortedKeys = topologicalSortKeys();
		
		//iterator
		for(int key : sortedKeys) {
			
			//determine input node location on screen
			if(this.nodes.get(key).getType() == NodeType.INPUT) {
				
				
				//increment counter for the offset
				inputLayerCounter++;
				
				//calculate and store point
				points.put(
					key,
					new Point(
						Config.FRAME_WIDTH - Config.NETWORK_DISPLAY_WIDTH,
						inputLayerCounter * inputNodeMargin
					)
				);
				
				
			//determine output node location on screen
			}else if(this.nodes.get(key).getType() == NodeType.OUTPUT) {
				
				//increment counter for the offset
				outputLayerCounter++;
				
				
				//calculate and store point
				points.put(
					key,
					new Point(
						Config.FRAME_WIDTH - Config.NETWORK_DISPLAY_MARGIN,
						outputLayerCounter  * outputNodeMargin
					)
				);
				
			//determine hidden node location on screen
			}else if(this.nodes.get(key).getType() == NodeType.HIDDEN) {
				
				//increment counter for the offset
				hiddenLayerCounter++;
				
				//calculate and store point
				points.put(
					key,
					new Point(
						hiddenLayerCounter * horizontalMargin + Config.FRAME_WIDTH - Config.NETWORK_DISPLAY_WIDTH, 
						hiddenLayerCounter * hiddenNodeMargin
					)
				);
				
				
			}
			
			

		}
			
		//display nodes
		g2d.setColor(Color.ORANGE);
		for(Integer key : points.keySet()) {
			g2d.fillOval(
					points.get(key).x - Config.NETWORK_DISPLAY_NODE_RADIUS / 2, 
					points.get(key).y - Config.NETWORK_DISPLAY_NODE_RADIUS / 2, 
					Config.NETWORK_DISPLAY_NODE_RADIUS, 
					Config.NETWORK_DISPLAY_NODE_RADIUS);
		}
		
		
		Stroke tmp = g2d.getStroke();
		
		//display connections
		for(Integer key : this.connections.keySet()) {
			
			//positive connections are red color
			g2d.setColor(Color.RED);
			//negative connections are blue color
			if(this.connections.get(key).getWeight() < 0) g2d.setColor(Color.BLUE);
			
			//set thickness of line relative to the weight
			g2d.setStroke(new BasicStroke( (float) (Math.abs(this.connections.get(key).getWeight()))));
			
			//only display active nodes that don't have the weight equal to 0
			if(this.connections.get(key).isActive() && this.connections.get(key).getWeight() != 0) {
				g2d.drawLine(
						points.get(this.connections.get(key).getStartingNode().getInovationNumber()).x,
						points.get(this.connections.get(key).getStartingNode().getInovationNumber()).y,
						points.get(this.connections.get(key).getEndNode().getInovationNumber()).x,
						points.get(this.connections.get(key).getEndNode().getInovationNumber()).y
				);
			}
			
		}
		
		g2d.setStroke(tmp);
		
		
	}

	
	//-------------------------------------------------------------- SPECIATION METHODS ----------------------------------------------------------
	
	
	public double averageMatchingGeneWeightDifference(Genome genome) {
		int matches = 0;
		double sum = 0;
		for(Integer key : this.connections.keySet()) {
			if(genome.connections.containsKey(key)) {
				matches++;
				sum += genome.connections.get(key).getWeight() - this.connections.get(key).getWeight();
			}
		}
		return sum / matches;
	}

	public int disjointGenes(Genome genome) {
		int counter = 0;
		int thisInNum = this.biggestConnectionInovationNumber();
		int genInNum = genome.biggestConnectionInovationNumber();
		Set<Integer> keys = this.connections.keySet();
		keys.addAll(genome.connections.keySet());
		
		for(Integer key : keys) {
			//then it's already an excess gene
			if(key > thisInNum || key > genInNum) break;
			
			if((!this.connections.containsKey(key) && genome.connections.containsKey(key)) || 
				(this.connections.containsKey(key) && !genome.connections.containsKey(key)))
				counter ++;
		}
		
		return counter;
	}
	

	public int excessGenes(Genome genome) {
		int counter = 0;
		int thisInNum = this.biggestConnectionInovationNumber();
		int genInNum = genome.biggestConnectionInovationNumber();
		if(thisInNum > genInNum) {
			for(Integer key : this.connections.keySet())
				if(key > genInNum)
					counter++;
		}else {
			for(Integer key : genome.connections.keySet())
				if(key > thisInNum)
					counter++;
		}
		return counter;
	}
	
	
	public int biggestConnectionInovationNumber() {
		int biggest = Integer.MIN_VALUE;
		for(Integer key : this.connections.keySet())
			if(key > biggest) biggest = key;
		return biggest;
	}

	public int biggestNodeInovationNumber() {
		int biggest = Integer.MIN_VALUE;
		for(Integer key : this.nodes.keySet())
			if(key > biggest) biggest = key;
		return biggest;
	}


	
	
}
