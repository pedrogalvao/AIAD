package game;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

import static java.lang.Thread.*;

public class Map extends Agent {

    /**
     *
     */
    // Global variables
    public static final long freezeTime = 1000;
    public static final String delimiterChar = " ";
    public static final String INFORM = "W";
    public static final String INVALID_MOVE = "I";

    public static final int numberTerritories = 18;
    public static final int numberAgents = 6;
    public static final int numAgentsColab = 2;
    public static final int numSmartAgents = 3;
    public static final int numRandomAgents = numberAgents - numSmartAgents;

    private static final long serialVersionUID = 1L;
    private ArrayList<game.Territory> territories;
    private ArrayList<AgentController> agents;

    protected void setup() {
        this.territories = new ArrayList<game.Territory>(0);
        this.agents=new ArrayList<AgentController>(0);
        ContainerController cc = getContainerController();

        // Creating territories and agents
        int numSmart = 0;
        for (int j=0; j < numberAgents; j++){
            ArrayList<game.Territory> agentsTerritories = new ArrayList<game.Territory>();
            Object[] args = new Object[3];

            // Creating territories
            for (int i = 0; i < numberTerritories/numberAgents; i++) {
                System.out.println("creating territory "+Integer.toString(j*numberTerritories/numberAgents + i));
                game.Territory newTer = new game.Territory();
                this.territories.add(newTer); // Adding territory to map
                agentsTerritories.add(newTer); // Adding territory to agent
                System.out.println("Territory "+Integer.toString(newTer.terID)+" belongs to agent "+Integer.toString(j));
            }

            // Creating agent to get the territories created above
            System.out.println("Creating agent " + Integer.toString(j));
            args[0] = agentsTerritories;

            if (j < numAgentsColab)
                args[1] = new float[]{-1,0,0,0,0,0,0};
            else
                args[1] = new float[]{-1,0,0,1,0,0,1};
            args[2] = numberAgents;

            try {
                AgentController ac;
                if (j < numRandomAgents)
                    ac = cc.createNewAgent("A"+Integer.toString(j), "game.WarAgent", args=args);
                else
                    ac = cc.createNewAgent("S"+Integer.toString(j), "game.IntelligentWarAgent", args=args);
                this.agents.add(ac);
                ac.start();
                numSmart++;
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("number of agents created: " + Integer.toString(this.agents.size()));

        // Adding frontiers
        for (game.Territory T : this.territories) {
            Random random = new Random();
            int front = random.nextInt(3) + 4;
            for (int i = 0; i < front; i++) {
                Random random2 = new Random();
                int k = random2.nextInt(this.territories.size());

                // Check if the random territory to make frontier is itself
                if (this.territories.get(k).terID == T.terID){
                    // If it is the last territory, then make frontier with the first one
                    if (this.territories.size() == k + 1){
                        k=0;
                    }
                    else{
                        k++;
                    }
                }

                T.addFrontier(this.territories.get(k));
                this.territories.get(k).addFrontier(T); // bidirectional frontiers
                System.out.println("frontier " + Integer.toString(T.terID)+ " " + Integer.toString(this.territories.get(k).terID) );
            }
        }

        try {
            sleep(freezeTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addBehaviour(new MapListenerBehaviour(this));
        addBehaviour(new MapBehaviour(this, 1000));
    }
    public void attackResults(game.Territory T1, game.Territory T2, int n) {
        // Print attacking informations
        // From territory A with x troops to territory B with y troops.
        System.out.println("Attack from territory "+Integer.toString(T1.getId())+" to territory "+Integer.toString(T2.getId())+" with "+Integer.toString(n));
        // If number of troops is higher than territory amount, send the maximum
        // TODO: Change that once agents are smarter. They might want to re-evaluate
        if (n >= T1.getTroops()) {
            //movimento invalido
            //System.out.println("invalid movement");
            n = T1.getTroops() - 1;
        }

        // Result: No status change
        if (n < T2.getTroops()) {
            T1.removeTroops(n);
            T2.removeTroops(n);

            // Inform players involved
            informAgent(T1.getPlayer(), "N", T1.getId(), T1.troops);
            informAgent(T2.getPlayer(), "N", T2.getId(), T2.troops);
            return;
        }

        // Conquer
        else if (n > T2.getTroops()) {
            T1.removeTroops(n);
            T2.setTroops(n - T2.getTroops());

            // Inform player 1 about previous territory and conquered territory
            informAgent(T1.getPlayer(), "N", T1.getId(), T1.troops);
            informAgent(T1.getPlayer(), "C", T1.getId(), T2.getId(), T2.troops);

            // Inform player 2 about lost territory
            informAgent(T2.getPlayer(), "L", T2.getId(), T2.troops);

            // Remove territory from T2 owner and add to T1 owner (attacker)
            T2.setPlayer(T1.getPlayer());
            return;
        }
        // Stalemate
        else if (n == T2.getTroops()) {
            T1.removeTroops(n);
            T2.setTroops(1);

            // Inform players involved
            informAgent(T1.getPlayer(), "N", T1.getId(), T1.troops);
            informAgent(T2.getPlayer(), "N", T2.getId(), T2.troops);
            return;
        }
    }

    public void moveTroops(game.Territory T1, game.Territory T2, int n){
        // Check if number of troops are available. If not, move max (all -1)
        if (n > T1.troops){
            n = T1.troops - 1;
            System.out.println("Moving too many troops. Now moving " + Integer.toString(n));
        }

        T1.troops -= n;
        T2.troops += n;

        // Inform player
        informAgent(T1.getPlayer(), "N", T1.getId(), T1.troops);
        informAgent(T1.getPlayer(), "N", T2.getId(), T2.troops);
        return;
    }

    public void informAgent(AID player, String status, int terID, int numTroops){
        /**
         * Informs the player about changes in the territory according to the following protocol:
         * Lost ("L"), Conquered ("C") or No status change ("N"). Territory (just "I" if movement was invalid)
         * Which territory (terID)
         * How many troops there are now in the territory
         *
         */

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(player);

        String msgCont = status + this.delimiterChar + Integer.toString(terID) + this.delimiterChar + Integer.toString(numTroops);
        msg.setContent(msgCont);
        send(msg);
    }
    public void informAgent(AID player, String status, int terIDOrig, int terIDDest, int numTroops){
        /**
         * Informs the player about changes in the territory according to the following protocol:
         * Lost ("L"), Conquered ("C") or No status change ("N"). Territory (just "I" if movement was invalid)
         * Which territory (terID)
         * Msg for conquered territory also includes orign of attack, so we can add the territory from inside the player via frontiers
         * How many troops there are now in the territory
         *
         *
         */

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(player);

        String msgCont = status + this.delimiterChar + Integer.toString(terIDOrig) + this.delimiterChar + Integer.toString(terIDDest) + Integer.toString(numTroops);
        msg.setContent(msgCont);
        send(msg);
    }
    public void takeDown(){
        this.territories = null;

        for (AgentController ac : this.agents){
            if (ac != null) {
                try {
                    ac.kill();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        }

        this.agents = null;
        doDelete();
    }


    class MapBehaviour extends Behaviour {
        /**
         *
         */
        public static final long serialVersionUID = 1L;
        public final game.Map map;
        public final long delay;
        public int rounds;
        public MapBehaviour(game.Map map, long msecs){
            this.map = map;
            this.delay = msecs;
            this.rounds = 0;
        }

        public void action() {
            block(this.delay);
            rounds++;


            // If not, add troops and inform game status
            System.out.println("Map status:");
            Boolean done = true;
            String P0 = this.map.territories.get(0).getPlayer().getLocalName();
            for (game.Territory T : this.map.territories) {
                T.troops += 2;
                if (!(T.getPlayer() == null)) {
                    if (!T.getPlayer().getLocalName().equals(P0)) done = false;
                    System.out.println("Territory " + Integer.toString(T.terID) + " belongs to " + T.getPlayer().getLocalName() + " with troops: " + Integer.toString(T.troops) + " troops '+2");
                }
            }
            // If one player conquered all territories, game is over

            if ( done ){
                System.out.println("\nWar is over. Agent "+ territories.get(0).getPlayer().getLocalName() + " conquered all the territories.");
                takeDown();
                return;
            }
            if (rounds>50) {
                int[] playersTerritories = new int[agents.size()];
                for (int i = 0; i<agents.size(); i++){
                    playersTerritories[i] = 0;
                }
                for (game.Territory T : territories){
                    String name = T.getPlayer().getLocalName();
                    int agentIndex = Integer.parseInt(name.substring(1));
                    playersTerritories[agentIndex] += 1;
                }
                int max=0, winner=0;
                for (int i = 0; i < playersTerritories.length; i++){
                    if (playersTerritories[i] > max){
                        winner = i;
                        max = playersTerritories[i];
                    }
                }
                System.out.println("\nWar is over. Agent "+ winner + " conquered more territories than the others.");
                takeDown();
            }

        }

        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }

    class MapListenerBehaviour extends Behaviour {
        public final game.Map map;

        public MapListenerBehaviour(game.Map map) {
            this.map = map;
        }

        public void action() {
            ACLMessage msg = this.map.receive();
            //System.out.println(msg);
            if (msg != null) {
                processMessage(msg);
            }
        }

        public void processMessage(ACLMessage msg) {
            // Process messages
            String[] content = msg.getContent().split(map.delimiterChar);
            AID msgSender = msg.getSender();

            if (content[0].equals("A") || content[0].equals("M")) {
                // Get msg content
                int t1id = Integer.parseInt(content[1]);
                int n = Integer.parseInt(content[2]);
                int t2id = Integer.parseInt(content[3]);

                // Instantiate territories involved in the play
                game.Territory T1, T2;
                if (t1id < this.map.territories.size())
                    T1 = territories.get(t1id); // This line works because of the way territories are created, but the terID is not necessarely the ID on ter list
                else {
                    System.out.println("Invalid. Territory out of map");
                    ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                    msg2.addReceiver(msgSender);
                    msg2.setContent("I"); // I for Invalid
                    send(msg);
                    return;
                }
                if (t2id < this.map.territories.size())
                    T2 = territories.get(t2id); // This line works because of the way territories are created, but the terID is not necessarely the ID on ter list
                else {
                    System.out.println("Invalid. Territory out of map");
                    ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                    msg2.addReceiver(msgSender);
                    msg2.setContent("I"); // I for Invalid
                    send(msg);
                    return;
                }

                // Validate number of troops
                if (n >= T1.getTroops()) {
                    System.out.println(msgSender.getLocalName()+" Invalid. Attacking with more troops than the territory currently have. \nFrom territory "+Integer.toString(T1.getId())+" to territory "+Integer.toString(T2.getId())+" with "+Integer.toString(n));
                    ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                    msg2.addReceiver(msgSender);
                    msg2.setContent(map.INVALID_MOVE);
                    send(msg);
                    return;
                }
                if (content[0].equals("A")) attackResults(T1, T2, n);
                else if (content[0].equals("M")) moveTroops(T1, T2, n);
                return;
            }
            else if (content[0].equals(game.WarAgent.REQUEST_INFO)){
                ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                msg2.addReceiver(msgSender);
                String answer = map.INFORM;
                int[] playersTerritories = new int[agents.size()];
                for (int i = 0; i<agents.size(); i++){
                    playersTerritories[i] = 0;
                }
                for (game.Territory T : territories){
                    String name = T.getPlayer().getLocalName();
                    int agentIndex = Integer.parseInt(name.substring(1));
                    playersTerritories[agentIndex] += 1;
                }
                for (int i = 0; i<playersTerritories.length; i++){
                    answer += map.delimiterChar+Integer.toString(playersTerritories[i]);
                }
                msg2.setContent(answer);
                send(msg2);
            }
        }


        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }
}