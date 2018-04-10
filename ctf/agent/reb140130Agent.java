package ctf.agent;
import ctf.common.AgentEnvironment;

import javax.swing.Action;
import java.util.ArrayList;
import java.util.Random;

import ctf.agent.Agent;
import ctf.common.AgentAction;


public class reb140130Agent extends Agent {
	static ArrayList<Double> hist= new ArrayList<>();
	public int getMove(AgentEnvironment inEnvironment){
		double[] weights ={0,0.1,0.1,0.1,0.1,0};
		boolean obstNorth = inEnvironment.isObstacleNorthImmediate();
		boolean obstSouth = inEnvironment.isObstacleSouthImmediate();
		boolean obstEast = inEnvironment.isObstacleEastImmediate();
		boolean obstWest = inEnvironment.isObstacleWestImmediate();
		double pi = Math.PI;
		double e = Math.E;


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


		if(inEnvironment.isFlagNorth(1,false)) weights[1] += 0.5;
		if(inEnvironment.isFlagSouth(1,false)) weights[2] += 0.5;
		if(inEnvironment.isFlagEast(1,false)) weights[3] += 0.5;
		if(inEnvironment.isFlagWest(1,false)) weights[4] += 0.5;

		if(inEnvironment.isFlagNorth(1,true)) weights[1] += 10;
		if(inEnvironment.isFlagSouth(1,true)) weights[2] += 10;
		if(inEnvironment.isFlagEast(1,true)) weights[3] += 10;
		if(inEnvironment.isFlagWest(1,true)) weights[4] += 10;


		if(inEnvironment.isAgentNorth(1,false)) weights[1] += -0.25;
		if(inEnvironment.isAgentSouth(1,false)) weights[2] += -0.25;
		if(inEnvironment.isAgentEast(1,false)) weights[3] += -0.25;
		if(inEnvironment.isAgentWest(1,false)) weights[4] += -0.25;

		if(inEnvironment.isAgentNorth(1,true)) weights[1] += -1;
		if(inEnvironment.isAgentSouth(1,true)) weights[2] += -1;
		if(inEnvironment.isAgentEast(1,true)) weights[3] += -1;
		if(inEnvironment.isAgentWest(1,true)) weights[4] += -1;

		double[] options = {e,e*-1,pi,pi*-1};
		double element;
		int choice;
		for(int j=0; j<options.length; j++){
			choice=j;
			double sum=0;
			hist.add(options[choice]);
			for(int i=0;i<hist.size();i++){
				element= hist.get(i);
				sum+=element;
			}
			if(sum==0.0){
				System.out.println(choice);
				weights[choice+1]=0.0;
			}
			hist.remove(hist.size()-1);
		}



		if(obstNorth) weights[1]=0;
		if(obstSouth) weights[2]=0;
		if(obstEast) weights[3]=0;
		if(obstWest) weights[4]=0;



		Random rand = new Random();
		int random = rand.nextInt(10)+1;
		int next, chosen=0;
		//System.out.println("weights");

		for(int i=0; i<weights.length; i++){
			//	System.out.println(weights[i]);
			if(weights[i]>=weights[chosen]){
				chosen=i;
			}
		}
		next = chosen;

		if(random <= 3){
			chosen =0;
			for(int i=0; i<weights.length; i++){
				//	System.out.println(weights[i]);
				if(weights[i]>weights[chosen] && i !=next){
					chosen=i;
				}
			}
		}

		if(hist.size()>2) hist.remove(0);


		switch(chosen){
		case 0:
			return AgentAction.DO_NOTHING;
		case 1:
			hist.add(pi);
			return AgentAction.MOVE_NORTH;
		case 2:
			hist.add(pi*-1);
			return AgentAction.MOVE_SOUTH;
		case 3:
			hist.add(e);
			return AgentAction.MOVE_EAST;
		case 4:
			hist.add(-e);
			return AgentAction.MOVE_WEST;
		case 5:
			return AgentAction.PLANT_HYPERDEADLY_PROXIMITY_MINE;
		default:
			return AgentAction.MOVE_EAST;
		}

	}
}
