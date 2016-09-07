package com.bezgachev.robocode.evolutionary;

import java.util.ArrayList;
import java.util.Random;

/**
 * The <strong>Node</strong> class represents a single node in a binary
 * expression tree. The class is designed to wwork with Genetic Programming and
 * its main purpose is to store genetic binary trees, as well as perform various
 * GP techniques, such as Crossover, Mutation, etc. </br> Much recursion awaits
 * you inside.
 * 
 * @author Slava Bezgachev
 *
 */
public class Node {

	/**
	 * The static variables that hold probability values for Terminals. Adjust
	 * as to experiment. LIVE - live result based, i.e. e.getHeading(). Event -
	 * stuff that are called on ScannedRobotEvent
	 */
	final static double TERMINAL_PROB_LIVE = 0.3, TERMINAL_PROB_CONSTANT = 0.3,
			TERMINAL_PROB_EVENT = 0.3, TERMINAL_PROB_RANDOM = 0.1;

	/**
	 * Array to hold Terminal probabilities. Should add up to 1
	 */
	final static double TERMINAL_PROB[] = { TERMINAL_PROB_LIVE,
			TERMINAL_PROB_CONSTANT, TERMINAL_PROB_EVENT, TERMINAL_PROB_RANDOM };

	/**
	 * Terminals that relate to situational numbers, such as getGunHeading,
	 * getHeight, etc.
	 */
	final static String LIVE[] = { "getEnergy()", "getHeading()", "getX()",
			"getY()", "getVelocity()", "getHeadingRadians()",
			"getDistanceRemaining()", "getGunHeadingRadians()",
			"getRadarHeadingRadians()" };
	/**
	 * Constant terminals, such as zero, random, etc.
	 */
	final static String CONSTANT[] = { /*"0.000001" a.k.a. zero ,*/
			"Math.random()", "Math.PI", "Math.random()*2-1", "Math.E"/* -1 to +1 */};
	/**
	 * Terminals related to events, for events that can only be called on
	 * ScannedRobotEvent.
	 */
	final static String EVENT[] = { "e.getBearingRadians()",
			"e.getVelocity()", "e.getHeadingRadians()" };
	/**
	 * A bunch of random constants, enable/disable to experiment
	 */
	final static String RANDOM[] = { /*"0.00001",*/ "-1", "0.5", "1", "1.5", "2", "2.5", "3" };

	/**
	 * 2D String arrya that contains the terminal expressions. 1st dimention - 0
	 * - Live, 1 - Constant, 2 - Event, 3 - Random 2nd dimention - Relevant
	 * expressions
	 */
	final static String TERMINAL_EXPRESSIONS[][] = { LIVE, CONSTANT, EVENT,
			RANDOM };

	/**
	 * 3D array that contains the function expressions. </br> 1 dimention array
	 * - Arity of 1, Arity of 2, Arity of 3, Arity of 4. </br> 2 dimention array
	 * - List of functions. </br> 3 dimention array - List of function members.
	 * Arity of 1 has 2, e.g. "Math.cos(" and ")". Arity of 2 has 3, and so on.
	 */
	final static String FUNCTION_EXPRESSIONS[][][] = {
			// Arity of zero. Non-applicable, so null, serves as a placeholder
			{ { null }, { null }, { null }, { null } },
			// Arity 1 functions. Abstract, Cos and Sin, Asin and Acos, and flip
			// positive/negative
			{ { "Math.abs(", ")" }, { "Math.cos(", ")" }, { "Math.sin(", ")" },
					{ "Math.asin(", ")" }, { "Math.acos(", ")" },
					{ "(-1) * ", "" } },
			// Arity 2 functions. Arithmetic functions, i.e. add, subtract,
			// multiply, divide
			{ { "", " + ", "" }, { "", " - ", "" }, { "", " * ", "" },
					{ "", " / ", "" }, { "Math.max(", ", ", ")" },
					{ "Math.min(", ", ", ")" } },
			// Arity 3 functions. A ternary operator, if positive, yes and no,
			// then if negative, yes and no.
			{ { "", " > 0 ? ", " : ", "" }, { "", " < 0 ? ", " : ", "" } },
			// Arity 4 functions. Is X > Y? If yes - Z, if no - A. Then same
			// with X==Y and X < Y
			{ { "", " >= ", " ? ", " : ", "" },
					{ "", " <= ", " ? ", " : ", "" } } };

