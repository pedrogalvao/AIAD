package game;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import java.util.ArrayList;
import java.util.Random;

public class IntelligentWarAgent extends game.WarAgent {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ArrayList<game.Territory> territories;
    private String agentName;
    private float[] parameters;
    public void setup() {
        this.parameters = new float[]{0};
        this.agentName = getAID().getName().substring(0,2);
        System.out.println("Agent " + this.agentName + " setup");

        Object[] args = getArguments();

        this.territories = (ArrayList< game.Territory >) args[0];
        addBehaviour(new IntelligentWarBehaviour());

        // System.out.println("setup is done");
    }

    public void addTerritory(game.Territory T) {
        T.setPlayer(this);
        this.territories.add(T);
    }

    public void removeTerritory(game.Territory T) {
        this.territories.remove(T);
    }

    public ArrayList<game.Territory> getTerritories() {
        return territories;
    }

    public String getAgentName() { return this.agentName; }

    public void takeDown(){
        System.out.println("Agent " + this.getName() + " has died (out of territories");
    }

    class IntelligentWarBehaviour extends WarBehaviour {
        /**
         *
         */
        public static final long serialVersionUID = 1L;

        public void action() {
            this.chooseAttack();
            //this.communicate();
        }

        private void chooseAttack(){
            int dif, maxdif;
            game.Territory src = territories.get(0), dest = territories.get(0).getFrontiers().get(0);
            maxdif = src.getTroops() - dest.getTroops();
            for (game.Territory T1 : territories) {
                for (game.Territory T2 : T1.getFrontiers()) {
                    if (T1.getPlayer().equals(T2.getPlayer())) continue;
                    dif = T1.getTroops() - T2.getTroops();
                    if (dif > maxdif) {
                        maxdif = dif;
                        src = T1;
                        dest = T2;
                    }
                }
            }
            if (maxdif - parameters[0] > 0) attackResults(src, dest, src.getTroops()-1);
        }

    }

}
