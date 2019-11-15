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
    public static final long freezeTime = 1000;
    private static final long serialVersionUID = 1L;
    private ArrayList<game.Territory> territories;
    private ArrayList<AgentController> agents;

    protected void setup() {
        int numberTerritories = 6;
        int numberAgents = 3;
        this.territories = new ArrayList<game.Territory>(0);
        this.agents=new ArrayList<AgentController>(0);

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
                System.out.println("Player " + T2.getPlayerName() + " has the following territories:");
                for (game.Territory ter : T2.player.getTerritories()) {
                    System.out.println(ter.terID);
                }
                T2.player.takeDown();
            }

            // Remove territory from T2 owner and add to T1 owner (attacker)
            T2.setPlayer(T1.getPlayer());
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

            // If one player conquered all territories, game is over
            if (this.map.territories.get(0).getPlayerName() != null && this.map.territories.size() == this.map.territories.get(0).player.getTerritories().size() ){
                System.out.println("\nWar is over. Agent "+ territories.get(0).getPlayer().getName() + " conquered all the territories.");
                doDelete();
                return;
            }

            // If not, add troops and inform game status
            System.out.println("Map status:");

            for (game.Territory T : this.map.territories) {
                T.troops += 2;
                if (!(T.player == null)) {
                    System.out.println("Territory " + Integer.toString(T.terID) + " belongs to " + T.player.getName() + " with troops: " + Integer.toString(T.troops) + " troops '+2");
                }
            }
        }

        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }

    class MapListenerBehaviour extends Behaviour {
        public final game.Map map;

        public MapListenerBehaviour(game.Map map){
            this.map = map;
        }

        public void action() {
            while (true) {
                ACLMessage msg = this.map.receive();
                System.out.println(msg);
                if (msg != null) {
                    String[] content = msg.getContent().split(" ");
                    System.out.println(msg.getContent());
                    if (content[0].equals("Attack")) {
                        game.Territory T1 = territories.get(Integer.parseInt(content[1]));
                        game.Territory T2 = territories.get(Integer.parseInt(content[2]));
                        attackResults(T1, T2, Integer.parseInt(content[3]));
                    }
                    break;
                }
            }
        }

        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }
}