	/**
	 * Probabilities of the arities for new function nodes. Arity = number of
	 * operators, and subsequently children in the tree
	 */
	final static double ARITY_PROB_1 = 0.2, ARITY_PROB_2 = 0.7,
			ARITY_PROB_3 = 0.05, ARITY_PROB_4 = 0.05;

	/**
	 * Array to hold the arity probabilities. Should add up to 1 or it might
	 * cause issues with deciding. Contained in an array to allow dynamic for
	 * loops that check for length of the array to decide. Can be extended to 5
	 * and 6, or shrank to 2 or 3 and shouldn't break anything below.
	 */
	final static double ARITY_PROB[] = { ARITY_PROB_1, ARITY_PROB_2,
			ARITY_PROB_3, ARITY_PROB_4 };

	// In case we introduce 5 or 6 in the future
	// ARITY_PROB_5 = 0.0,
	// ARITY_PROB_6 = 0.0;

	/**
	 * Chance to mutate
	 */
	final static double MUTATION_CHANCE = 0.15;

	/**
	 * Chance of mutating this Node
	 */
	final static double MUTATION_SUBTREE_CHANCE = 0.3;

	/**
	 * Probability that a new node becomes a terminal
	 */
	final static double TERMINAL_PROBABILITY = 0.2;

	/**
	 * Min and Max levels of how deep the trees should go. The minimum level is
	 * strict, the maximum isn't. Instead, to prevent robots from bloating
	 * (going too deep in the tree structure) we'll punish them by reducing the
	 * score if they go over the limit.
	 */
	final static int MIN_LEVEL = GPRunner.TREE_MIN_LEVEL,
			MAX_LEVEL = GPRunner.TREE_MAX_LEVEL;

	/**
	 * The array will hold children. A terminal node has 0 children, a function
	 * node can have from 1 to 4 (or maybe 5 or 6 in the future) children.
	 */
	Node childArray[];

	/**
	 * Arity represents the number of arguments. In out case it's the number of
	 * children nodes. Different functions have different arities.
	 */
	int arity = -1;

	/**
	 * The level represents the depth of the tree node, i.e. how far down the
	 * tree it is, i.e. how many parents it has.
	 */
	int level = -1;

	// Pretty self-explanatory, whether the node is a terminal node, i.e.
	// childless.
	boolean isTerminal;

	/**
	 * String array that will hold all of the expressions of the Node.
	 */
	String expression[];

	/**
	 * Used for probabilities.
	 */
	Random random = new Random();

	// CONSTRUCTORS
	/**
	 * Constructor.
	 * 
	 * @param depthLevel
	 *            - the initial depth level. Use 0 for a new tree
	 */
	public Node(int depthLevel) {
		this.level = depthLevel;
	}

	/**
	 * Class Constructor.
	 * 
	 * @param depthLevel
	 *            - Initial depth level, use 0 for a new tree.
	 * @param arity
	 *            - the number of operators the function Node has. Terminals
	 *            have 0.
	 * @param isTerminal
	 *            - whether the Node is a terminal Node.
	 */
	public Node(int depthLevel, int arity, boolean isTerminal) {
		this.level = depthLevel;
		this.arity = arity;
		this.isTerminal = isTerminal;
	}

	// METHODS

