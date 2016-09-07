package com.bezgachev.robocode.evolutionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import robocode.BattleEndedEvent;
import robocode.BattleResults;
import robocode.control.*;
import robocode.control.events.*;

//
// Application that demonstrates how to run two sample robots in Robocode using the
// RobocodeEngine from the robocode.control package.
//
// @author Flemming N. Larsen
//
public class BattleRunner {

	RobocodeEngine engine;
	BattlefieldSpecification battlefield;
	BattleObserver battleObs;
	
	public static int totalTicks = 0;
	
	/**
	 * Constructor for the class
	 * 
	 * @param visible
	 *            - makes the Robocode window visible
	 */
	public BattleRunner(boolean visible) {
		engine = new RobocodeEngine(new java.io.File("C:/Robocode"));
		battleObs = new BattleObserver();
		engine.addBattleListener(battleObs);
		engine.setVisible(visible);
		battlefield = new BattlefieldSpecification(800, 600);
	}

/*	public double[] runWithSamples(String bots[], String samples[],
			int numOfRounds) {
		engine = new RobocodeEngine(new java.io.File("C:/Robocode"));

		double botFitnesses[] = new double[bots.length];
		String bot, adversary;
		BattleResults[] results;

		System.out.println("Running battles vs the sample batch");

		for (int i = 0; i < bots.length; i++) {
			double fitnessValue = 0;
			for (int j = 0; j < samples.length; j++) {
				bot = bots[i];
				adversary = samples[j];

				RobotSpecification[] selectedBots = engine
						.getLocalRepository(bot + ", " + adversary);
				BattleSpecification battleSpec = new BattleSpecification(
						numOfRounds, battlefield, selectedBots);
				engine.runBattle(battleSpec, true);

				results = battleObs.getResults();
				int myBot = (results[0].getTeamLeaderName().equals(bots[i]) ? 0
						: 1);
				int opBot = (myBot == 1 ? 0 : 1);
				int botScore = results[myBot].getScore();

				double totalScore = botScore + results[opBot].getScore();
				double roundFitness = (botScore) / totalScore;

				fitnessValue += roundFitness;
			}

			botFitnesses[i] = fitnessValue / samples.length;
		}

		return botFitnesses;

	}*/

