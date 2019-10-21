package game;

import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

public class Map extends Agent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Territory> territories;
	private ArrayList<AgentController> agents;

	protected void setup() {
		int numberTerritories = 6;
		int numberAgents = 3;
		this.territories = new ArrayList<Territory>(0);
		this.agents=new ArrayList<AgentController>(0);

		ContainerController cc = getContainerController();

		for (int j=0; j < numberAgents; j++){
			ArrayList<game.Territory> agentsTerritories = new ArrayList<game.Territory>();
			Object[] args = new Object[1];

			for (int i = 0; i < numberTerritories/numberAgents; i++) {
				System.out.println("creating territory "+Integer.toString(j*numberTerritories/numberAgents + i));
				game.Territory newTer = new game.Territory();
				this.territories.add(newTer); // Adding territory to map
				agentsTerritories.add(newTer); // Adding territory to agent
				System.out.println("Territory "+Integer.toString(i)+" belongs to agent "+Integer.toString(j));
			}

			System.out.println("Creating agent " + Integer.toString(j));
			args[0] = agentsTerritories;
			try {
				AgentController ac = cc.createNewAgent("A"+Integer.toString(j), "game.WarAgent", args=args);
				this.agents.add(ac);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("number of agents created: "+Integer.toString(this.agents.size()));

		// Adding frontiers
		for (Territory T : this.territories) {
			Random random = new Random();
			int front = random.nextInt(3) + 2;			 
			for (int i = 0; i < front; i++) {
				Random random2 = new Random();
				int k = random2.nextInt(this.territories.size());
				T.addFrontier(this.territories.get(k));
				System.out.println("frontier "+Integer.toString(i)+" "+Integer.toString(k));
			}
		}

		System.out.println("map is created; starting agents");
		for (AgentController ac : agents) {
			try {
				ac.start();
				System.out.println("ac.start()");
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

}
