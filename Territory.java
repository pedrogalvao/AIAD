package game;

import java.util.ArrayList;

public class Territory {
	public static int terCount;
	public int terID;
	public WarAgent player;
	public String playerName;
	public int troops;
	public ArrayList<Territory> frontiers;

	public Territory() {
		this.terID = terCount;
		terCount++;
		this.troops = 2;
		this.frontiers = new ArrayList<Territory>(0);
		this.player = null;
	}

	public Territory(WarAgent player) {
		this.terID = terCount;
		terCount++;
		this.troops = 2;
		this.frontiers = new ArrayList<Territory>(0);
		this.player = player;
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

	public WarAgent getPlayer() {
		return this.player;
	}

	public void setPlayer(WarAgent P) {
		//if (this.player != null) this.player.removeTerritory(this);
		this.player = P;
	}

	public ArrayList<Territory> getFrontiers(){
		return this.frontiers;
	}

	public void addFrontier(Territory T) {
		this.frontiers.add(T);
		T.frontiers.add(this);
	}
}
