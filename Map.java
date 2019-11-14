package game;

import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.Random;

public class Map extends Agent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<Territory> territories;
    private ArrayList<AgentController> agents;

    protected void setup() {
        int numberTerritories = 6;
        int numberAgents = 3;
        this.territories = new ArrayList<Territory>(0);
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
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        System.out.println("number of agents created: " + Integer.toString(this.agents.size()));

        // Adding frontiers
        for (Territory T : this.territories) {
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

        System.out.println("map is created; starting agents");
        for (AgentController ac : this.agents) {
            try {
                ac.start();
                System.out.println("ac.start()");
            } catch (StaleProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        addBehaviour(new MapBehaviour(this, 1000));
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

            System.out.println("Map status:");

            for (game.Territory T : this.map.territories) {
                T.troops += 2;
                if (!(T.player == null))
                    System.out.println("Territory " + Integer.toString(T.terID) + " belongs to " + T.player.getName() + " with troops: " + Integer.toString(T.troops) + " troops '+2");
                //System.out.println("Added troops to territory. Territory " + Integer.toString(T.terID) + " troops count is now: " + Integer.toString(T.troops));
            }
        }

        public boolean done() {
            return false;
        }
    }

}