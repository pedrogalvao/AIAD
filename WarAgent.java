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
	private ArrayList<game.Territory> territories;
	private String agentName;

	public void setup() {
		this.agentName = getAID().getName().substring(0,2);
		System.out.println("Agent " + this.agentName + " setup");

		Object[] args = getArguments();

		this.territories = (ArrayList< game.Territory >) args[0];
		addBehaviour(new WarBehaviour());

		// System.out.println("setup is done");
	}

	 public void addTerritory(game.Territory T) {
		 T.setPlayer(this.agentName);
		 this.territories.add(T);
	 }

	 public void removeTerritory(game.Territory T) {
		 this.territories.remove(T);		 
	 }

	 public ArrayList<game.Territory> getTerritories() {
		return territories;
	 }

	 public String getAgentName() { return this.agentName; }

	 public void takeDown(){
		 System.out.println("Agent " + this.getName() + " has died (out of territories");
	 }


	 class WarBehaviour extends Behaviour {
	 	/**
		 * 
		 */
		public static final long serialVersionUID = 1L;

		public void action() {
			/*if (territories.size() == 0){
				takeDown();
				return;
			}*/
			this.attackTerritory();
			//this.communicate();
		}


		private void attackTerritory(){

			Random random = new Random();
			int t = 0;
			int n = 0;
			game.Territory T1;

			 do {
				 // Get origin of attack
				 if (territories.size() > 1) 	t = random.nextInt(territories.size()); // 0 inclusive size exclusive
				 else if (territories.size() == 1) t = 0;
				 else if (territories.size() == 0) {
					 //System.out.println("0");
				 	return;
				 }
				 T1 = territories.get(t);

				 if (!T1.getPlayer().equals(agentName)) {
				 	territories.remove(T1);
				 	continue;
				 }

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
			game.Territory T2;
			 do {
				int f = random.nextInt(T1.frontiers.size() -1);
				T2 = T1.frontiers.get(f);
				n++;
			 } while(T2.getPlayer().equals(agentName) && n < T1.frontiers.size());
			 if (T1.getPlayer().equals(T2.getPlayer())) return;
			 attack(T1,T2,T1.troops-1);
			 return;
		 }

	 	public void attack(game.Territory T1, game.Territory T2, int n) {
			// Print attacking informations
			// From territory A with x troops to territory B with y troops.
 			//System.out.println("Attack from Territory " + Integer.toString(T1.terID) + " with " + Integer.toString(n) + " troops, to Territory " + Integer.toString(T2.terID) + " with " + Integer.toString(T2.troops) + " troops");

 			if (n >= T1.getTroops()) {
	 			//movimento invalido
	 			//System.out.println("invalid movement");
	 			return;
	 		}
	 		else if (n < T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.removeTroops(n);
				// Information about the attack
				System.out.println("Player " + T1.getPlayer() + " attacked Territory " + Integer.toString(T2.terID) + " of player " + T2.getPlayer() + " and now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");

				// If conquered last territory, destroy player
				//if (T2.player.getTerritories().size() == 1)
				//	T2.player.takeDown();
				//System.out.println("Attack from terr." + Integer.toString(T1.terID) + " (" + T1.getPlayer() + ") to terr." + Integer.toString(T2.terID) + " (" + T2.getPlayer() + ") failed");
	 			return;
	 		}

	 		// Conquer
	 		else if (n > T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.setTroops(n -T2.getTroops());
	 			addTerritory(T2);
				System.out.println("Player " + T1.getPlayer() + " conquered Territory " + Integer.toString(T2.terID) + " from player " + T2.getPlayer() + " and now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
				// Information after the attack
				//System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
	 			return;
	 		}

	 		// Stalemate
	 		else if (n == T2.getTroops()) {
	 			T1.removeTroops(n);
	 			T2.setTroops(1);

				System.out.println("Player " + T1.getPlayer() + " attacked Territory " + Integer.toString(T2.terID) + " of player " + T2.getPlayer() + " and now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
	 			return;
	 		}
	 	}



		public boolean done() {
			return false;
		}
	}

}
