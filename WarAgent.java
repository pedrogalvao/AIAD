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
    private ArrayList<Behaviour> behaviours;

    public void setup()  {
        this.agentName = getAID().getName();
        System.out.println("Agent " + this.agentName + " setup");

        Object[] args = getArguments();

        this.territories = (ArrayList<game.Territory>) args[0];
        for (game.Territory T : this.territories) {
            T.setPlayer(this);
        }

        try {
            Thread.sleep(game.Map.freezeTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Adding behaviours
        this.behaviours = new ArrayList<Behaviour>();
        Behaviour attackingBehaviour = new WarBehaviour();
        behaviours.add(attackingBehaviour);
        addBehaviour(attackingBehaviour);
    }

    public void addTerritory(Territory T) {
        T.setPlayer(this);
        this.territories.add(T);
    }

    public void removeTerritory(Territory T) {
        this.territories.remove(T);
    }

    public ArrayList<Territory> getTerritories() {
        return territories;
    }

    public String getAgentName() {
        return this.agentName;
    }

    public void takeDown() {
        System.out.println("Agent " + this.getName() + " has died (out of territories");

        // Is it necessary since we are deleting below?
        for (Behaviour b : behaviours) {
            removeBehaviour(b);
        }

        doDelete();
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


        private void attackTerritory() {

            // If agent doesn't have any more territories, take down agent
            if (territories.size() == 0) {
                System.out.println("Agent " + getName() + " doesn't have any more territories so it can't attack. Take down.");
                takeDown();
                return;
            }

            // Select territories to attack and troop amount
            Territory[] srcDest = new Territory[2];
            int numTroops = selectAttack(srcDest);
            if (0 == numTroops)
                return;

            // If same owner, move troops. Else attack
            if (srcDest[0].getPlayer().getName().equals(srcDest[1].getPlayer().getName() ) )
            	moveTroops(srcDest[0], srcDest[1], numTroops);
			else
	            attackResults(srcDest[0], srcDest[1], numTroops);

            return;
        }

        public int selectAttack(Territory[] srcDest) {
            Random random = new Random();
            int t;
            Territory T1;
            int n = 0; // Default not attacking

            int srcCheck = 0;
            do {
                // Get origin of attack
                t = random.nextInt(territories.size()); // 0 inclusive size exclusive
                T1 = territories.get(t);

                // Check if this territory can attack. If not, get next territory.
                if (T1.troops < 2) {
                    // System.out.println("You cannot attack from a territory with only one troop");
                    t += 1;

                    if (t == territories.size() - 1)
                        t = 0;
                }

                srcCheck++;
            } while (T1.troops < 2 && n < territories.size()); // Loop until find a territory with more than 2 troops

            if (T1.troops < 2)
                return 0;

            int check = 0;
            Territory T2;
            // Get destiny of attack
			int f = random.nextInt(T1.frontiers.size());
			T2 = T1.frontiers.get(f);

            srcDest[0] = T1;
            srcDest[1] = T2;
            if (T1.troops > 1)
                n = T1.troops - 1;
            else
                n = 0;
            return n;
        }

        public void moveTroops(Territory T1, Territory T2, int n){
			// Check if number of troops are available. If not, move max (all -1)
        	if (n > T1.troops){
                n = T1.troops - 1;
                System.out.println("Moving too many troops. Now moving " + Integer.toString(n));
            }

        	T1.troops -= n;
			T2.troops += n;
		}

        public void attackResults(Territory T1, Territory T2, int n) {
            // Print attacking informations
            // From territory A with x troops to territory B with y troops.
            //System.out.println("Attack from Territory " + Integer.toString(T1.terID) + " (belongs to) " + T1.player.getName() + " with " + Integer.toString(n) + " troops, to Territory " + Integer.toString(T2.terID) + " with " + Integer.toString(T2.troops) + " troops");

            if (n >= T1.getTroops()) {
                //movimento invalido
                //System.out.println("invalid movement");
                n = T1.getTroops() - 1;
            }

            if (n < T2.getTroops()) {
                T1.removeTroops(n);
                T2.removeTroops(n);
                // Information after the attack
                //System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
                return;
            }

            // Conquer
            else if (n > T2.getTroops()) {
                T1.removeTroops(n);
                T2.setTroops(n - T2.getTroops());

                // Information about the attack
                // System.out.println("Player " + T1.player.getName() + " conquered Territory " + Integer.toString(T2.terID) + " from player " + T2.player.getName() + " and now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");

                // If conquered last territory, destroy player
                if (T2.player.getTerritories().size() == 1) {
                    System.out.println("Player " + T2.player.getName() + " has the following territories:");
                    for (game.Territory ter : T2.player.territories) {
                        System.out.println(ter.terID);
                    }
                    T2.player.takeDown();

                }

                // Remove territory from T2 owner and add to T1 owner (attacker)
                T2.player.removeTerritory(T2);
                addTerritory(T2);
                return;
            }

            // Stalemate
            else if (n == T2.getTroops()) {
                T1.removeTroops(n);
                T2.setTroops(1);
                // Information after the attack
                //System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
                return;
            }
        }


        public boolean done() {
            return false;
        }
    }
}
