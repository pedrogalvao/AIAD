package game;

import game.Territory;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.Random;

public class Map extends Agent {

	private static final long serialVersionUID = 1L;
	private ArrayList<Territory> territories;
	private ArrayList<AgentController> agents;

	protected void setup() {
		int numberTerritories = 12;
		int numberAgents = 4;
		this.territories = new ArrayList<Territory>(0);
		this.agents=new ArrayList<AgentController>(0);

		ContainerController cc = getContainerController();

		for (int j=0; j < numberAgents; j++){
			ArrayList<game.Territory> agentsTerritories = new ArrayList<game.Territory>();
			Object[] args = new Object[1];


			System.out.println("Creating agent " + Integer.toString(j));
			args[0] = agentsTerritories;
			for (int i = 0; i < numberTerritories/numberAgents; i++) {
				System.out.println("creating territory "+Integer.toString(j*numberTerritories/numberAgents + i));
				game.Territory newTer = new game.Territory();
				this.territories.add(newTer); // Adding territory to map
				agentsTerritories.add(newTer); // Adding territory to agent
				newTer.setPlayer("A"+Integer.toString(j));
				System.out.println("Territory "+Integer.toString(newTer.terID)+" belongs to agent "+Integer.toString(j));
			}

			try {
				AgentController ac = cc.createNewAgent("A"+Integer.toString(j), "game.WarAgent", args=args);
				this.agents.add(ac);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			j++;

			agentsTerritories = new ArrayList<game.Territory>();
			args = new Object[1];

			System.out.println("Creating agent " + Integer.toString(j));
			args[0] = agentsTerritories;
			for (int i = 0; i < numberTerritories/numberAgents; i++) {
				System.out.println("creating territory "+Integer.toString(j*numberTerritories/numberAgents + i));
				game.Territory newTer = new game.Territory();
				this.territories.add(newTer); // Adding territory to map
				agentsTerritories.add(newTer); // Adding territory to agent
				newTer.setPlayer("A"+Integer.toString(j));
				System.out.println("Territory "+Integer.toString(newTer.terID)+" belongs to agent "+Integer.toString(j));
			}

			try {
				AgentController ac = cc.createNewAgent("A"+Integer.toString(j), "game.IntelligentWarAgent", args=args);
				this.agents.add(ac);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("number of agents created: " + Integer.toString(this.agents.size()));

		// Adding frontiers
		for (Territory T : this.territories) {
			Random random = new Random();
			int front = random.nextInt(3) + 2;
			for (int i = 0; i < front; i++) {
				Random random2 = new Random();
				int k = random2.nextInt(this.territories.size());

				// Check if the random territory to make frontier is itself
				if (this.territories.get(k).terID == T.terID){
					k++;
				}

				T.addFrontier(this.territories.get(k));
				this.territories.get(k).addFrontier(T); //as fronteiras sao bidirecionais
				System.out.println("frontier " + Integer.toString(T.terID)+ " " + Integer.toString(this.territories.get(k).terID) );
			}
		}

		System.out.println("map is created; starting agents");
		for (AgentController ac : this.agents) {
			try {
				ac.start();
				System.out.println("ac.start()");
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		addBehaviour(new MapBehaviour(this, 1000));
	}

	class MapBehaviour extends Behaviour {
		/**
		 *
		 */
		public static final long serialVersionUID = 1L;
		public final game.Map map;
		public final long delay;

		public MapBehaviour(game.Map map, long msecs){
			this.map = map;
			this.delay = msecs;
		}

		public void action() {

			block(this.delay);

			for (game.Territory T : this.map.territories) {
				T.troops += 2;
				//System.out.println("Added troops to territory. Territory " + Integer.toString(T.terID) + ". troops count is now: " + Integer.toString(T.troops));
			}

			Boolean done = true;
			System.out.println("Map status: ");
			for (int i =0; i < territories.size(); i++){
				game.Territory T = territories.get(i);
				if (done && !T.getPlayer().equals(territories.get(0).getPlayer()))
					done = false;
				System.out.println("Territory "+ Integer.toString(T.terID) + " Agent "+ T.getPlayer() + " Troops "+ T.getTroops());
			}

			if (done) {
				System.out.println("\nWar is over. Agent "+ territories.get(0).getPlayer() + " conquered all the territories.");
				doDelete();
			}


		}

		public boolean done() {
			return false;
		}
	}
}
