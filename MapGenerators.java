package game;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class MapGenerators extends Agent {
    public static final long maxMaps = 500;
    public static long mapCount = 0;
    public static final long maxRounds = 150;

    public static final int numberAgents = 6;
    public static final int numAgentsColab = 3;
    /*
    public static final int numberTerritories = 18;


    public static final int numSmartAgents = 3;
    public static final int numRandomAgents = numberAgents - numSmartAgents;
     */

    // TODO: Variar número de:
    /*
    Parâmetros dos agentes inteligentes

    Num Territórios iniciais
    Número de agentes que colaboram
    Número de tropas que o mapa adiciona a cada rodada (
        nesse seria interessante mudar tb se ele adiciona somando ou multiplicando tb
        Multiplicando podia ser um valor entre 1 e 1.5, por ex, pra não explodir muito rápido
        Acho que multiplicando tende a favorecer agentes mais conservadores, pq se ele acumula mais tropas no território, ele vai ganhar mais tropas

    Agentes?
    Número de agentes inteligentes

     */

    protected void setup(){
        // Generate first map; Wait for first map to signalize game over to generate next maps
        generateMap();
        addBehaviour(new GameOverListener(this));
    }

    protected void generateMap(){
        // Generate parameters for smart agents

        game.Territory.terCount = 0;
        Object[] args = new Object[1];
        args[0] = generateSmartParam();

        ContainerController cc = getContainerController();
        AgentController ac = null;
        try {
            ac = cc.createNewAgent("map" + Long.toString(mapCount), "game.Map", args=args);
            ac.start();
            //System.out.println("\nMap "+Long.toString(mapCount));
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    private float[][] generateSmartParam() {
        float[][] param = new float[numberAgents][7];

        for (int numAgentsCreated = 0; numAgentsCreated < numberAgents; numAgentsCreated++){
            if (numAgentsCreated < numAgentsColab)
                param[numAgentsCreated] = new float[]{-1,0,0,0,0,0,0};
            else
                param[numAgentsCreated] = new float[]{-1,0,0,1,0,0,1};
        }

        return param;
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
                    //System.out.println(msg.getContent());
                    // Check if number of maps is over
                    if (MapGenerators.mapCount < MapGenerators.maxMaps) {
                        //System.out.println(Long.toString(MapGenerators.mapCount));
                        MapGenerators.mapCount++;
                        //System.out.println(Long.toString(MapGenerators.mapCount));
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