	/**
	 * Decide whether the node becomes a terminal or a function node, and set
	 * arity and isTerminal values.
	 * 
	 * TODO: If the trees are too deep, set a limit here in this function to
	 * prevent the spawning of function nodes below a certain depth level.
	 * 
	 */
	public void setArityAndType(int level) {
		// Decide if it's going to be a terminal or a function
		if (level > MIN_LEVEL && random.nextDouble() < TERMINAL_PROBABILITY
				|| level == MAX_LEVEL) {
			// Terminal
			this.arity = 0;
			this.isTerminal = true;
		} else {
			// Function
			this.isTerminal = false;
			// Decide arity
			double arityChance = random.nextDouble();

			// arityChance is between 0.0 and 1.0, excluding 1.0. Since the
			// probabilities in ARITY_PROB add up to one, they get subtracted
			// from arityChance until it's less than or equal to 0. Once it is
			// we know the number of operators.
			for (int i = 0; i < ARITY_PROB.length; i++) {
				if ((arityChance -= ARITY_PROB[i]) <= 0) {
					this.arity = i + 1;
					break;
				}
			}
			if (arityChance > 0) {
				System.out
						.println("Error: Method SetArity - overflow of the random double. Do the arity probabilities add up to 1.0? Random arity assigned, possibly a terminal now.");
				// Error, seems like we overstepped, assign a random arity from
				// 0 to max number of arities, set to isTerminal if needed.
				this.arity = random.nextInt(ARITY_PROB.length);
				if (this.arity == 0) {
					this.isTerminal = true;
				}
			}

			// Create a new child array with the needed number of children
			childArray = new Node[this.arity];
		}
	}

	/**
	 * Chooses a type of terminal, then chooses a random terminal of that type
	 * and assigns to the Node.
	 */
	public void assignTerminal() {

		expression = new String[1];
		// Kill children
		this.childArray = null;

		// Randomly decide the type of terminal
		double typeChance = random.nextDouble();

		for (int i = 0; i < TERMINAL_PROB.length; i++) {
			if ((typeChance -= TERMINAL_PROB[i]) <= 0) {
				// Similar to the method above, the terminal is assigned a
				// random type, then a random value from that type.
				expression[0] = TERMINAL_EXPRESSIONS[i][random
						.nextInt(TERMINAL_EXPRESSIONS[i].length)];
				break;
			}
		}
		if (typeChance > 0) {
			// Error, overflow of the random double. The likelihood is that the
			// Terminal probabilities don't add up to 1.0
			System.out
					.println("Error: Method assignTerminal, overflow of the random double. Do the terminal probabilities add up to 1.0? Random double assigned to the terminal.");
			expression[0] = Double.toString(random.nextDouble());
		}
	}

	/**
	 * Chooses a random expression based on the number of operators (i.e.
	 * arity). </br> Expressions are stored in a big array.
	 * 
	 * @param level
	 *            - the depth level of the Node.
	 */
	public void assignExpression(int level, int event) {
		// Check if terminal, assign terminal if yes
		if (this.arity == 0) {
			this.assignTerminal();
		} else {
			// Otherwise it's a function node. Assign it a random type of
			// expression depending on the arity.
			this.expression = FUNCTION_EXPRESSIONS[this.arity][random
					.nextInt(FUNCTION_EXPRESSIONS[this.arity].length)];

			// Create and grow children
			for (int i = 0; i < this.arity; i++) {
				childArray[i] = new Node(level + 1);
				childArray[i].grow(level + 1, event);
			}
		}

	}

	public void grow(int level, int event) {
		// Set arity and grow into a tree with children
		this.setArityAndType(level);
		this.assignExpression(level, event);
	}

	/**
	 * Recursive function. Sets the depth levels of the node and all child
	 * nodes.
	 * 
	 * @param level
	 *            - the depth level will be set to this. The depth levels of
	 *            child nodes will be higher by 1 per level.
	 */
	public void setDepthLevel(int level) {
		this.level = level;
		for (int i = 0; i < this.arity; i++) {
			this.childArray[i].setDepthLevel(level + 1);
		}
	}

	/**
	 * A recursive method. Takes the Node's expression, stuffs the children's
	 * expressions in, which stuffs it's children's expressions in, and so on,
	 * until we've stuffed all of the expressions in. </br>After that we output
	 * the result.
	 * 
	 * @return - The whole composed string of the tree's expression. Depending
	 *         on the tree depth can be anywhere from quite long to very long.
	 */
	public String createExpressionString() {

		String treeExpression = expression[0];

		for (int i = 0; i < this.arity; i++) {
			// Goes through every child, combining the the expressions into a
			// massive string.
			treeExpression += childArray[i].createExpressionString()
					+ expression[i + 1];

		}
		// Finally, make sure there's brackets around everything so that the
		// parent node picks us up correctly.
		treeExpression = "(" + treeExpression + ")";

		return treeExpression;
	}

