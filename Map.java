import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import java.util.Random;

public class Map extends Agent {
	
	ArrayList<Territory> territories;

	Map(int n){
		this.territories = new ArrayList<Territory>(0);

		for (int i = 0; i < n; i++) {
			this.territories.add(new Territory());
		}
		for (Territory T : this.territories) {
			Random random = new Random();
			int front = random.nextInt(3) + 2;			 
			for (int i = 0; i < front; i++) {
				Random random2 = new Random();
				int k = random2.nextInt(this.territories.size());	
				T.addFrontier(this.territories.get(k));
			}
		}
		
		
	}

}