	/**
	 * Runs the battles. Takes bots from killerBots one by one and matches them up against ALL of the adversaries AT ONCE for a certain number of rounds.
	 * @param killerBots - The killer bot array. Each one fights independently from others
	 * @param adversaries - The opponents. All spawn at once!
	 * @param numOfRounds - the number of rounds you'd like to run for
	 * @return
	 */
	public double[] runRobotVsAdversaries(String[] killerBots,
			String[] adversaries, int numOfRounds) {
		// Each bot from killerBots[] will run vs all members of adversaries[]
		// at once for the numOfRounds rounds.
		//engine = new RobocodeEngine(new java.io.File("C:/Robocode"));

		int botNum = killerBots.length;
		int advNum = adversaries.length;

		double[] allFitnesses = new double[botNum];

		String killerBot, adversary;

		//BattleResults[] results;
		List<BattleResults> results;
		

		System.out.println("Initialising the battles...");

		// For loop for killer bots
		for (int i = 0; i < botNum; i++) {
			// For each killer bot...
			// Set fitness to 0
			double fitnessValue = 0;
			killerBot = killerBots[i];
			
			//Reset the results
			results = new ArrayList<BattleResults>();

			// Create a comma delimited string of all robot names
			String selectedRobots = killerBot;
			for (int j = 0; j < advNum; j++) {
				selectedRobots += ("," + adversaries[j]);
			}

			RobotSpecification[] botsSpec = engine
					.getLocalRepository(selectedRobots);
			BattleSpecification battleSpec = new BattleSpecification(
					numOfRounds, battlefield, botsSpec);
			engine.runBattle(battleSpec, true);
			
			
			results = battleObs.getResults();
			
			// The results are sorted by ranks, winner at the top, losers at the
			// bottom, ordered by total score

			// Okay, time to think about the Fitness function.
			// We need to take other bots' scores into account

			// Z - the fitness value
			// X - the score of our robot
			// Y - the score of all opponents
			// The formula
			// z = 100 * (1 + (x - y) / (x + y))

			// Create a new HashMap
			// have to use Integer, not int as we can't use primitive types for
			// generic arguments
			Map<String, Integer> scores = new HashMap<String, Integer>();
			for (int k = 0; k < results.size(); k++) {
				// Put (Name, Score) into the HashMap
				scores.put(results.get(k).getTeamLeaderName(), new Integer(
						results.get(k).getScore()));
				
			}
			int x = scores.get(killerBot);
			
			// Take average of all bot's scores as y
			int sumOfY = 0;
			// Sum of ALL scores
			for (int sc : scores.values()) {
				sumOfY += sc;
			}
			// Subtract the killer's score from all
			sumOfY -= x;
			// Divide the sum of all Y scores by (number of bots - 1)
			double y = sumOfY / (scores.size() - 1);

			// y is now the average adversarry's score.

			// Fitness score
			// 0.000001 to prevent division by 0 (NaN)
			
			
			
			// NEW FITNESS FUNCTION = (100 / sqrt(0.001*t)) * (1 + (x+y)/(x-y)), 
			// where t is average number of rounds
			
			
			
			/*double z = (100 / (Math.sqrt(0.001 * avgRounds))) * (1 + (x - y) / (x + y + 0.00001));
			
			//Punish if x = 0
			
			if (x==0){
				z = z * 0.05;
			}*/
			//Let's try a different ftness function, a simpler one. The above one doesn't work at all...
			double z = 0;
			if (x!=0){
				z = (GPRunner.ff_constant + x) / (GPRunner.ff_constant + x + y);
				
			} /*else if (x == 0 & y > 0){
				//Robot's score is 0, opponents is higher
				z = 0.01;
			} */else {
				// robot's score is 0, assign 0
				z = 0;
			}
			
			
			// TODO If the killer bot has a max depth level > MAX_LEVEL, then his Fitness value will be artifically reduced to reduce bloat.
			
			// Add this bot's fitness value to all fitnesses
			allFitnesses[i] = z;
			
		}
		return allFitnesses;
	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * // Disable log messages from Robocode
	 * RobocodeEngine.setLogMessagesEnabled(false);
	 * 
	 * // Create the RobocodeEngine // RobocodeEngine engine = new
	 * RobocodeEngine(); // Run from current working directory RobocodeEngine
	 * engine = new RobocodeEngine(new java.io.File("C:/Robocode")); // Run from
	 * D:/Robocode
	 * 
	 * 
	 * 
	 * // Add our own battle listener to the RobocodeEngine
	 * engine.addBattleListener(new BattleObserver());
	 * 
	 * // Show the Robocode battle view engine.setVisible(true);
	 * 
	 * 
	 * 
	 * // Setup the battle specification
	 * 
	 * int numberOfRounds = 5; BattlefieldSpecification battlefield = new
	 * BattlefieldSpecification(800, 600); // 800x600 RobotSpecification[]
	 * selectedRobots =
	 * engine.getLocalRepository("sample.RamFire,sample.Corners, sample.SittingDuck"
	 * );
	 * 
	 * BattleSpecification battleSpec = new BattleSpecification(numberOfRounds,
	 * battlefield, selectedRobots);
	 * 
	 * // Run our specified battle and let it run till it is over
	 * engine.runBattle(battleSpec, true); // waits till the battle finishes
	 * 
	 * // Cleanup our RobocodeEngine engine.close();
	 * 
	 * // Make sure that the Java VM is shut down properly System.exit(0); } }
	 */
	//
	// Our private battle listener for handling the battle event we are
	// interested in.
	//
	class BattleObserver extends BattleAdaptor {
		//robocode.BattleResults[] results;
		List<BattleResults> resultList = new ArrayList<BattleResults>();
		
		public BattleObserver(){
			super();
		}
		@Override
		public void onBattleFinished(BattleFinishedEvent event) {
			System.out.println(System.currentTimeMillis() + " Battle finished, resetting the scores...");
			// Reset the scores
			resultList.clear();
			super.onBattleFinished(event);
			
		}
		
		// Called when the game sends out an information message during the
		// battle
		public void onBattleMessage(BattleMessageEvent e) {
			System.out.println("Msg> " + e.getMessage());
		}
		
		public void onRoundEnded(RoundEndedEvent e) {
			System.out.println("Round completed. Turns this rounds: "
					+ e.getTurns());
			System.out.println("Total turns in this battle: "
					+ e.getTotalTurns());
			totalTicks += e.getTotalTurns();
		}

		@Override
		public void onBattleStarted(BattleStartedEvent event) {
			// Reset the number of total ticks this round
			totalTicks = 0;
			super.onBattleStarted(event);
		}
		public void onBattleCompleted(BattleCompletedEvent e) {
			for (int i = 0; i < e.getIndexedResults().length; i++){
				resultList.add(e.getIndexedResults()[i]);
			}
		}
		
		public void onBattleEnded(BattleEndedEvent e) {
			resultList.add(e.getResults());
		}

		
		public List<BattleResults> getResults() {
			return resultList;
		}

		
		public void onBattleError(BattleErrorEvent e) {
			System.out.println("Error running battle: " + e.getError());
		}
		
		
	}
}