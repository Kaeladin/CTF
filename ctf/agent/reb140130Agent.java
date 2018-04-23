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
	boolean iHadFlag;
	double teleProb=0.0;

	ArrayList<int[]> pathHist = new ArrayList<int[]>();

	public void statelessSetup(AgentEnvironment inEnvironment){
		obstNorth = inEnvironment.isObstacleNorthImmediate();
		obstSouth = inEnvironment.isObstacleSouthImmediate();
		obstEast = inEnvironment.isObstacleEastImmediate();
		obstWest = inEnvironment.isObstacleWestImmediate();	

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

		if(iHadFlag && !inEnvironment.hasFlag()){
			teleProb+=5;
		}

		int chosen= 0;
		for(int i=0; i<weights.length; i++){
			if(prevWeights[i]>=prevWeights[chosen]){
				chosen=i;
			}
		}		

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

		if(teleProb>=5){
			position[0]=0;
			position[1]=0;
			pathHist.removeAll(pathHist);	
			teleProb=0;
		}



		if(pathHist.isEmpty()){
			if(inEnvironment.isObstacleSouthImmediate() && inEnvironment.isObstacleWestImmediate()){
				int[] pos = {0,0};
				pathHist.add(pos);
			}
			else if(inEnvironment.isObstacleSouthImmediate() && inEnvironment.isObstacleEastImmediate()){
				int[] pos = {0,0};
				pathHist.add(pos);
			}
			else if(inEnvironment.isObstacleNorthImmediate() && inEnvironment.isObstacleEastImmediate()){
				int[] pos = {0,0};
				pathHist.add(pos);
			}
			else if(inEnvironment.isObstacleNorthImmediate() && inEnvironment.isObstacleWestImmediate()){
				int[] pos = {0,0};
				pathHist.add(pos);
			}
		}



		int newState = fsm[state][getStateAction()];
		if(newState!=state){

			state=newState;
			pathHist.removeAll(pathHist);
		}

		enemyHad=enemyHas;
		hadFlag=hasFlag;
	}

	public void stateSetup(){
		//weights = {0,0.1,0.1,0.1,0.1,-10};


		weights[0]= -10;
		weights[1] = 0;
		weights[2] = 0;
		weights[3] = 0;
		weights[4] = 0;
		weights[5] = Double.NEGATIVE_INFINITY;


	}


	public int getStateAction(){

		if(!hadFlag && hasFlag) return 0;
		if(!enemyHad && enemyHas) return 1;
		if(hadFlag && !hasFlag ){
			if(tele) return 2;
			else return 3;
		}
		if(enemyHad && !enemyHas) return 4;

		return 5;
	}

	public void statelessHeuristic(){


		if(obstNorth) weights[1]=Double.NEGATIVE_INFINITY;
		if(obstSouth) weights[2]=Double.NEGATIVE_INFINITY;
		if(obstEast) weights[3]=Double.NEGATIVE_INFINITY;
		if(obstWest) weights[4]=Double.NEGATIVE_INFINITY;




		//System.out.println("pos"+position[0]+","+position[1]+"\n"+"Hist: ");
		double penScal= -30 / (pathHist.size()+1);
		boolean wasNorth=false, wasSouth=false, wasEast=false, wasWest=false;
		for(int i=pathHist.size()-1; i>=0; i--){
			int[] histp=pathHist.get(i);
			//System.out.println(histp[0]+","+histp[1]);
			if(histp[0]==position[0]){
				if(histp[1]==position[1]+1 && !wasNorth){
					weights[1]+=penScal*i;
					wasNorth=true;

				}

				if(histp[1]==position[1]-1 && !wasSouth){
					weights[2]+=penScal*i;
					wasSouth=true;

				}

				if(histp[1]==position[1]){
					weights[0]+=penScal*i;
				}
			}
			if(histp[1]==position[1] && !wasEast){
				if(histp[0]==position[0]+1){
					weights[3]+=penScal*i;
					wasEast=true;

				}

				if(histp[0]==position[0]-1 && !wasWest){
					weights[4]+=penScal*i;
					wasWest=true;
				}
			}
		}
	}

	public void stateHeuristic(AgentEnvironment inEnvironment){
		switch(state){
		case 1:
			if(inEnvironment.isFlagNorth(1,false)) weights[1] += 10;
			if(inEnvironment.isFlagSouth(1,false)) weights[2] += 10;
			if(inEnvironment.isFlagEast(1,false)) weights[3] += 10;
			if(inEnvironment.isFlagWest(1,false)) weights[4] += 10;

			if(inEnvironment.isFlagNorth(1,true)) weights[1] += 1000;
			if(inEnvironment.isFlagSouth(1,true)) weights[2] += 1000;
			if(inEnvironment.isFlagEast(1,true)) weights[3] += 1000;
			if(inEnvironment.isFlagWest(1,true)) weights[4] += 1000;


			if(inEnvironment.isAgentNorth(1,false)) weights[1] += -1;
			if(inEnvironment.isAgentSouth(1,false)) weights[2] += -1;
			if(inEnvironment.isAgentEast(1,false)) weights[3] += -1;
			if(inEnvironment.isAgentWest(1,false)) weights[4] += -1;

			//scale this to be dependent on how close you are to base
			if(inEnvironment.isAgentNorth(1,true)) weights[1] += -1000;
			if(inEnvironment.isAgentSouth(1,true)) weights[2] += -1000;
			if(inEnvironment.isAgentEast(1,true)) weights[3] += -1000;
			if(inEnvironment.isAgentWest(1,true)) weights[4] += -1000;
			
			if(inEnvironment.isAgentNorth(0,true)) weights[1] += -10;
			if(inEnvironment.isAgentSouth(0,true)) weights[2] += -10;
			if(inEnvironment.isAgentEast(0,true)) weights[3] += -10;
			if(inEnvironment.isAgentWest(0,true)) weights[4] += -10;
			break;

		case 2:

			if(inEnvironment.isFlagNorth(0,false)) weights[1] += 5;
			if(inEnvironment.isFlagSouth(0,false)) weights[2] += 5;
			if(inEnvironment.isFlagEast(0,false)) weights[3] += 5;
			if(inEnvironment.isFlagWest(0,false)) weights[4] += 5;

			if(inEnvironment.isFlagNorth(0,true)) weights[1] += 1000;
			if(inEnvironment.isFlagSouth(0,true)) weights[2] += 1000;
			if(inEnvironment.isFlagEast(0,true)) weights[3] += 1000;
			if(inEnvironment.isFlagWest(0,true)) weights[4] += 1000;

			if(inEnvironment.isAgentNorth(1,false)) weights[1] += 3;
			if(inEnvironment.isAgentSouth(1,false)) weights[2] += 3;
			if(inEnvironment.isAgentEast(1,false)) weights[3] += 3;
			if(inEnvironment.isAgentWest(1,false)) weights[4] += 3;

			if(inEnvironment.isAgentNorth(1,true)) weights[1] += 5;
			if(inEnvironment.isAgentSouth(1,true)) weights[2] += 5;
			if(inEnvironment.isAgentEast(1,true)) weights[3] += 5;
			if(inEnvironment.isAgentWest(1,true)) weights[4] += 5;
			
			if(inEnvironment.isAgentNorth(0,true)) weights[1] += -10;
			if(inEnvironment.isAgentSouth(0,true)) weights[2] += -10;
			if(inEnvironment.isAgentEast(0,true)) weights[3] += -10;
			if(inEnvironment.isAgentWest(0,true)) weights[4] += -10;
			break;

		case 3:

			if(inEnvironment.hasFlag()){
				if(inEnvironment.isBaseNorth(0,false)) weights[1] += 10;
				if(inEnvironment.isBaseSouth(0,false)) weights[2] += 10;
				if(inEnvironment.isBaseEast(0,false)) weights[3] += 10;
				if(inEnvironment.isBaseWest(0,false)) weights[4] += 10;

				if(inEnvironment.isBaseNorth(0,true)) weights[1] += Double.POSITIVE_INFINITY;
				if(inEnvironment.isBaseSouth(0,true)) weights[2] += Double.POSITIVE_INFINITY;
				if(inEnvironment.isBaseEast(0,true)) weights[3] += Double.POSITIVE_INFINITY;
				if(inEnvironment.isBaseWest(0,true)) weights[4] += Double.POSITIVE_INFINITY;


				if(inEnvironment.isAgentNorth(1,false)) weights[1] += -5;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += -5;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += -5;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += -5;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += -100000;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += -100000;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += -100000;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += -100000;
				
				if(inEnvironment.isAgentNorth(0,true)) weights[1] += -10;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += -10;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += -10;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += -10;
			}
			else{

	

				if(inEnvironment.isAgentNorth(0,true)) weights[1] += -10;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += -10;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += -10;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += -10;

				if(inEnvironment.isAgentNorth(1,false)) weights[1] += 5;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += 5;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += 5;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += 5;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += 10;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += 10;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += 10;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += 10;
			}
			break;

		case 4:

			if(inEnvironment.hasFlag()){
				if(inEnvironment.isBaseNorth(0,false)) weights[1] += 5;
				if(inEnvironment.isBaseSouth(0,false)) weights[2] += 5;
				if(inEnvironment.isBaseEast(0,false)) weights[3] += 5;
				if(inEnvironment.isBaseWest(0,false)) weights[4] += 5;

				if(inEnvironment.isBaseNorth(0,true)) weights[1] += 10;
				if(inEnvironment.isBaseSouth(0,true)) weights[2] += 10;
				if(inEnvironment.isBaseEast(0,true)) weights[3] += 10;
				if(inEnvironment.isBaseWest(0,true)) weights[4] += 10;


				if(inEnvironment.isAgentNorth(1,false)) weights[1] += -2;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += -2;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += -2;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += -2;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += -10;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += -10;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += -10;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += -10;
				
				if(inEnvironment.isAgentNorth(0,true)) weights[1] += -10;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += -10;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += -10;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += -10;

			}

			else{
				if(inEnvironment.isFlagNorth(0,false)) weights[1] += 1;
				if(inEnvironment.isFlagSouth(0,false)) weights[2] += 1;
				if(inEnvironment.isFlagEast(0,false)) weights[3] += 1;
				if(inEnvironment.isFlagWest(0,false)) weights[4] += 1;

				if(inEnvironment.isFlagNorth(0,true)) weights[1] += 10;
				if(inEnvironment.isFlagSouth(0,true)) weights[2] += 10;
				if(inEnvironment.isFlagEast(0,true)) weights[3] += 10;
				if(inEnvironment.isFlagWest(0,true)) weights[4] += 10;

				if(inEnvironment.isAgentNorth(1,false)) weights[1] += 3;
				if(inEnvironment.isAgentSouth(1,false)) weights[2] += 3;
				if(inEnvironment.isAgentEast(1,false)) weights[3] += 3;
				if(inEnvironment.isAgentWest(1,false)) weights[4] += 3;

				if(inEnvironment.isAgentNorth(1,true)) weights[1] += 5;
				if(inEnvironment.isAgentSouth(1,true)) weights[2] += 5;
				if(inEnvironment.isAgentEast(1,true)) weights[3] += 5;
				if(inEnvironment.isAgentWest(1,true)) weights[4] += 5;
				
				if(inEnvironment.isAgentNorth(0,true)) weights[1] += -10;
				if(inEnvironment.isAgentSouth(0,true)) weights[2] += -10;
				if(inEnvironment.isAgentEast(0,true)) weights[3] += -10;
				if(inEnvironment.isAgentWest(0,true)) weights[4] += -10;

				

			}
			break;

		}
	}
	public int getMove(AgentEnvironment inEnvironment){

		statelessSetup(inEnvironment);
		stateSetup();
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
		System.out.println("weights");

		for(int i=0; i<weights.length; i++){
			System.out.println(weights[i]);
			if(weights[i]>=weights[chosen]){
				chosen=i;
			}
		}

		for (int i=0; i<weights.length; i++){
			prevWeights[i]=weights[i];
		}

		iHadFlag=inEnvironment.hasFlag();

		if(pathHist.size()>20){
			pathHist.remove(0);
		}

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
