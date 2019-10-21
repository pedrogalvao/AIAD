package game;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import java.util.ArrayList;
import java.util.Random;

public class WarAgent extends Agent {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Territory> territories;
	private String agentName;
	 
	public void setup(ArrayList<Territory> territories) {

		this.agentName = getAID().getName();
		System.out.println("Agent " + this.agentName + " setup");
		this.territories = territories;
		addBehaviour(new WarBehaviour());
		System.out.println("setup is done");
	}

	 public void addTerritory(Territory T) {
		 T.setPlayer(this);
		 this.territories.add(T);
	 }

	 public void removeTerritory(Territory T) {
		 T.setPlayer(this);
		 this.territories.remove(T);		 
	 }

	 public void takeDown(){
	 	System.out.println("Done with war");
	 }


	 class WarBehaviour extends Behaviour {
	 	/**
		 * 
		 */
		public static final long serialVersionUID = 1L;

		public void action() {
			Random random = new Random();
			int t = random.nextInt(territories.size());
			Territory T1 = territories.get(t);
			int f = T1.frontiers.size();

			Territory T2 = territories.get(t).frontiers.get(f); 
			attack(T1,T2,T1.troops-1);	
	 		return;
		}
	 	
	 	public void attack(Territory T1, Territory T2, int n) {
	 		// ataque do territorio T1 para o territorio T2 com n tropas
 			System.out.println("attack with "+Integer.toString(n)+" troops");
	 		if (n >= T1.getTroops()) {
	 			//movimento invalido
	 			System.out.println("invalid movement");
	 			return;
	 		}
	 		else if (n < T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.removeTroops(n);
	 			return;
	 		}
	 		else if (n > T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.setTroops(n -T2.getTroops());
	 			T2.setPlayer(T1.getPlayer());
	 			
	 			return;
	 		}
	 		else if (n == T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.setTroops(1);
	 			return;
	 		}	 		 		
	 	}
	 	
		public boolean done() {
			return false;
		}
	}

}
