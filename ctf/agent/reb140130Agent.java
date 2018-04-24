package ctf.agent;
import ctf.common.AgentEnvironment;

import javax.swing.Action;
import java.util.ArrayList;
import java.util.Random;

import ctf.agent.Agent;
import ctf.common.AgentAction;


public class reb140130Agent extends Agent {
	int[] position={0,0};
	double[] weights = new double[6];
	double[] prevWeights=new double[6];
	static int state=1;
	boolean obstNorth, obstSouth, obstEast, obstWest;
	final int fsm[][] = {{0,0,0,0,0,0},{3,2,1,1,1,1},{4,2,2,2,1,2},{3,4,0,1,3,3},{4,4,0,2,3,4}};
	static boolean hadFlag = false, hasFlag = false, enemyHad = false, enemyHas = false, tele=false;
	boolean iHadFlag, enWasNorth,enWasSouth,enWasEast, enWasWest, baseWasNorth, baseWasSouth, baseWasEast, baseWasWest;
	double teleProb=0.0;
	int numAdjObst;

	//DNA
	double curiosity = -60;
	double caution = -3;
	double fear = -10;
	double politeness = -1000;
	double terror= -10000;
	double interest = 1;
	double desire = 50;
	double greed = 10000;

	ArrayList<int[]> pathHist = new ArrayList<int[]>();

	public void statelessSetup(AgentEnvironment inEnvironment){

		numAdjObst=0;
		obstNorth = inEnvironment.isObstacleNorthImmediate();
		obstSouth = inEnvironment.isObstacleSouthImmediate();
		obstEast = inEnvironment.isObstacleEastImmediate();
		obstWest = inEnvironment.isObstacleWestImmediate();	

		if(obstNorth){
			numAdjObst++;
		}

		if(obstSouth){
			numAdjObst++;
		}

		if(obstEast){
			numAdjObst++;
		}

		if(obstWest){
			numAdjObst++;
		}

		hasFlag=inEnvironment.hasFlag(0);
		enemyHas=inEnvironment.hasFlag(1);

		if(!enemyHas && !hasFlag && inEnvironment.isFlagWest(0, true)) obstWest = true;
		if(!enemyHas && !hasFlag &&inEnvironment.isFlagEast(0, true)) obstEast = true;
		if(!enemyHas && !hasFlag && inEnvironment.isFlagSouth(0, true)) obstSouth = true;
		if(!enemyHas && !hasFlag && inEnvironment.isFlagNorth(0, true)) obstNorth = true;
		/*
		if(inEnvironment.isAgentSouth(0,true)) obstSouth = true;
		if(inEnvironment.isAgentNorth(0,true)) obstNorth = true;
		if(inEnvironment.isAgentEast(0,true)) obstEast = true;
		if(inEnvironment.isAgentWest(0,true)) obstWest = true;

		 */

		int chosen= 0;
		for(int i=0; i<weights.length; i++){
			if(prevWeights[i]>=prevWeights[chosen]){
				chosen=i;
			}
		}		


		if(iHadFlag && !inEnvironment.hasFlag()){
			teleProb+=5;
		}

		if(baseWasNorth && inEnvironment.isBaseSouth(0,false)){
			teleProb+=5;
		}

		if(baseWasSouth && inEnvironment.isBaseNorth(0,false)){
			teleProb+=5;
		}

		if(baseWasEast && !inEnvironment.isBaseEast(0,false) ){
			if(chosen!=3){
				teleProb+=5;
			}
			else{
				teleProb+=2;
			}

			if(numAdjObst>=2){
				teleProb+=2;
			}
		}

		if(baseWasWest && !inEnvironment.isBaseWest(0,false)){
			if(chosen!=4){
				teleProb+=5;
			}
			else{
				teleProb+=2;
			}

			if(numAdjObst>=2){
				teleProb+=2;
			}		}



		if(chosen==0){
			for(int j=0; j<weights.length; j++){
				if(weights[j]==Double.NEGATIVE_INFINITY && prevWeights[j]!=Double.NEGATIVE_INFINITY){
					teleProb+=5;
				}

				if(weights[j]!=Double.NEGATIVE_INFINITY && prevWeights[j]==Double.NEGATIVE_INFINITY){
					teleProb+=5;
				}
			}
		}

		if (chosen==1){
			if (obstSouth){
				teleProb+=5;
			}

			if (enWasNorth){
				teleProb+=1;
			}
		}


		if (chosen==2){
			if (obstNorth){
				teleProb+=5;
			}

			if (enWasSouth){
				teleProb+=1;
			}
		}


		if (chosen==3){
			if (obstWest){
				teleProb+=5;
			}

			if (enWasEast){
				teleProb+=1;
			}
		}

		if (chosen==4){
			if (obstEast){
				teleProb+=5;
			}

			if (enWasWest){
				teleProb+=1;
			}
		}




		if(teleProb>=5){
			position[0]=0;
			position[1]=0;
			pathHist.removeAll(pathHist);	
			teleProb=0;
			//System.out.println("Teleported!");

		}



		if(pathHist.isEmpty()){
			int[] pos = {0,0};
			pathHist.add(pos);	
		}



		int newState = fsm[state][getStateAction()];
		if(newState!=state){


			state=newState;
			pathHist.removeAll(pathHist);
		}

		enemyHad=enemyHas;
		hadFlag=hasFlag;
	}

