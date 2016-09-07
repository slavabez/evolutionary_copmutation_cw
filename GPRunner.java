package com.bezgachev.robocode.evolutionary;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class GPRunner {

	final static String[] adversaries = { "sample.TrackFire"/*,
			"sample.SittingDuck", "sample.SittingDuck", "sample.SittingDuck", "sample.SittingDuck"*/ };

	final static boolean USE_GUI = false;
	// If both are true, uses both by using the tournament select, then applying the roulete wheel on the ones selected.
	// Tournament size is specified in TOUR_SIZE
	final static boolean USE_TOURNAMENT_SELECT = false, USE_ROULETTE_WHEEL_SELECT = true;
	final static int POPULATION_SIZE = 100, GENERATION_CAP = 100,
			TREE_MIN_LEVEL = 2, TREE_MAX_LEVEL = 9, NUM_OF_ROUNDS = 3,
			TOUR_SIZE = 15;

	static double PROBABILITY_CROSSOVER = 0.8, PROBABILITY_DUPLICATION = 0.15,
			PROBABILITY_MUTATION = 0.05, ff_constant = 0.5,

			fitnesses[] = new double[POPULATION_SIZE], totalFitnesses,
			avgFitness, allOfAvgFitnesses[] = new double[GENERATION_CAP], avgNodes, totalNodes;
			
	static TestBot pool[] = new TestBot[POPULATION_SIZE],
			newPool[] = new TestBot[POPULATION_SIZE], bestYet, best5Bots[];

	static String candidates[] = new String[GENERATION_CAP];
	static String botNames[] = new String[POPULATION_SIZE];
	static String best5BotNames[] = new String[5];
	static int currentGeneration = 0;
	static Random random = new Random();

	public static void main(String[] args) {

		bestYet = new TestBot(-1, 0);
		bestYet.fitnessValue = 0;

		initialisePool();
		compilePoolBots();
		createFileHeaders();

		// Main generation loop
		while (currentGeneration < GENERATION_CAP) {

			// Set Bot names from the bot pool
			for (int j = 0; j < POPULATION_SIZE; j++) {
				botNames[j] = pool[j].BOTS_PACKAGE + "." + pool[j].robotName;
			}

			runAndRecordFitness(adversaries);

			totalFitnesses = 0;
			avgFitness = 0;
			avgNodes = 0;
			totalNodes = 0;

			ArrayList<TestBot> botList = new ArrayList<TestBot>();

			// Add all to the List
			for (int j = 0; j < POPULATION_SIZE; j++) {
				totalFitnesses += (pool[j].fitnessValue = fitnesses[j]);
				totalNodes += (pool[j].nodeCount);
				// i.e. the Double 'fitnessValue'
				botList.add(pool[j]);
			}
			// All bots have been added to the list
			// Sort (should sort by fitnesses as specified in the custom
			// comparator)
			Collections.sort(botList);

			// Should now be sorted by FitnessValues, not sure which way round
			// Add the top 5 elements to the best5BotNames

			for (int j = 0; j < 5; j++) {
				// This should take the bottom 5 results and assign their names
				// to
				// best5BotNames. Okay, I think best ones are at the top, so take top 5
				best5BotNames[j] = botList.get(j).robotName;
			}

			avgNodes = totalNodes / POPULATION_SIZE;
			avgFitness = totalFitnesses / POPULATION_SIZE;

			allOfAvgFitnesses[currentGeneration] = avgFitness;

			// Find the best in this generation
			// As we sorted by fitnesses already, the best shoulkd be the last
			// item on the botList ArrayList
			
			System.out.println("\nRound: " + currentGeneration
					+ "\nAvg Fitness:\t " + avgFitness
					+ "\nAvg Num of Nodes: \t" + avgNodes
					+ "\nBest in round: \t" + botList.get(botList.size()-1).robotName
					+ " - has " + botList.get(botList.size()-1).nodeCount
					+ " nodes.\nWorst in round: \t" + botList.get(0).robotName
					+ " - has " + botList.get(0).nodeCount
					+ " nodes.\nBest bot so far: " + bestYet.robotName
					+ " - has " + bestYet.fitnessValue + " fitness and "
					+ bestYet.nodeCount + " nodes.");
			// Record all the data to files.
			// In order: Generation number, Average Fitness, Best fitness,
			// Average Number of Nodes, Node Count of the best bot and the array
			// with 5 best names.
			recordDataToFiles(currentGeneration, avgFitness,
					botList.get(botList.size()-1).fitnessValue,
					avgNodes,
					botList.get(botList.size()-1).nodeCount, best5BotNames);
			
			
			ArrayList<Integer> botsNotToDelete = new ArrayList<Integer>();
			// Add the top 3 to the arraylist so that they're not deleted
			for (int x = 0; x < 3; x++){
				botsNotToDelete.add(botList.get(x).number);
			}
			
			// This should delete all but the top 3 bots
			TestBot.deleteOldBots(currentGeneration, POPULATION_SIZE, botsNotToDelete);
			
			currentGeneration++;
			
			System.out.println("Breeding now...");
			breedBotPool();
			
			pool = newPool;
			newPool = new TestBot[POPULATION_SIZE];
			
			System.out.println("On to compiling new bots...");
			compilePoolBots();
			
			

		}
		// End of While loop
		System.out.println("Finished, exiting the application...");
	}

	/**
	 * Creates a POPULATION_SIZE number of TestBots and initialises them
	 */
	private static void initialisePool() {
		System.out.println("Initialising the TestBots...");
		for (int i = 0; i < POPULATION_SIZE; i++) {
			pool[i] = new TestBot(0, i);
			pool[i].initialise();
		}
		System.out.println("Initialisation complete.");
	}

	/**
	 * Executes the construct() and compile() methods for each bot in the bot
	 * pool.
	 */
	private static void compilePoolBots() {
		System.out.println("Constructing and compiling bots from the pool.");
		for (TestBot bot : pool) {
			bot.construct();
			System.out.println("Bot " + bot.robotName + " constructed...");
			bot.compile();
			System.out.println("Bot " + bot.robotName + " compiled...");
		}
		System.out.println("Bots compiled.");
	}

	private static void recordDataToFiles(int round, double avgFitness,
			double bestFitness, double avgNumOfNodes, int bestNumOfNodes,
			String[] top5BotNames) {
		FileWriter fw;

		try {
			fw = new FileWriter(TestBot.BOTS_PATH + "\\all_data.csv", true);
			fw.write(round + ", " + avgFitness + ", " + bestFitness + ", "
					+ avgNumOfNodes + ", " + bestNumOfNodes + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH + "\\data_avg_fitness.csv",
					true);
			fw.write(round + "," + avgFitness + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH + "\\data_best_fitness.csv",
					true);
			fw.write(round + "," + bestFitness + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH
					+ "\\data_avg_number_of_nodes.csv", true);
			fw.write(round + "," + avgNumOfNodes + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH
					+ "\\data_best_number_of_nodes.csv", true);
			fw.write(round + "," + bestNumOfNodes + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH + "\\data_best_5_bots.csv",
					true);
			fw.write(round + ", ");
			for (int i = 0; i < top5BotNames.length; i++) {
				fw.write(top5BotNames[i] + ", ");
			}
			fw.write("\n");
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private static void createFileHeaders() {
		FileWriter fw;

		try {
			fw = new FileWriter(TestBot.BOTS_PATH + "\\all_data.csv", true);
			fw.write("Round" + ", " + "Average Fitness" + ", " + "Best Fitness" + ", "
					+ "Average Number of Nodes" + ", " + "Best Number of Nodes" + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH + "\\data_avg_fitness.csv",
					true);
			fw.write("Round" + "," + avgFitness + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH + "\\data_best_fitness.csv",
					true);
			fw.write("Round" + "," + "Best Fitness" + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH
					+ "\\data_avg_number_of_nodes.csv", true);
			fw.write("Round" + "," + "Average Number of Nodes" + "\n");
			fw.close();

			fw = new FileWriter(TestBot.BOTS_PATH
					+ "\\data_best_number_of_nodes.csv", true);
			fw.write("Round" + "," +  "Best Number of Nodes" + "\n");
			fw.close();

			

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	

	private static void runAndRecordFitness(String[] adversaries) {
		// Boolean is for visible/unvisible window
		BattleRunner arena = new BattleRunner(USE_GUI);
		fitnesses = arena.runRobotVsAdversaries(botNames, adversaries,
				NUM_OF_ROUNDS);
		for (int i = 0; i < POPULATION_SIZE; i++) {
			pool[i].fitnessValue = fitnesses[i];
		}

	}

	/**
	 * Breed a new generation
	 */
	@SuppressWarnings("unused")
	private static void breedBotPool() {

		double geneticOperator;
		int newPop = 0;

		while (newPop < POPULATION_SIZE) {
			geneticOperator = random.nextDouble();
			
			if (USE_TOURNAMENT_SELECT == true) {
				// Use tournament only
				if ((geneticOperator -= PROBABILITY_CROSSOVER) <= 0) {
					// Crossover of 2
					int p1 = tournamentSelect();
					int p2 = tournamentSelect();

					System.out.println("Crossing over bots " +p1+ " & " +p2+" -> " +newPop);

					newPool[newPop] = pool[p1].crossover(pool[p2],
							currentGeneration, newPop);
				} else if ((geneticOperator -= PROBABILITY_MUTATION) <= 0) {
					// Mutation
					newPool[newPop] = pool[tournamentSelect()].mutate(
							currentGeneration, newPop);
					System.out.println("Mutating bot");
				} else {
					// Replication / Duplication
					newPool[newPop] = pool[tournamentSelect()].duplicate(
							currentGeneration, newPop);
					System.out.println("Replicating Bot");
				}
			} else if (USE_ROULETTE_WHEEL_SELECT == true){
				// Use roulette only
				if ((geneticOperator -= PROBABILITY_CROSSOVER) <= 0) {
					// Crossover of 2
					int p1 = rouletteSelect();
					int p2 = rouletteSelect();

					System.out.println("Crossing over bots " +p1+ " & " +p2+" -> " +newPop);

					newPool[newPop] = pool[p1].crossover(pool[p2],
							currentGeneration, newPop);
				} else if ((geneticOperator -= PROBABILITY_MUTATION) <= 0) {
					// Mutation
					newPool[newPop] = pool[rouletteSelect()].mutate(
							currentGeneration, newPop);
					System.out.println("Mutating bot");
				} else {
					// Replication / Duplication
					newPool[newPop] = pool[rouletteSelect()].duplicate(
							currentGeneration, newPop);
					System.out.println("Replicating Bot");
				}
			}

			
			newPop++;
		}

	}

	@SuppressWarnings("unused")
	private static int tournamentSelect() {
		int size = TOUR_SIZE;
		TestBot[] subPool = new TestBot[size];
		for (int i = 0; i < size; i++) {
			subPool[i] = pool[random.nextInt(POPULATION_SIZE)];
		}
		if (USE_ROULETTE_WHEEL_SELECT == true) {
			// use roulette on this subpool
			int selected = rouletteSelect(subPool);
			return selected;
		} else {
			// simply select best from the subpool
			TestBot best = subPool[0];
			for (int i = 0; i < subPool.length; i++){
				if (subPool[i].fitnessValue > best.fitnessValue){
					best = subPool[i];
				}
			}
			return best.number;
		}		
	}
	
	
	
	private static int rouletteSelect() {
		int selectedBot = 0;
		double totalFitnesses = 0;
		// will hold all probabilities
		double probability[] = new double[POPULATION_SIZE];
		// will ad up to 1.0
		double weighedProbability[] = new double[POPULATION_SIZE];
		
		double sumOfProbabilities = 0;
		
		for (int i=0; i < pool.length; i++){
			totalFitnesses = totalFitnesses + pool[i].fitnessValue;
		}
		
		for (int i = 0; i < pool.length; i++){
			probability[i] = pool[i].fitnessValue / totalFitnesses;
			sumOfProbabilities = sumOfProbabilities + probability[i];
		}
		
		for (int i = 0; i < pool.length; i++){
			// shrink the probabilities so they add up to 1.0
			// divide by SumOfProb
			weighedProbability[i] = probability[i] / sumOfProbabilities;
		}
		
		// now get a new double from 0.0 to 1.0 and select a random bot.
		double rand = random.nextDouble();
		
		for (int i = 0; i < pool.length; i++){
			if ((rand -= weighedProbability[i]) <= 0) {
				// i is the chosen one
				return i;
			}
		}
		if (rand > 0){
			System.out.println("Error: Roulette Select overstepped...");
		}
		
		
		
		return selectedBot;
	}
	
	private static int rouletteSelect(TestBot selectPool[]) {
		int selectedBot = 0;
		double totalFitnesses = 0;
		// will hold all probabilities
		double probability[] = new double[selectPool.length];
		// will ad up to 1.0
		double weighedProbability[] = new double[selectPool.length];
		
		double sumOfProbabilities = 0;
		
		for (int i=0; i < selectPool.length; i++){
			totalFitnesses = totalFitnesses + pool[i].fitnessValue;
		}
		
		for (int i = 0; i < selectPool.length; i++){
			probability[i] = selectPool[i].fitnessValue / totalFitnesses;
			sumOfProbabilities = sumOfProbabilities + probability[i];
		}
		
		for (int i = 0; i < selectPool.length; i++){
			// shrink the probabilities so they add up to 1.0
			// divide by SumOfProb
			weighedProbability[i] = probability[i] / sumOfProbabilities;
		}
		
		// now get a new double from 0.0 to 1.0 and select a random bot.
		double rand = random.nextDouble();
		
		for (int i = 0; i < selectPool.length; i++){
			if ((rand -= weighedProbability[i]) <= 0) {
				// i is the chosen one
				return i;
			}
		}
		if (rand > 0){
			System.out.println("Error: Roulette Select (overloaded) overstepped...");
		}
		
		
		
		return selectedBot;
	}
}
