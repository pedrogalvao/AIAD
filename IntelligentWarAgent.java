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

    public game.Territory searchTerritory(int terId){
        //look for a specific territory in frontiers
        for (game.Territory T : this.territories) {
            if (T.getId() == terId) return T;
            for(game.Territory T2 : T.getFrontiers()) {
                if (T2.getId() == terId) return T2;
            }
        }
        return null;
    }
    public int checkNumberOfTroops(int terId) {
        //search for a territory in frontiers and check the number of troops it contains
        game.Territory T = searchTerritory(terId);
        if (T != null) return T.getTroops();
        //returns -1 if the territory is not in the frontiers
        return -1;
    }

    public void setup() {
        this.agentName = getAID().getName();
        Object[] args = getArguments();
        int numberOfPlayers = (int)args[2];
        this.mapAID = new AID("map"+ Long.toString(game.MapGenerators.mapCount), AID.ISLOCALNAME);

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
        Behaviour attackingBehaviour = new IntelligentWarBehaviour(this);
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
//        addBehaviour(new IntelligentWarBehaviour());

        //System.out.println("Agent " + this.agentName + " setup is done");
    }

    class IntelligentWarBehaviour extends WarBehaviour {
        /**
         *
         */
        public static final long serialVersionUID = 1L;

        int t = 0;
        private game.IntelligentWarAgent player;

        public IntelligentWarBehaviour (game.IntelligentWarAgent player){
            this.player = player;
        }

        public void action() {
            t++;
            if (territories.size() > 0) this.chooseAttack();
            else takeDown();
            if(t%3==0){
                requestInformation();
                chooseAlliances();
                proposeDeal();
            }
            block(WarBehaviour.delay);
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

        public Boolean allianceValue(AID P){
            int index = Integer.parseInt(P.getLocalName().substring(1)) % game.MapGenerators.numberAgents;
            float t1 = (float)playersTerritories[index]/(float)this.player.numberOfTerritories;
            float t2 = (float)this.player.territories.size()/(float)this.player.numberOfTerritories;
            float v1 = parameters[1] * t1 * t1 + parameters[2] * t1 + parameters[3];
            float v2 = parameters[4] * t2 * t2 + parameters[5] * t2 + parameters[6];
            if (v1 > t2 && v2 > t1) return true;
            else return false;
        }

        private void chooseAttack(){
            // Checks if agent still has territories, if not takeDown
            if (territories.size() == 0){
                takeDown();
            }

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
        String firstDealMessage(AID ally, int allyDestTer, int allyTroops) {
            String content = game.WarAgent.DEAL + game.WarAgent.delimiterChar + Integer.toString(allyDestTer) + game.WarAgent.delimiterChar + Integer.toString(allyTroops);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(ally);
            msg.setContent(content);
            send(msg);
            //System.out.println(msg.getContent());
            return content;
        }
        private void proposeDeal(){

            if (territories.size() == 0){
                takeDown();
            }

            int dif, maxdif;
            game.Territory src = territories.get(0), dest = territories.get(0).getFrontiers().get(0);
            maxdif = src.getTroops() - dest.getTroops();
            for (game.Territory T1 : territories) {
                for (game.Territory T2 : T1.getFrontiers()) {
                    if (T1.getPlayer().getLocalName().equals(T2.getPlayer().getLocalName())) continue;
                    dif = T2.getTroops() - T1.getTroops();
                    if (dif > maxdif) {
                        maxdif = dif;
                        src = T1;
                        dest = T2;
                    }
                }
            }
            if (maxdif < 0) return;

            int dif2=-1, maxdif2=-1;
            game.Territory src2 = dest.getFrontiers().get(0);
            for (game.Territory T2 : dest.getFrontiers()) {
                if (dest.getPlayer().getLocalName().equals(T2.getPlayer().getLocalName())) continue;
                if (dif2 > maxdif2) {
                    maxdif2 = dif2;
                    src2 = T2;
                }
            }
            int numTroops = Math.min(src2.getTroops()-1,dest.getTroops()-1);
            firstDealMessage(src2.getPlayer(), dest.getId(), numTroops);
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
            //System.out.println("Agent "+ player.getLocalName() +" allies: "+aa);*/
            ACLMessage msg = this.player.receive();
            processMessage(msg);
        }
        public Boolean allianceValue(AID P){
            int index = Integer.parseInt(P.getLocalName().substring(1)) % game.MapGenerators.numberAgents;
            float t1 = (float)playersTerritories[index]/(float)this.player.numberOfTerritories;
            float t2 = (float)this.player.territories.size()/(float)this.player.numberOfTerritories;
            float v1 = parameters[1] * t1 * t1 + parameters[2] * t1 + parameters[3];
            float v2 = parameters[4] * t2 * t2 + parameters[5] * t2 + parameters[6];
            if (v1 > t2 && v2 > t1) return true;
            else return false;
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

        public double dealUtility(int myTroops, int enemyTroops, int allyTroops, int costTroops){

            double utility = 0;
            //if (myTroops < enemyTroops && allyTroops > enemyTroops - myTroops) utility += parameters[7];
            if (myTroops < enemyTroops && allyTroops > enemyTroops - myTroops) utility += 4;
            utility += allyTroops;
            utility -= costTroops;
            //utility += parameters[8] * (myTroops - (enemyTroops - allyTroops)) * Math.pow((double)Math.abs(myTroops - (enemyTroops - allyTroops)) , 1.2) / myTroops;
            utility += 0.2 * (myTroops - (enemyTroops - allyTroops)) * Math.pow((double)Math.abs(myTroops - (enemyTroops - allyTroops)) , 1.2) / myTroops;
            return utility;

        }

        void dealMessage(AID ally, int allyDestTer, int allyTroops, int myDestTer, int myTroops) {
            String content = game.WarAgent.DEAL + game.WarAgent.delimiterChar + Integer.toString(allyDestTer) + game.WarAgent.delimiterChar + Integer.toString(allyTroops) + game.WarAgent.delimiterChar + Integer.toString(myDestTer) + game.WarAgent.delimiterChar + Integer.toString(myTroops);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(ally);
            msg.setContent(content);
            send(msg);
            //System.out.println(msg.getContent());
            return;
        }
        void dealReplyMessage(AID ally, int allyDestTer, int allyTroops, int myDestTer, int myTroops) {
            String content = game.WarAgent.REPLY_DEAL + game.WarAgent.delimiterChar + Integer.toString(allyDestTer) + game.WarAgent.delimiterChar + Integer.toString(allyTroops) + game.WarAgent.delimiterChar + Integer.toString(myDestTer) + game.WarAgent.delimiterChar + Integer.toString(myTroops);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(ally);
            msg.setContent(content);
            send(msg);
            //System.out.println(msg.getContent());
            return;
        }
        void acceptDealMessage(AID ally, int allyDestTer, int allyTroops, int myDestTer, int myTroops) {
            String content = game.WarAgent.ACCEPT_DEAL + game.WarAgent.delimiterChar + Integer.toString(allyDestTer) + game.WarAgent.delimiterChar + Integer.toString(allyTroops) + game.WarAgent.delimiterChar + Integer.toString(myDestTer) + game.WarAgent.delimiterChar + Integer.toString(myTroops);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(ally);
            msg.setContent(content);
            send(msg);
            //System.out.println(msg.getContent());
            return;
        }

        public void respondDeal(ACLMessage msg){
            String[] content = msg.getContent().split(game.WarAgent.delimiterChar);

            if (content.length == 3) {
                if (territories.size() == 0) {
                    takeDown();
                }

                int dif, maxdif;
                game.Territory myTer = territories.get(0), enemyTer = territories.get(0).getFrontiers().get(0);
                maxdif = myTer.getTroops() - enemyTer.getTroops();
                for (game.Territory T1 : territories) {
                    for (game.Territory T2 : T1.getFrontiers()) {
                        if (T1.getPlayer().getLocalName().equals(T2.getPlayer().getLocalName())) continue;
                        dif = T2.getTroops() - T1.getTroops();
                        if (dif > maxdif) {
                            maxdif = dif;
                            myTer = T1;
                            enemyTer = T2;
                        }
                    }
                }
                if (maxdif < 0) return;

                double util = -10, maxutil = -10;
                game.Territory allyTer = enemyTer.getFrontiers().get(0);
                for (game.Territory allyTer2 : enemyTer.getFrontiers()) {
                    if (!allyTer2.getPlayer().getLocalName().equals(msg.getSender().getLocalName())) continue;

                    int allyTroops = Math.min(allyTer2.getTroops() - 1, enemyTer.getTroops() - 1);

                    if (allyTroops > 0)
                        util = dealUtility(myTer.getTroops(), enemyTer.getTroops(), allyTroops, Integer.parseInt(content[2]));
                    else util = -10;

                    if (util > maxutil) {
                        maxutil = util;
                        allyTer = allyTer2;
                    }
                }

                if (maxutil <= 0) {
                    //System.out.println("Rejecting deal");
                    return;
                    /*
                    ACLMessage msgReply = new ACLMessage(ACLMessage.INFORM);
                    msgReply.addReceiver(msg.getSender());
                    msgReply.setContent(game.WarAgent.REJECT_DEAL);
                    send(msgReply);*/
                } else {
                    int allyTroops = Math.min(enemyTer.getTroops() -1, allyTer.getTroops() - 1);
                    dealMessage(msg.getSender(), enemyTer.getId(), allyTroops, Integer.parseInt(content[1]), Integer.parseInt(content[2]));
                }
            }
            else if (content.length == 5) {
                respondDealReply(msg);
                return;
            }
            else {
                //System.out.println("\nUnexpected msg length: "+ Integer.toString(content.length));
                //System.out.println(msg.getContent());
            }
        }

        private boolean respondDealReply(ACLMessage msg){
            String[] content = msg.getContent().split(game.WarAgent.delimiterChar);
            game.Territory enemyTer = searchTerritory(Integer.parseInt(content[3]));
            if (enemyTer == null){
                //System.out.println("ERROR: Couldn't find enemy territory: "+Integer.parseInt(content[3]));
                return false;
            }
            game.Territory myTer = territories.get(0);
            int maxdif = -10;
            for (game.Territory T : enemyTer.getFrontiers()){
                int dif = enemyTer.getTroops() - T.getTroops();
                if (dif > maxdif) {
                    maxdif = dif;
                    myTer = T;
                }
            }
            int myTroops = myTer.getTroops();
            int enemyTroops = checkNumberOfTroops(Integer.parseInt(content[1]));
            if (enemyTroops == -1) return false;
            int allyTroops = Integer.parseInt(content[4]);
            int costTroops = Integer.parseInt(content[2]);
            double util = dealUtility(myTroops, enemyTroops, allyTroops, costTroops);
            if (util > 0){

                acceptDealMessage(msg.getSender(), Integer.parseInt(content[3]), allyTroops, Integer.parseInt(content[1]), costTroops);

                //attack enemy territory
                game.Territory src = enemyTer.getFrontiers().get(0);
                int dif;
                maxdif = -999999;
                for (game.Territory T : enemyTer.getFrontiers()){
                    if (!T.getPlayer().getLocalName().equals(this.player.getLocalName())) continue;
                    dif = T.getTroops() - enemyTer.getTroops();
                    if (dif > maxdif) {
                        maxdif = dif;
                        src = T;
                    }
                }
                attackMessage(src, enemyTer, costTroops);

                return true;
            }
            else if ( dealUtility(myTroops, enemyTroops, allyTroops, costTroops - (int)util - 2) >0 ) {
                //reply offering less troops

                //System.out.println("___________________DEAL REPLY REPLY_________________________");
                int allyDestTer = Integer.parseInt(content[3]);
                dealReplyMessage(msg.getSender(), allyDestTer, allyTroops, enemyTer.getId(),costTroops - (int)util - 2);
            }
            return false;
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
                else if (content[0].equals(game.WarAgent.DEAL)){
                    respondDeal(msg);
                }
                else if (content[0].equals(game.WarAgent.REPLY_DEAL)){
                    respondDealReply(msg);
                }
                else if (content[0].equals(game.WarAgent.ACCEPT_DEAL)){
                    game.Territory enemyTer = searchTerritory(Integer.parseInt(content[1]));
                    if (enemyTer == null){
                        //System.out.println("ERROR: Couldn't find enemy territory: "+Integer.parseInt(content[3]));
                        return;
                    }
                    game.Territory src = enemyTer.getFrontiers().get(0);
                    int dif;
                    int maxdif = -999999;
                    for (game.Territory T : enemyTer.getFrontiers()){
                        if (!T.getPlayer().getLocalName().equals(this.player.getLocalName())) continue;
                        dif = T.getTroops() - enemyTer.getTroops();
                        if (dif > maxdif) {
                            maxdif = dif;
                            src = T;
                        }
                    }
                    game.Territory dest = searchTerritory(Integer.parseInt(content[1]));
                    attackMessage(src, dest, Integer.parseInt(content[2]));
                    //System.out.println("_________________________SUCCESSFULLY CONCLUDED DEAL___________________________");
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

                //System.out.println("Conquered territory");

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
                        //System.out.println("Conquering ERR: Couldn't find conquered territory in frontiers from origin territory");
                    }

                } else{
                    //System.out.println("Conquering ERR: Orign of conquering attack not found.");
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
