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
    protected int numberOfTerritories;

    public void setup() {
        this.agentName = getAID().getName();
        Object[] args = getArguments();
        int numberOfPlayers = (int)args[2];
        this.mapAID = new AID("map0", AID.ISLOCALNAME);

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
        players = new ArrayList<AID>(0);
        for (int i = 0;  i < numberOfPlayers; i++){
            players.add(new AID("A"+Integer.toString(i), AID.ISLOCALNAME));
        }
        this.playersTerritories = new int[numberOfPlayers];
        this.numberOfTerritories = numberOfPlayers*this.territories.size();
        this.allies = new ArrayList<AID>(0);
        this.parameters = (float[])args[1];
        addBehaviour(new IntelligentWarBehaviour());

        System.out.println("Agent " + this.agentName + " setup is done");
    }

    class IntelligentWarBehaviour extends WarBehaviour {
        /**
         *
         */
        public static final long serialVersionUID = 1L;

        public void action() {
            if (territories.size() > 0) this.chooseAttack();
            else takeDown();

            block(WarBehaviour.delay);
        }

        private void chooseAttack(){
            int dif, maxdif;
            game.Territory src = territories.get(0), dest = territories.get(0).getFrontiers().get(0);
            maxdif = src.getTroops() - dest.getTroops();
            for (game.Territory T1 : territories) {
                for (game.Territory T2 : T1.getFrontiers()) {
                    if (T1.getPlayer().getLocalName().equals(T2.getPlayer().getLocalName())) continue;
                    Boolean notInAllies = true;
                    for (AID A : allies){
                        if (A.getLocalName().equals(T2.getPlayer().getLocalName())) {
                            notInAllies = false;
                            break;
                        }
                    }
                    if (notInAllies){
                        dif = T1.getTroops() - T2.getTroops();
                        if (dif > maxdif) {
                            maxdif = dif;
                            src = T1;
                            dest = T2;
                        }
                    }
                }
            }
            if (maxdif - parameters[0] > 0) attackMessage(src, dest, src.getTroops()-1);

        }

    }
    class IntelligentWarListener extends Behaviour {

        private game.IntelligentWarAgent player;

        public IntelligentWarListener (game.IntelligentWarAgent player){
            this.player = player;
        }

        public void action(){

            /*String aa = "";
            for (AID A:player.allies){
                aa+=A.getLocalName()+", ";
            }
            System.out.println("Agent "+ player.getLocalName() +" allies: "+aa);*/
            requestInformation();
            ACLMessage msg = this.player.receive();
            processMessage(msg);
            chooseAlliances();
        }
        public Boolean allianceValue(AID P){
            int index = Integer.parseInt(P.getLocalName().substring(1));
            float t1 = (float)playersTerritories[index]/(float)this.player.numberOfTerritories;
            float t2 = (float)this.player.territories.size()/(float)this.player.numberOfTerritories;
            float v1 = parameters[1] * t1 * t1 + parameters[2] * t1 + parameters[3];
            float v2 = parameters[4] * t2 * t2 + parameters[5] * t2 + parameters[6];
            if (v1 > t2 && v2 > t1) return true;
            else return false;
        }

        public void requestInformation(){
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(mapAID);
            msg.setContent(game.WarAgent.REQUEST_INFO);
            send(msg);
        }

        public void chooseAlliances() {
            for (AID P : players) {
                if (P.getLocalName().equals(this.player.getLocalName())) continue;
                int i = players.indexOf(P);
                Boolean value = allianceValue(P);

                Boolean inAllies = false;
                for (AID A : allies){
                    if (A.getLocalName().equals(P.getLocalName())) {
                        inAllies = true;
                        break;
                    }
                }

                if (value && !inAllies) {
                    proposeAlliance(P);
                }
                else if (value && inAllies) {
                    breakAlliance(P);
                }
            }
        }
        public void decideAlliance(AID P){
            if (P.getLocalName().equals(this.player.getLocalName())) return;
            else if (allianceValue(P)) {
                for (AID A : allies){
                    if (A.getLocalName().equals(P.getLocalName())) {
                        return;
                    }
                }
                this.player.allies.add(P);
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(P);
                msg.setContent(game.WarAgent.ACCEPT_ALLIANCE);
                send(msg);
                return;
            }
            else {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(P);
                msg.setContent(game.WarAgent.REJECT_ALLIANCE);
                send(msg);
                return;
            }
        }

        public void breakAlliance(AID P){
            allies.remove(P);
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

            // Get sender and send msg to the right "inbox"
            AID msgSender = msg.getSender();

            if (msgSender.equals(mapAID)){
                mapMessage(msg);
                return;
            }
            else {
                String[] content = msg.getContent().split(game.WarAgent.delimiterChar);
                if (content[0].equals(game.WarAgent.PROPOSE_ALLIANCE)) {
                    decideAlliance(msgSender);
                }
                else if (content[0].equals(game.WarAgent.ACCEPT_ALLIANCE)){
                    for (AID A : allies){
                        if (A.getLocalName().equals(msgSender.getLocalName())) {
                            return;
                        }
                    }
                    allies.add(msgSender);
                }
                else if (content[0].equals(game.WarAgent.REJECT_ALLIANCE)){
                    allies.remove(msgSender);
                }
                else if (content[0].equals(game.WarAgent.BREAK_ALLIANCE)){
                    allies.remove(msgSender);
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
            if (content[0].equals(game.Map.INVALID_MOVE))
                return;

            // If received message for game over, takedown
            else if (content[0].equals("O")){
                takeDown();
                return;
            }

            else if (content[0].equals("L")){// Lost territory
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
                }
                return;
            }
            else if (content[0].equals(game.Map.INFORM)){
                for (int i = 1; i < playersTerritories.length + 1; i++){
                    playersTerritories[i-1] = Integer.parseInt(content[i]);
                }
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
        // @Override some JADE method. Necessary
        public boolean done(){
            return false;
        }
    }
}
