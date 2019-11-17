package game;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Random;

public class IntelligentWarAgent extends game.WarAgent {
    /**
     *
     */
    protected ArrayList<AID> allies;
    protected float[] parameters;
    protected ArrayList<AID> players;
    protected int[] playersTerritories;

    public void setup() {
        this.agentName = getAID().getName();
        System.out.println("Agent " + this.agentName + " setup");
        this.mapAID = new AID("map0", AID.ISLOCALNAME);
        System.out.println("mapId " + this.mapAID + " setup");

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
        Behaviour attackingBehaviour = new IntelligentWarBehaviour();
        behaviours.add(attackingBehaviour);
        addBehaviour(attackingBehaviour);

        // Listening behaviour
        Behaviour listeningBehaviour = new IntelligentWarListener(this);
        behaviours.add(listeningBehaviour);
        addBehaviour(listeningBehaviour);
        this.parameters = new float[]{0};
        this.allies = new ArrayList<AID>(0);
        System.out.println("Agent " + this.agentName + " setup");
        this.parameters = (float[])args[1];
        addBehaviour(new IntelligentWarBehaviour());

        // System.out.println("setup is done");
    }

    public void addTerritory(game.Territory T) {
        T.setPlayer(this.getAID());
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
            if (territories.size() > 0) this.chooseAttack();
            else takeDown();
            //this.communicate();
        }

        private void chooseAttack(){
            int dif, maxdif;
            game.Territory src = territories.get(0), dest = territories.get(0).getFrontiers().get(0);
            maxdif = src.getTroops() - dest.getTroops();
            for (game.Territory T1 : territories) {
                for (game.Territory T2 : T1.getFrontiers()) {
                    if (T1.getPlayer().getLocalName().equals(T2.getPlayer().getLocalName())) continue;
                    dif = T1.getTroops() - T2.getTroops();
                    if (dif > maxdif) {
                        maxdif = dif;
                        src = T1;
                        dest = T2;
                    }
                }
            }
            if (maxdif - parameters[0] > 0) attackMessage(src, dest, src.getTroops()-1);
        }

    }
    class IntelligentWarListener extends Behaviour {

        private game.WarAgent player;

        public IntelligentWarListener (game.WarAgent player){
            this.player = player;
        }

        public void action(){
            ACLMessage msg = this.player.receive();
            processMessage(msg);
        }
        public float allianceValue(AID P){
            int t = playersTerritories[players.indexOf(P)];
            float v = parameters[1] * t * t + parameters[2] * t + parameters[3];
            return v;
        }

        public void requestInformation(){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(mapAID);
            msg.setContent(game.WarAgent.REQUEST_INFO);
            send(msg);
        }

        public void chooseAlliances() {
            float[] values = new float[players.size()];
            for (AID P : players) {
                int i = players.indexOf(P);
                values[i] = allianceValue(P);
                if (values[i] > 0 && !allies.contains(P)) {
                    proposeAlliance(P);
                }
                else if (values[i] <= 0 && allies.contains(P)) {
                    breakAlliance(P);
                }
            }
        }
        public Boolean decideAlliance(AID P){
            if (allianceValue(P)>0) return true;
            else return false;
        }

        public void breakAlliance(AID P){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(P);
            msg.setContent(game.WarAgent.BREAK_ALLIANCE);
            send(msg);
            allies.remove(P);
        }

        public void proposeAlliance(AID P){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(P);
            msg.setContent(game.WarAgent.PROPOSE_ALLIANCE);
            send(msg);
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
            else {
                String[] content = msg.getContent().split(game.WarAgent.delimiterChar);
                if (content[0].equals(game.WarAgent.PROPOSE_ALLIANCE)) {
                    decideAlliance(msg.getSender());
                }
                else if (content[0].equals(game.WarAgent.ACCEPT_ALLIANCE)){
                    allies.add(msg.getSender());
                }
                else if (content[0].equals(game.WarAgent.REJECT_ALLIANCE)){
                    allies.add(msg.getSender());
                }
                else if (content[0].equals(game.WarAgent.BREAK_ALLIANCE)){
                    allies.remove(msg.getSender());
                }

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

                for (Territory t : territories){
                    if (t.getId() == origID){
                        ter = t;
                        break;
                    }
                }

                if (ter != null){
                    for (Territory t : ter.frontiers){
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
        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }
}
