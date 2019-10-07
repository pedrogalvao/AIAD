import java.util.ArrayList;
import WarAgent;

public class Territory {
	WarAgent player;
	private int troops;
	private ArrayList<Territory> frontiers;
	
	Territory() {
		this.troops = 2;
		this.frontiers = new ArrayList<Territory>(0);
	}
	
	public int getTroops() {
		return this.troops;
	}	
	
	public void setTroops(int n) {
		this.troops = n;
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
		this.player.removeTerritory(this);
		P.addTerritory(this);
	}
	
	public ArrayList<Territory> getFrontiers(){
		return this.frontiers;
	}
	
	public void addFrontier(Territory T) {
		this.frontiers.add(T);
		T.frontiers.add(this);
	}
	
}
