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
    public static final String delimiterChar = " ";
    public static final String BREAK_ALLIANCE = "B";
    public static final String PROPOSE_ALLIANCE = "P";
    public static final String ACCEPT_ALLIANCE = "Y";
    public static final String REJECT_ALLIANCE = "N";
    public static final String REQUEST_INFO = "R";
    public static final String ATTACK = "A";
    public static final String MOVE = "M";

    protected static final long serialVersionUID = 1L;
    protected ArrayList<game.Territory> territories;
    protected String agentName;
    protected ArrayList<Behaviour> behaviours;
    protected AID mapAID;

    public void setup()  {
        this.agentName = getAID().getName();
        System.out.println("Agent " + this.agentName + " setup");
        this.mapAID = new AID("map0", AID.ISLOCALNAME);
        Object[] args = getArguments();

        this.territories = (ArrayList<game.Territory>) args[0];
        for (game.Territory T : this.territories) {
            T.setPlayer(this.getAID());
        }

        // Freeze time before game starts
        try {
            Thread.sleep(game.Map.freezeTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Adding behaviours
        this.behaviours = new ArrayList<Behaviour>();

        // Attacking behaviour
        Behaviour attackingBehaviour = new WarBehaviour();
        behaviours.add(attackingBehaviour);
        addBehaviour(attackingBehaviour);

        // Listening behaviour
        Behaviour listeningBehaviour = new WarListener(this);
        behaviours.add(listeningBehaviour);
        addBehaviour(listeningBehaviour);
    }

    public void addTerritory(game.Territory T) {
        this.territories.add(T);
    }
    public void removeTerritory(game.Territory ter) {
        this.territories.remove(ter);

        if (this.territories.size() == 0)
            this.takeDown();
    }
    public ArrayList<game.Territory> getTerritories() {
        return territories;
    }
    public String getAgentName() {
        return this.agentName;
    }
    public void takeDown() {
        System.out.println("Agent " + this.getName() + " has died");

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
        public static final long delay = 1000; // Delay so map can process messages

        public void action() {

            this.attackTerritory();
            //this.communicate();

            // Blocking agents so map can process all the messages
            block(delay);
        }
        private void attackTerritory() {

            // If agent doesn't have any more territories, take down agent
            if (territories.size() == 0) {
                System.out.println("Agent " + getName() + " doesn't have any more territories so it can't attack. Take down.");
                takeDown();
                return;
            }

            // Select territories to attack and troop amount
            game.Territory[] srcDest = new game.Territory[2];
            int numTroops = selectAttack(srcDest);
            if (0 == numTroops)
                return;

            // If same owner, move troops. Else attack
            if (srcDest[0].getPlayer().equals(srcDest[1].getPlayer() ) )
            	moveMessage(srcDest[0], srcDest[1], numTroops);
			else
	            attackMessage(srcDest[0], srcDest[1], numTroops);

            return;
        }
        public int selectAttack(game.Territory[] srcDest) {
            Random random = new Random();
            int t;
            game.Territory T1;
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
            game.Territory T2;
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
        public void attackMessage(game.Territory T1, game.Territory T2, int n) {
            //System.out.println("attackMessage");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(mapAID);
            msg.setContent("A" + game.Map.delimiterChar + Integer.toString(T1.getId()) + game.Map.delimiterChar + Integer.toString(n) + game.Map.delimiterChar + Integer.toString(T2.getId()));
            send(msg);
        }
        public void moveMessage(game.Territory T1, game.Territory T2, int n) {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(mapAID);
            msg.setContent("M" + game.Map.delimiterChar + Integer.toString(T1.getId()) + game.Map.delimiterChar + Integer.toString(n) + game.Map.delimiterChar + Integer.toString(T2.getId()));
            send(msg);
        }
        public boolean done() {
            return false;
        }
    }

    protected class WarListener extends Behaviour {

        private WarAgent player;

        public WarListener (WarAgent player){
            this.player = player;
        }

        public void action(){
            ACLMessage msg = this.player.receive();
            processMessage(msg);
        }

        public void processMessage(ACLMessage msg){
            if (msg == null)
                return;

            //System.out.println(msg.getContent());

            // Get sender and send msg to the right "inbox"
            AID msgSender = msg.getSender();

            if (msgSender.equals(mapAID)){
                mapMessage(msg);
                return;
            }

        }

        public void mapMessage(ACLMessage msg){
            /**
             * Receives msg from the map according to the following protocol:
             * Lost ("L"), Conquered ("C") or No status change ("N"). Territory (just "I" if movement was invalid)
             * Which territory (terID)
             * How many troops there are now in the territory
             *
             */

            String[] content = msg.getContent().split(game.Map.delimiterChar);

            // Invalid movement
            if (content[0].equals("I"))
                return;

            // If received message for game over, takedown
            if (content[0].equals("O")){
                takeDown();
                return;
            }

            // Lost territory
            if (content[0].equals("L")){
                int terID = Integer.parseInt(content[1]);
                game.Territory ter = null;

                for (game.Territory t : territories){
                    if (t.getId() == terID) {
                        ter = t;
                        break;
                    }
                }

                if (ter != null){
                    removeTerritory(ter);
                } else{
                    System.out.println("Removing err. Territory not found");
                }
                System.out.println("Removed territory");
                return;
            }

            // Conquered territory
            if (content[0].equals("C")){
                game.Territory ter = null;
                game.Territory tDest = null;
                int origID = Integer.parseInt(content[1]);
                int destID = Integer.parseInt(content[2]);

                System.out.println("Conquered territory");

                for (game.Territory t : territories){
                    if (t.getId() == origID){
                        ter = t;
                        break;
                    }
                }

                if (ter != null){
                    for (game.Territory t : ter.frontiers){
                        if (t.getId() == destID){
                            tDest = t;
                            break;
                        }
                    }

                    if (tDest != null){
                        addTerritory(tDest);
                        return;
                    } else {
                        System.out.println("Conquering ERR: Couldn't find conquered territory in frontiers from origin territory");
                    }

                } else{
                    System.out.println("Conquering ERR: Orign of conquering attack not found.");
                }
            }

            // Change status of territory (not yet implemented because currently acquiring information from map directly)
        }

        public boolean done() {
            return false;
        }
    }
}
