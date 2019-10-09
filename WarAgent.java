import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import java.util.ArrayList;


public class WarAgent extends Agent {
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Territory> territories;

	 public WarAgent(){
		 this.territories = new ArrayList<Territory>(0);
	 }
	 
	 protected void setup() {
		 addBehaviour(new WarBehaviour());
		 this.territories = new ArrayList<Territory>(0);
	 }

	 public void addTerritory(Territory T) {
		 T.setPlayer(this);
		 this.territories.add(T);
	 }

	 public void removeTerritory(Territory T) {
		 T.setPlayer(this);
		 this.territories.remove(T);		 
	 }


	 class WarBehaviour extends Behaviour {
	 	/**
		 * 
		 */
		public static final long serialVersionUID = 1L;

		public void action() {
	 		return;
		}
	 	
	 	public void attack(Territory T1, Territory T2, int n) {
	 		// ataque do territorio T1 para o territorio T2 com n tropas
	 		if (n >= T1.getTroops()) {
	 			//movimento invalido
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
