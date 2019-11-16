package game;

import jade.core.AID;

import java.util.ArrayList;

public class Territory {
	public static int terCount;
	public int terID;
	public int troops;
	public ArrayList<Territory> frontiers;
	private AID player;

	public Territory() {
		this.terID = terCount;
		terCount++;
		this.troops = 2;
		this.frontiers = new ArrayList<Territory>(0);
		this.player = null;
	}

	public int getTroops() {
		return this.troops;
	}	

	public void setTroops(int n) {
		if (n > 0) this.troops = n;
		else this.troops = 1;
	}

	public void addTroops(int n) {
		this.troops += n;
	}

	public void removeTroops(int n) {
		this.troops -= n;
		if (n <= 0) {
			this.troops = 1;
		}
	}

	public void setPlayer(AID playerAID) {
		this.player = playerAID;
	}
	public AID getPlayer() {
		return this.player;
	}
	public ArrayList<Territory> getFrontiers(){
		return this.frontiers;
	}

	public void addFrontier(Territory T) {
		this.frontiers.add(T);
		T.frontiers.add(this);
	}
	public int getId() {
		return terID;
	}
}
