package game;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

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
    private AID mapAID;

    public void setup()  {
        this.agentName = getAID().getName();
        System.out.println("Agent " + this.agentName + " setup");
        mapAID = new AID("map", AID.ISLOCALNAME);
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
            	moveMessage(srcDest[0], srcDest[1], numTroops);
			else
	            attackMessage(srcDest[0], srcDest[1], numTroops);

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

                if (territories.size() == 0) {
                    System.out.println("Agent " + getName() + " doesn't have any more territories so it can't attack. Take down.");
                    takeDown();
                    return 0;
                }

                t = random.nextInt(territories.size()); // 0 inclusive size exclusive               }
                if (t >= territories.size()) t = 0;
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

        public void attackMessage(Territory T1, Territory T2, int n) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(mapAID);
            msg.setContent("A" + game.Map.delimiterChar + Integer.toString(T1.getId()) + game.Map.delimiterChar + Integer.toString(n) + game.Map.delimiterChar + Integer.toString(T2.getId()));
            //System.out.println("send");
            send(msg);
        }

        public void moveMessage(Territory T1, Territory T2, int n) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(mapAID);
            msg.setContent("M" + game.Map.delimiterChar + Integer.toString(T1.getId()) + game.Map.delimiterChar + Integer.toString(n) + game.Map.delimiterChar + Integer.toString(T2.getId()));
            //System.out.println("send");
            send(msg);
        }

        public boolean done() {
            return false;
        }
    }
}