	/**
	 * Recursive method. Uses recursion to count the total number of nodes
	 * starting with whichever one is called first. Includes the one that the
	 * method is called from.
	 * 
	 * @return - the number of Nodes.
	 */
	public int nodeCount() {
		int count = 1;
		// Recursively counts every node
		for (int i = 0; i < this.arity; i++) {
			count += this.childArray[i].nodeCount();
		}

		return count;
	}

	// OK, that should be everything for the generic class methods. Now onto GP
	// methods.
	/**
	 * Returns the depth of the deepest node it can find.
	 * 
	 * @return - Depth level of the deepest node
	 */
	public int deepestNode() {
		int num = this.level;
		// Recursively goes thorugh every child, compares the depth and assigns
		// the deepest
		for (int i = 0; i < this.arity; i++) {
			num = Math.max(childArray[i].deepestNode(), num);
		}

		return num;
	}

	/**
	 * Should find the depth of the highest Terminal node.
	 * 
	 * @return - depth of the highest terminal node.
	 */
	public int topTerminal() {
		// If terminal, return your depth level
		if (this.isTerminal) {
			return this.level;
		} else {
			// if not a terminal, set initial depth to the deepest node, then go
			// down the nodes and recursively compare depths until we hit a
			// terminal
			int dLevel = this.deepestNode();

			for (Node n : childArray) {
				dLevel = Math.min(dLevel, n.topTerminal());
			}
			return dLevel;
		}
	}

	/**
	 * Creates a new Node with the same properties and children as this Node.
	 */
	public Node clone() {
		// Create a new node, assign same values
		Node newNode = new Node(this.level, this.arity, this.isTerminal);
		newNode.expression = this.expression;

		// Go through all expressions and copy
		for (int i = 0; i < expression.length; i++) {
			newNode.expression[i] = this.expression[i];
		}

		// Set the children to null if terminal, clone children recursively if
		// function
		if (this.isTerminal) {
			newNode.childArray = null;
		} else {
			// For each child node, create a new empty one and assign values
			// respectively
			newNode.childArray = new Node[this.childArray.length];
			for (int i = 0; i < childArray.length; i++) {
				newNode.childArray[i] = this.childArray[i].clone();
			}
		}

		return newNode;
	}

	/**
	 * Recursive method. Returns a subtree. If getTerminal is true, navigates to
	 * a terminal and returns it. otherwise gets a non-root, non-terminal node
	 * at a random depth.
	 * 
	 * @param getTerminal
	 *            - whether you'd like to get a terminal
	 * @return - the subtree structure contained in a Node.
	 */
	public Node getSubTree(boolean getTerminal) {

		if (getTerminal) {
			if (this.arity == 0) {
				return this.clone();
			} else {
				return childArray[random.nextInt(this.arity)].getSubTree(true);
			}
		} else {
			int depth = 0;
			if (this.deepestNode() > 2) {
				depth = random.nextInt(this.deepestNode() - 1) + 1;
			} else {
				depth = 1;
			}
			
			return this.getNodeAtLevel(depth);
		}

	}

	/**
	 * Recursive method. Returns a Node at the specified level. Works by going
	 * through the paths that are deeper than targetDepth until the desired
	 * level is reached. If more than one path is available, paths are chosen
	 * randomly.
	 * 
	 * @param targetDepth
	 *            - the desired level at which the node is to be returned.
	 * @return - the Node found.
	 */
	private Node getNodeAtLevel(int targetDepth) {
		// Return this once we get at the required level
		if (this.level == targetDepth) {
			return this;
		} else {
			// Create an ArrayList of the potential paths which are deeper that
			// targetDepth
			
			//If terminal, return this
			if (this.arity == 0){
				return this;
			}
			ArrayList<Integer> potential = new ArrayList<Integer>();
			for (int i = 0; i < this.arity; i++) {
				// If the path ahead is deeper than needed, add the child number
				// as a potential path.
				if (childArray[i].deepestNode() >= targetDepth) {
					potential.add(i);
				}
			}
			// Choose one of the potential paths, repeat the method.
			int target = 0;
			if (potential.size() <=0){
				System.out.println("Error in getNodeAtLevel: no potential paths");
			} else {
				target = potential.get(random.nextInt(potential.size()));
			}
			
			return childArray[target].getNodeAtLevel(targetDepth);
		}
	}