	public void stateSetup(AgentEnvironment inEnvironment){
		//weights = {0,0.1,0.1,0.1,0.1,-10};


		weights[0]= -10;
		weights[1] = 0;
		weights[2] = 0;
		weights[3] = 0;
		weights[4] = 0;
		weights[5] = Double.NEGATIVE_INFINITY;


	}


	public int getStateAction(){

		if(!hadFlag && hasFlag){
			return 0;
		}
		if(!enemyHad && enemyHas){
			return 1;
		}
		if(hadFlag && !hasFlag ){
			if(tele) {
				return 2;
			}
			else {
				return 3;
			}
		}
		if(enemyHad && !enemyHas){
			return 4;
		}

		return 5;
	}

	public void statelessHeuristic(){


		if(obstNorth) weights[1]=Double.NEGATIVE_INFINITY;
		if(obstSouth) weights[2]=Double.NEGATIVE_INFINITY;
		if(obstEast) weights[3]=Double.NEGATIVE_INFINITY;
		if(obstWest) weights[4]=Double.NEGATIVE_INFINITY;




		//System.out.println("pos"+position[0]+","+position[1]+"\n"+"Hist: ");
		int n = pathHist.size();
		double focus= (curiosity / Math.pow(n+1,1.5));
		boolean wasNorth=false, wasSouth=false, wasEast=false, wasWest=false;
		for(int i=pathHist.size()-1; i>=0; i--){
			int[] histp=pathHist.get(i);
			double scale=Math.pow(i, 1.5);
			//System.out.println(histp[0]+","+histp[1]);
			if(histp[0]==position[0]){
				if(histp[1]==position[1]+1){
					weights[1]+=focus*scale-20;
					wasNorth=true;

				}

				if(histp[1]==position[1]-1){
					weights[2]+=focus*scale-20;
					wasSouth=true;

				}

				if(histp[1]==position[1]){
					weights[0]+=focus*scale-20;
				}
			}
			if(histp[1]==position[1]){
				if(histp[0]==position[0]+1){
					weights[3]+=focus*scale-20;
					wasEast=true;

				}

				if(histp[0]==position[0]-1 ){
					weights[4]+=focus*scale-20;
					wasWest=true;
				}
			}
		}
	}

