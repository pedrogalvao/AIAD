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
	 
	public void setup() {
		this.agentName = getAID().getName();
		System.out.println("Agent " + this.agentName + " setup");

		Object[] args = getArguments();

		this.territories = (ArrayList< game.Territory >) args[0];
		addBehaviour(new WarBehaviour());

		// System.out.println("setup is done");
	}

	 public void addTerritory(Territory T) {
		 T.setPlayer(this);
		 this.territories.add(T);
	 }

	 public void removeTerritory(Territory T) {
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
			this.attackTerritory();
			//this.communicate();
		}


		private void attackTerritory(){

			Random random = new Random();
			int t;
			int n = 0;
			game.Territory T1;

			 do {
				 // Get origin of attack
				 t = random.nextInt(territories.size() -1);
				 T1 = territories.get(t);

				 // Check if this territory can attack. If not, get next territory.
				 if (T1.troops < 2){
					 // System.out.println("You cannot attack from a territory with only one troop");
					 t += 1;

					 if (t == territories.size() -1)
						 t=0;
				 }

				 n++;
			 } while(T1.troops < 2 && n < territories.size());

			 if (T1.troops < 2){
				 // System.out.println("You can no longer attack. Wait for more troops");
				 return;
			 }

			 // Get destiny of attack
			 int f = random.nextInt(T1.frontiers.size() -1);
			 Territory T2 = T1.frontiers.get(f);

			 attack(T1,T2,T1.troops-1);
			 return;
		 }

	 	public void attack(Territory T1, Territory T2, int n) {
			// Print attacking informations
			// From territory A with x troops to territory B with y troops.
 			System.out.println("Attack from Territory " + Integer.toString(T1.terID) + " with " + Integer.toString(n) + " troops, to Territory " + Integer.toString(T2.terID) + " with " + Integer.toString(T2.troops) + " troops");

 			if (n >= T1.getTroops()) {
	 			//movimento invalido
	 			System.out.println("invalid movement");
	 			return;
	 		}
	 		else if (n < T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.removeTroops(n);
				// Information after the attack
				System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
	 			return;
	 		}

	 		// Conquer
	 		else if (n > T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.setTroops(n -T2.getTroops());
	 			T2.setPlayer(T1.getPlayer());
				// Information after the attack
				System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
	 			return;
	 		}

	 		// Stalemate
	 		else if (n == T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.setTroops(1);
				// Information after the attack
				System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
	 			return;
	 		}
	 	}



		public boolean done() {
			return false;
		}
	}

}
