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


    private static final long serialVersionUID = 1L;
    private ArrayList<game.Territory> territories;
    private ArrayList<AgentController> agents;

    protected void setup() {
        int numberTerritories = 6;
        int numberAgents = 3;
        this.territories = new ArrayList<game.Territory>(0);
        this.agents=new ArrayList<AgentController>(0);

        System.out.println("Map mapAID " + this.getAID());
        ContainerController cc = getContainerController();

        // Creating territories and agents
        for (int j=0; j < numberAgents; j++){
            ArrayList<game.Territory> agentsTerritories = new ArrayList<game.Territory>();
            Object[] args = new Object[1];

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
            try {
                AgentController ac = cc.createNewAgent("A"+Integer.toString(j), "game.WarAgent", args=args);
                this.agents.add(ac);
                ac.start();
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("number of agents created: " + Integer.toString(this.agents.size()));

        // Adding frontiers
        for (game.Territory T : this.territories) {
            Random random = new Random();
            int front = random.nextInt(3) + 2;
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
        //System.out.println("Attack from Territory " + Integer.toString(T1.terID) + " (belongs to) " + T1.player.getName() + " with " + Integer.toString(n) + " troops, to Territory " + Integer.toString(T2.terID) + " with " + Integer.toString(T2.troops) + " troops");

        if (n >= T1.getTroops()) {
            //movimento invalido
            //System.out.println("invalid movement");
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(T1.getPlayer());
            msg.setContent("I"); // F for Failure
            send(msg);
            n = T1.getTroops() - 1;
        }

        if (n < T2.getTroops()) {
            T1.removeTroops(n);
            T2.removeTroops(n);
            // Information after the attack
            //System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(T1.getPlayer());
            msg.setContent("F"); // F for Failure
            send(msg);
            return;
        }

        // Conquer
        else if (n > T2.getTroops()) {
            T1.removeTroops(n);
            T2.setTroops(n - T2.getTroops());

            // Information about the attack
            // System.out.println("Player " + T1.player.getName() + " conquered Territory " + Integer.toString(T2.terID) + " from player " + T2.player.getName() + " and now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");

            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(T1.getPlayer());
            msg.setContent("V"); // V for Victory
            send(msg);
            // Remove territory from T2 owner and add to T1 owner (attacker)
            T2.setPlayer(T1.getPlayer());
            return;
        }
        // Stalemate
        else if (n == T2.getTroops()) {
            T1.removeTroops(n);
            T2.setTroops(1);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(T1.getPlayer());
            msg.setContent("F"); // F for Failure
            send(msg);
            return;
            // Information after the attack
            //System.out.println("Territory " + Integer.toString(T1.terID) + " now has " + Integer.toString(T1.troops) + " troops and Territory " + Integer.toString(T2.terID) + " has " + Integer.toString(T2.troops) + " troops");
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
    }

    public void informAgent(ACLMessage msg, AID destiny){

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
                doDelete();
                return;
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
            while (true) {
                ACLMessage msg = this.map.receive();
                //System.out.println(msg);
                if (msg != null) {
                    processMessage(msg);
                }
                break;
            }
        }

        public void processMessage(ACLMessage msg) {
            // Process messages
            String[] content = msg.getContent().split(map.delimiterChar);
            System.out.println(msg.getContent());
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
                    System.out.println("Invalid. Attacking with more troops than the territory currently have");
                    ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
                    msg2.addReceiver(msgSender);
                    msg2.setContent("I"); // I for Invalid
                    send(msg);
                    return;
                }
                if (content[0].equals("A")) attackResults(T1, T2, n);
                else if (content[0].equals("M")) moveTroops(T1, T2, n);
                return;
            }
        }


        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }
}