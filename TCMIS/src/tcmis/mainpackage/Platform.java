package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import java.util.Random;

import jade.core.Agent;

public class Platform extends Agent {
	int Station;
	LedIndicator[] led = new LedIndicator[10];
	int peopleWaiting = 0;
	int busyIndicator = 1; // Get this from station
	Random r = new Random();

	protected void setup() {
		// Printout a welcome message
		System.out.println("MOEDERMILFJES");
		System.out.println("Hello World. I am an agent!");
		System.out.println("Hello World. I am also an agent! (KRIS)");
		
		populate(); //Call this every minute or smthing?
	}
	
	
	/**
	 * Populate the platform
	 */
	public void populate(){
		peopleWaiting += r.nextInt(busyIndicator * 10);
	}
}
