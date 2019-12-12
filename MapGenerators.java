package game;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class MapGenerators extends Agent {
    public static final long maxMaps = 30;
    public static long mapCount = 0;

    /*
    public static final int numberTerritories = 18;
    public static final int numberAgents = 6;
    public static final int numAgentsColab = 2;
    public static final int numSmartAgents = 3;
    public static final int numRandomAgents = numberAgents - numSmartAgents;
     */

    protected void setup(){
        // Generate first map; Wait for first map to signalize game over to generate next maps
        generateMap();
        addBehaviour(new GameOverListener(this));
    }

    protected void generateMap(){
        game.Territory.terCount = 0;
        ContainerController cc = getContainerController();
        AgentController ac = null;
        try {
            ac = cc.createNewAgent("map" + Long.toString(mapCount), "game.Map", null);
            System.out.println("\nMap "+Long.toString(mapCount));
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
        try {
            ac.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    class GameOverListener extends Behaviour{
        private MapGenerators gen;

        public GameOverListener(MapGenerators gen){
            this.gen = gen;
        }

        public void action(){
            ACLMessage msg = this.gen.receive();

            if (msg != null){
                String content = msg.getContent();
                String cmp = new String("N");

                if (content.equals(cmp)){ // If asked to generate next map
                    System.out.println(msg.getContent());
                    // Check if number of maps is over
                    if (MapGenerators.mapCount < MapGenerators.maxMaps) {
                        System.out.println(Long.toString(MapGenerators.mapCount));
                        MapGenerators.mapCount++;
                        System.out.println(Long.toString(MapGenerators.mapCount));
                        generateMap();
                    }
                    else {
                        gen.doDelete();
                        System.out.println("Game over");
                    }
                }
            }
        }

        @Override
        public boolean done(){
            return false;
        }
    }
}
