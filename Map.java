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
		int n = 6;
		this.territories = new ArrayList<Territory>(0);


		System.out.println("Creating agents...");
		agents=new ArrayList<AgentController>(0);
		ContainerController cc = getContainerController();
		for (int i = 0; i < 3; i++)	{
			try {
				AgentController ac = cc.createNewAgent("A"+Integer.toString(i), "WarAgent", null);
				agents.add(ac);
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//agents.add(ac.getAgent("A"+Integer.toString(i)));
		}
		System.out.println("number of agents: "+Integer.toString(agents.size()));
		for (int i = 0; i < n; i++) {
			System.out.println("creating territory "+Integer.toString(i));
			this.territories.add(new Territory());
			//this.territories.get(i).setPlayer(this.agents.get(i%3));
			System.out.println("Territory "+Integer.toString(i)+" belongs to agent "+Integer.toString(i%3));
		}
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
		System.out.println("map is done");
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