	public void stateHeuristic(AgentEnvironment inEnvironment){


		switch(state){
		case 1:
			/*
			if(inEnvironment.isAgentNorth(0,false)) weights[1] += interest;
			if(inEnvironment.isAgentSouth(0,false)) weights[2] += interest;
			if(inEnvironment.isAgentEast(0,false)) weights[3] += interest;
			 */
			if(inEnvironment.isFlagNorth(1,false)) weights[1] += desire/2;
			if(inEnvironment.isFlagSouth(1,false)) weights[2] += desire/2;
			if(inEnvironment.isFlagEast(1,false)) weights[3] += desire;
			if(inEnvironment.isFlagWest(1,false)) weights[4] += desire;

			if(inEnvironment.isFlagNorth(1,true)) weights[1] += greed;
			if(inEnvironment.isFlagSouth(1,true)) weights[2] += greed;
			if(inEnvironment.isFlagEast(1,true)) weights[3] += greed;
			if(inEnvironment.isFlagWest(1,true)) weights[4] += greed;



			if(inEnvironment.isAgentNorth(1,false)) weights[1] += caution;
			if(inEnvironment.isAgentSouth(1,false)) weights[2] += caution;
			if(inEnvironment.isAgentEast(1,false)) weights[3] += caution;
			if(inEnvironment.isAgentWest(1,false)) weights[4] += caution;

			//scale this to be dependent on how close you are to base
			if(inEnvironment.isAgentNorth(1,true)) weights[1] += terror;
			if(inEnvironment.isAgentSouth(1,true)) weights[2] += terror;
			if(inEnvironment.isAgentEast(1,true)) weights[3] +=  terror;
			if(inEnvironment.isAgentWest(1,true)) weights[4] += terror;

			if(inEnvironment.isAgentNorth(0,true)) weights[1] += fear;
			if(inEnvironment.isAgentSouth(0,true)) weights[2] += fear;
			if(inEnvironment.isAgentEast(0,true)) weights[3] += fear;
			if(inEnvironment.isAgentWest(0,true)) weights[4] += fear;


			if(inEnvironment.isBaseNorth(0,true)) weights[1] += terror;
			if(inEnvironment.isBaseSouth(0,true)) weights[2] += terror;
			if(inEnvironment.isBaseEast(0,true)) weights[3] += terror;
			if(inEnvironment.isBaseWest(0,true)) weights[4] += terror;

			if(inEnvironment.isAgentNorth(0,true)) weights[1] += politeness;
			if(inEnvironment.isAgentSouth(0,true)) weights[2] += politeness;
			if(inEnvironment.isAgentEast(0,true)) weights[3] += politeness;
			if(inEnvironment.isAgentWest(0,true)) weights[4] += politeness;
			break;

		case 2:

			if(inEnvironment.isBaseNorth(1,false)) weights[1] += desire/2;
			if(inEnvironment.isBaseSouth(1,false)) weights[2] += desire/2;
			if(inEnvironment.isBaseEast(1,false)) weights[3] += desire;
			if(inEnvironment.isBaseWest(1,false)) weights[4] += desire;

			if(inEnvironment.isFlagNorth(0,false)) weights[1] += desire/2;
			if(inEnvironment.isFlagSouth(0,false)) weights[2] += desire/2;
			if(inEnvironment.isFlagEast(0,false)) weights[3] += desire;
			if(inEnvironment.isFlagWest(0,false)) weights[4] += desire;

			if(inEnvironment.isFlagNorth(0,true)) weights[1] += greed;
			if(inEnvironment.isFlagSouth(0,true)) weights[2] += greed;
			if(inEnvironment.isFlagEast(0,true)) weights[3] += greed;
			if(inEnvironment.isFlagWest(0,true)) weights[4] += greed;

			if(inEnvironment.isAgentNorth(1,false)) weights[1] += caution;
			if(inEnvironment.isAgentSouth(1,false)) weights[2] += caution;
			if(inEnvironment.isAgentEast(1,false)) weights[3] += caution;
			if(inEnvironment.isAgentWest(1,false)) weights[4] += caution;

			if(inEnvironment.isAgentNorth(1,true)) weights[1] += caution;
			if(inEnvironment.isAgentSouth(1,true)) weights[2] += caution;
			if(inEnvironment.isAgentEast(1,true)) weights[3] += caution;
			if(inEnvironment.isAgentWest(1,true)) weights[4] += caution;

			if(inEnvironment.isAgentNorth(0,true)) weights[1] += politeness;
			if(inEnvironment.isAgentSouth(0,true)) weights[2] += politeness;
			if(inEnvironment.isAgentEast(0,true)) weights[3] += politeness;
			if(inEnvironment.isAgentWest(0,true)) weights[4] += politeness;
			break;

		case 3:

			if(inEnvironment.hasFlag()){
				if(inEnvironment.isBaseNorth(0,false)) weights[1] += desire/2;
				if(inEnvironment.isBaseSouth(0,false)) weights[2] += desire/2;
				if(inEnvironment.isBaseEast(0,false)) weights[3] += desire;
				if(inEnvironment.isBaseWest(0,false)) weights[4] += desire;

				if(inEnvironment.isBaseNorth(0,true)) weights[1] += Double.POSITIVE_INFINITY;
				if(inEnvironment.isBaseSouth(0,true)) weights[2] += Double.POSITIVE_INFINITY;
				if(inEnvironment.isBaseEast(0,true)) weights[3] += Double.POSITIVE_INFINITY;
				if(inEnvironment.isBaseWest(0,true)) weights[4] += Double.POSITIVE_INFINITY;


				if(inEnvironment.isAgentNorth(1,false)) weights[1] += caution;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += caution;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += caution;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += caution;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] +=  terror;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += terror;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += terror;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += terror;

				if(inEnvironment.isAgentNorth(0,true)) weights[1] += politeness;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += politeness;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += politeness;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += politeness;
			}
			else{



				if(inEnvironment.isAgentNorth(0,true)) weights[1] += politeness;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += politeness;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += politeness;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += politeness;

				if(inEnvironment.isAgentNorth(1,false)) weights[1] += desire;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += desire;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += desire;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += desire;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += greed;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += greed;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += greed;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += greed;


				if(inEnvironment.isBaseNorth(0,true)) weights[1] += terror;
				if(inEnvironment.isBaseSouth(0,true)) weights[2] += terror;
				if(inEnvironment.isBaseEast(0,true)) weights[3] += terror;
				if(inEnvironment.isBaseWest(0,true)) weights[4] += terror;

			}
			break;

		case 4:

			if(inEnvironment.hasFlag()){
				if(inEnvironment.isBaseNorth(0,false)) weights[1] += desire/2;
				if(inEnvironment.isBaseSouth(0,false)) weights[2] += desire/2;
				if(inEnvironment.isBaseEast(0,false)) weights[3] += desire;
				if(inEnvironment.isBaseWest(0,false)) weights[4] += desire;

				if(inEnvironment.isBaseNorth(0,true)) weights[1] += greed;
				if(inEnvironment.isBaseSouth(0,true)) weights[2] += greed;
				if(inEnvironment.isBaseEast(0,true)) weights[3] += greed;
				if(inEnvironment.isBaseWest(0,true)) weights[4] += greed;


				if(inEnvironment.isAgentNorth(1,false)) weights[1] += caution;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += caution;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += caution;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += caution;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += terror;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += terror;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += terror;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += terror;

				if(inEnvironment.isAgentNorth(0,true)) weights[1] += politeness;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += politeness;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += politeness;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += politeness;

			}

			else{
				if(inEnvironment.isFlagNorth(0,false)) weights[1] += interest;
				if(inEnvironment.isFlagSouth(0,false)) weights[2] += interest;
				if(inEnvironment.isFlagEast(0,false)) weights[3] += interest;
				if(inEnvironment.isFlagWest(0,false)) weights[4] += interest;

				if(inEnvironment.isFlagNorth(0,true)) weights[1] += greed;
				if(inEnvironment.isFlagSouth(0,true)) weights[2] += greed;
				if(inEnvironment.isFlagEast(0,true)) weights[3] += greed;
				if(inEnvironment.isFlagWest(0,true)) weights[4] += greed;

				if(inEnvironment.isAgentNorth(1,false)) weights[1] += desire;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += desire;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += desire;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += desire;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += greed;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += greed;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += greed;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += greed;

				if(inEnvironment.isAgentNorth(0,true)) weights[1] += politeness;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += politeness;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += politeness;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += politeness;



			}
			break;

		}
	}
	public int getMove(AgentEnvironment inEnvironment){

		statelessSetup(inEnvironment);
		stateSetup(inEnvironment);
		//System.out.println(pathHist.size());
		//System.out.print(position[0]+", "+position[1]+", ");
		//System.out.println(state);
		stateHeuristic(inEnvironment);
		statelessHeuristic();


		//return AgentAction.
		//DO_NOTHING
		//MOVE_SOUTH, MOVE_NORTH, MOVE_WEST, MOVE_EAST
		//PLANT_HYPERDEADLY_PROXIMITY_MINE


		//generate weights for each possible move and execute highest possible
		//A* -- use genetic algorithms to determine heuristic
		/* heuristic cases:
				1. neither team has a flag
				2. enemy team has your flag
				3. you have the enemy flag 
				4. both teams have the other flag		
				(3 and 4 player dependent on possession of flag)
		 */

		/*System.out.println("Nothing " + AgentAction.DO_NOTHING);
		System.out.println("South " + AgentAction.MOVE_SOUTH);
		System.out.println("North " + AgentAction.MOVE_NORTH);
		System.out.println("East " + AgentAction.MOVE_EAST);
		System.out.println("West " + AgentAction.MOVE_WEST);
		System.out.println("Bomb " + AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE);*/

		int chosen=0;
		//System.out.println("weights");

		ArrayList<Integer> highest = new ArrayList<>();
		for(int i=0; i<weights.length; i++){
			//System.out.println(weights[i]);
			if(weights[i]>weights[chosen]){
				highest.removeAll(highest);
				chosen=i;
			}

			if(weights[i]==weights[chosen]){
				if(highest.isEmpty()){
					highest.add(chosen);
				}
				highest.add(i);
			}

			if(!highest.isEmpty()){
				Random rand= new Random();
				int randChoose=rand.nextInt(highest.size());
				chosen=highest.get(randChoose);
			}
			System.out.println(highest.size()+","+chosen);




		}

		for (int i=0; i<weights.length; i++){
			prevWeights[i]=weights[i];
		}

		iHadFlag=inEnvironment.hasFlag();
		enWasNorth=inEnvironment.isAgentNorth(1,true);
		enWasSouth=inEnvironment.isAgentSouth(1,true);
		enWasEast=inEnvironment.isAgentEast(1,true);
		enWasWest=inEnvironment.isAgentWest(1,true);

		baseWasNorth=inEnvironment.isBaseNorth(0,false);
		baseWasSouth=inEnvironment.isBaseSouth(0,false);
		baseWasEast=inEnvironment.isBaseEast(0,false);
		baseWasWest=inEnvironment.isBaseWest(0,false);


		if(pathHist.size()>20){
			int[] toRemove = pathHist.get(0);
			//System.out.println("toRemove: "+toRemove[0]+","+toRemove[1]);
			//System.out.println("position: "+position[0]+","+position[1]);

			pathHist.remove(0);
		}

		int distanceToHome=Math.abs(position[0])+Math.abs(position[1]);
		//System.out.println("distanceToHome"+distanceToHome);
		int[] newPos = new int[2];

		switch(chosen){
		case 0:
			newPos[0]= position[0];
			newPos[1] = position[1];
			pathHist.add(newPos);
			return AgentAction.DO_NOTHING;
		case 1:
			position[1]+=1;
			newPos[0]= position[0];
			newPos[1] = position[1];
			pathHist.add(newPos);
			return AgentAction.MOVE_NORTH;
		case 2:
			position[1]+= -1;
			newPos[0]= position[0];
			newPos[1] = position[1];
			pathHist.add(newPos);
			return AgentAction.MOVE_SOUTH;
		case 3:
			position[0]+= 1;
			newPos[0]= position[0];
			newPos[1] = position[1];
			pathHist.add(newPos);
			return AgentAction.MOVE_EAST;
		case 4:
			position[0]+= -1;
			newPos[0]= position[0];
			newPos[1] = position[1];
			pathHist.add(newPos);
			return AgentAction.MOVE_WEST;
		case 5:
			return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
		default:
			return AgentAction.MOVE_EAST;
		}


	}


}