	/**
	 * Selects a random depth and inserts the whole Node newNode into one of the
	 * local Nodes.
	 * 
	 * @param newNode
	 *            - the Node to insert
	 */
	public void insertNode(Node newNode) {
		// TODO revise this section after testing.
		// Okay, we'll just pick a random Node at a random depth and replace it.

		int deepestNode = newNode.deepestNode() - newNode.level;
		
		// To prevent an error
		if (deepestNode <= 0) {
			deepestNode = 1;
		}
		// Get a random depth to insert at
		int targetDepth = random.nextInt(deepestNode);
		int lowest = this.deepestNode();

		if (targetDepth > lowest) {
			this.insertNodeAtLevel(newNode, lowest);
		} else {
			this.insertNodeAtLevel(newNode, targetDepth);
		}

	}

	/**
	 * Recursive method. Goes through Nodes, inserts a node at a level specified
	 * to a random child.
	 * 
	 * @param node
	 *            - the Node we're working with.
	 * @param targetLevel
	 *            - the desired depth level.
	 */
	public void insertNodeAtLevel(Node node, int targetLevel) {
		// Once we get here the Node's depth level is the same as the target
		// depth level, tehrefore we simply replace it.
		if (this.level == targetLevel) {
			this.replaceNodeWith(node);
		} else {
			// Go through random paths that satisfy the target depth level, keep
			// track of which ones go deep enough
			ArrayList<Integer> potential = new ArrayList<Integer>();
			for (int i = 0; i < this.arity; i++) {
				if (childArray[i].deepestNode() >= targetLevel) {
					potential.add(i);
				}
			}
			// Choose a random paths from the ones selected above.
			int target = potential.get(random.nextInt(potential.size()));
			childArray[target].insertNodeAtLevel(node, targetLevel);
		}
	}

	/**
	 * Replace the Node tree with another Node tree. Can be used at any deapth
	 * level.
	 * 
	 * @param node
	 *            - Node to copy into the existing Node.
	 */
	public void replaceNodeWith(Node node) {
		// Copy values
		this.arity = node.arity;
		this.isTerminal = node.isTerminal;
		// Copy the expressions
		this.expression = new String[node.expression.length];
		for (int i = 0; i < this.expression.length; i++) {
			this.expression[i] = node.expression[i];
		}

		// Set the children
		if (node.isTerminal) {
			// NULL children if a terminal
			this.childArray = null;
		} else {
			// Copy the rest of the children
			this.childArray = new Node[this.arity];
			for (int i = 0; i < node.arity; i++) {
				this.childArray[i] = new Node(level + 1);
				this.childArray[i].replaceNodeWith(node.childArray[i]);
			}
		}
	}

	/**
	 * Recursive Method. Looks for a terminal using a random path, then mutates
	 * it by changing in into a new random terminal.
	 */
	public void mutateTerminal() {

		if (!this.isTerminal) {
			// if this isn't a terminal, use the method on a random child until
			// we get to a Terminal
			this.childArray[random.nextInt(this.arity)].mutateTerminal();
		} else {
			// If this IS a terminal, assign it a terminal which will assign it
			// a random terminal
			this.assignTerminal();
		}
	}

	public void mutateFunction() {

		// If we're at level 0, keep going
		if (this.level == 0) {
			if (this.arity == 0){
				this.assignTerminal();
			} else {
				this.childArray[random.nextInt(this.arity)].mutateFunction();
			}
			
		} else if (this.level == MAX_LEVEL - 1
				|| random.nextDouble() < MUTATION_SUBTREE_CHANCE) {
			// If we're getting to the bottom OR
			// if we're lucky, mutate by creating a new SubTree and replacing
			// this Node with the subtree Node
			Node newSubTree = new Node(this.level);
			newSubTree.grow(this.level, 0);
			this.replaceNodeWith(newSubTree);
		}
	}
}
