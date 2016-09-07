package com.bezgachev.robocode.evolutionary;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class TestBot implements Comparator<TestBot>, Comparable<TestBot> {
	// Static variables
	final static String BOTS_PATH = "C:\\robocode\\robots\\sampleex";
	final String BOTS_PACKAGE = "sampleex";
	final static String ROBOCODE_JAR = "C:\\robocode\\libs\\robocode.jar";

	final static int NUM_OF_GENOMES = 5;

	final static double PROBABILITY_CROSSOVER_AT_ROOT = 0.1;
	final static double PROBABILITY_CROSSOVER_AT_TERMINAL = 0.1;
	final static double PROBABILITY_SKIP = 0.1;
	final static double MUTATION_PROB_AT_ROOT = 0.05;
	final static double MUTATION_PROB_AT_TERMINAL = 0.1;

	static Random random = new Random();

	// Class atributes

	int generation = 0, number = 0, nodeCount;
	private int successfulHits = 0;

	String robotName = new String();
	String phenome[] = new String[NUM_OF_GENOMES];
	String sourceCode = new String();
	String fileName;

	Node genes[] = new Node[NUM_OF_GENOMES];

	double fitnessValue;

	public TestBot(int gen, int robotID) {
		robotName = "KB_g" + Integer.toString(gen) + "_n"
				+ Integer.toString(robotID);
		this.generation = gen;
		this.number = robotID;
		this.fileName = BOTS_PACKAGE + robotName;
	}

	/**
	 * Initialise the bot by creating all of the trees for the genomes
	 */
	public void initialise() {

		for (int i = 0; i < NUM_OF_GENOMES; i++) {
			genes[i] = new Node(0);
			genes[i].grow(0, 0);
		}
	}

	public void construct() {

		for (int i = 0; i < NUM_OF_GENOMES; i++) {
			phenome[i] = genes[i].createExpressionString();
			this.writeCode();
		}

	}

	/**
	 * This method writes the code for the Robot, saves it as this.sourceCode
	 */
	public void writeCode() {
		this.sourceCode = "package " + this.BOTS_PACKAGE + ";\r\n" + "\r\n"
				+ "import robocode.*;\r\n" + "\r\n"
				+ "import java.awt.Color;\r\n" + "import java.awt.Color.*;\r\n"
				+ "\r\n" + "public class " + this.robotName
				+ " extends AdvancedRobot {\r\n" + "	public void run() {\r\n"
				+ "		this.setAdjustGunForRobotTurn(true);\r\n" + "\r\n"
				+ "		this.setColors(Color.RED, Color.BLACK, Color.WHITE);\r\n"
				+ "\r\n" + "		while (true) {\r\n"
				+ "			this.turnGunRight(Double.POSITIVE_INFINITY);\r\n" + "		}\r\n"
				+ "	}\r\n" + "	\r\n"
				+ "	public void onScannedRobot(ScannedRobotEvent e){\r\n"
				+ "		\r\n" + "		this.fire(" + this.phenome[0]
				+ ");\r\n" + "		this.setAhead(" + this.phenome[1] + ");\r\n"
				+ "		this.setTurnLeft(" + this.phenome[2] + ");\r\n" + "	}\r\n"
				+ "	\r\n"
				+ "	public void onHitByBullet(HitByBulletEvent e){\r\n"
				+ "		this.turnLeft(" + this.phenome[3] + ");"
				+ "		\r\n" + "		this.back(" + this.phenome[4] + ");\r\n"
				+ "	}\r\n" + "\r\n" + "}";
	}

	public int getNumberOfNodes() {
		this.nodeCount = 0;
		for (int i = 0; i < genes.length; i++) {
			this.nodeCount += this.genes[i].nodeCount();
		}
		return this.nodeCount;
	}

	/**
	 * Sets the depths to zero
	 */
	public void setDepths() {
		for (Node n : genes) {
			n.setDepthLevel(0);
		}
	}

	/**
	 * A method that takes the bot's source code and compiles it into a .class
	 * file, ready for deployment.
	 * 
	 * @return - The path of the compiled .class file.
	 */
	public String compile() {
		try {
			FileWriter fr = new FileWriter(BOTS_PATH + "\\" + this.robotName
					+ ".java");
			BufferedWriter bw = new BufferedWriter(fr);
			bw.write(this.sourceCode);
			bw.close();
		} catch (Exception e) {
			System.err
					.println("Error in method compile(); on line 122 in TestBot.java");
		}

		try {
			execute("javac -cp " + ROBOCODE_JAR + " " + BOTS_PATH + "\\"
					+ this.robotName + ".java");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (BOTS_PATH + "\\" + this.robotName + ".class");
	}

	/**
	 * Method that takes care of using the console to compile bots.
	 * 
	 * @param operators
	 * @throws Exception
	 */
	public static void execute(String operators) throws Exception {
		Process p = Runtime.getRuntime().exec(operators);
		printMessage(operators + " stdout:", p.getInputStream());
		printMessage(operators + " stderr:", p.getErrorStream());

		p.waitFor();
		if (p.exitValue() != 0) {
			System.out.println(operators + " exited with value "
					+ p.exitValue());
		}
	}

	/**
	 * Method that takes care of displaying messages
	 * 
	 * @param string
	 * @param is
	 * @throws Exception
	 */
	private static void printMessage(String string, InputStream is)
			throws Exception {
		String op = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		while ((op = br.readLine()) != null) {
			System.out.println(string + " " + op);
		}
	}

	public static void deleteOldBots(int generation, int popSize, ArrayList<Integer> ignoreIDs) {
		System.out.println("Deleting old bots we don't need...");
		File javaFile, classFile;
		
		for (int i = 0; i < popSize; i++){
			if (!ignoreIDs.contains(i)){
				// IF i is NOT in the ignore list, delte
				javaFile = new File(BOTS_PATH + "\\KB_g" + generation + "_n" + i + ".java");
				classFile = new File(BOTS_PATH + "\\KB_g" + generation + "_n" + i + ".class");
				
				//delete files
				javaFile.delete();
				classFile.delete();
				
			}
		}
		
		
	}

	public TestBot crossover(TestBot bot1, int generation, int botID) {
		TestBot result = new TestBot(generation, botID);

		for (int i = 0; i < NUM_OF_GENOMES; i++) {
			result.genes[i] = this.genes[i].clone();
		}

		// Choose a random chromosome from 1st and 2nd
		int chromosome1 = random.nextInt(NUM_OF_GENOMES);
		int chromosome2 = random.nextInt(NUM_OF_GENOMES);

		// The while looks makes sure the two are different.
		while (chromosome2 == chromosome1) {
			chromosome2 = random.nextInt(NUM_OF_GENOMES);
		}

		if (random.nextDouble() < PROBABILITY_CROSSOVER_AT_ROOT) {
			// Swap the genomes at the root level
			if (random.nextDouble() < PROBABILITY_SKIP) {
				// Swap different chromosomes
				result.genes[chromosome1]
						.replaceNodeWith(bot1.genes[chromosome2]);
			} else {
				// Swap the same chromosome
				result.genes[chromosome1]
						.replaceNodeWith(bot1.genes[chromosome1]);
			}
		} else {
			// Swap subtrees

			// t1 and t2 is whether to swap at the terminal levels
			boolean t1, t2;

			if (random.nextDouble() < PROBABILITY_CROSSOVER_AT_TERMINAL) {
				// Swap at the terminal level for 1
				t1 = true;
			} else {
				t1 = false;
			}

			if (random.nextDouble() < PROBABILITY_CROSSOVER_AT_TERMINAL) {
				// Swap at the terminal level for 2
				t2 = true;
			} else {
				t2 = false;
			}

			// Select random subtrees of bot1, crossover at the subtrees
			result.genes[chromosome1].insertNode(bot1.genes[chromosome1]
					.getSubTree(t1));
			result.genes[chromosome2].insertNode(bot1.genes[chromosome2]
					.getSubTree(t2));
			
			// Sort out and return the result
			result.setDepths();
			result.getNumberOfNodes();
			return result;

		}

		return result;
	}

	/**
	 * Mutates a random Node. Has a chance to mutate at root, a function node or
	 * a terminal.
	 * 
	 * @param generation
	 * @param botId
	 * @return
	 */
	public TestBot mutate(int generation, int botId) {

		TestBot result = new TestBot(generation, botId);

		for (int i = 0; i < NUM_OF_GENOMES; i++) {
			result.genes[i] = this.genes[i].clone();
		}

		int geneToMutate = random.nextInt(NUM_OF_GENOMES);

		if (random.nextDouble() < MUTATION_PROB_AT_ROOT) {
			result.genes[geneToMutate] = new Node(0);
			result.genes[geneToMutate].grow(0,0);
		}

		else if (random.nextDouble() < MUTATION_PROB_AT_TERMINAL) {
			result.genes[geneToMutate].mutateTerminal();
		} else {
			result.genes[geneToMutate].mutateFunction();
		}

		result.setDepths();
		result.getNumberOfNodes();
		return result;
	}

	/**
	 * Creates a new bot with identical stats
	 * 
	 * @param generation
	 * @param botID
	 * @return
	 */
	public TestBot duplicate(int generation, int botID) {
		TestBot result = new TestBot(generation, botID);

		for (int i = 0; i < NUM_OF_GENOMES; i++) {
			result.genes[i] = new Node(0);
			result.genes[i].replaceNodeWith(this.genes[i]);
		}
		result.setDepths();
		result.getNumberOfNodes();
		return result;

	}

	@Override
	public int compareTo(TestBot o) {
		return compare(this, o);
	}

	@Override
	public int compare(TestBot o1, TestBot o2) {
		// comparison by the Fitness value
		if (o1.fitnessValue < o2.fitnessValue) return -1;
		if (o1.fitnessValue > o2.fitnessValue) return 1;
		return 0;
	}

}