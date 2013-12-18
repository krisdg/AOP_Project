package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Car extends Agent {
	int currentX = 0, currentY = 0, destinationX, destinationY;
	State trainState = State.AVAILABLE;

	public enum State {
		AVAILABLE, UNAVAILABLE
	}

	
	protected void setup() {
		System.out.println("Hello World. I am an Train!");

		// The Receiver of train
		addBehaviour(new ReceiveBehaviour(this));

	}

	/**
	 * Set the destination
	 * @param x the X destination
	 * @param y the Y destination
	 * @return Boolean
	 */
	public boolean goTo(int x, int y) {
		if(trainState.equals(State.AVAILABLE)){
			changeState(State.UNAVAILABLE);
			destinationX = x;
			destinationY = y;
			return true;
		}
		return false;
	}
	
	/**
	 * Return the location
	 * @return int[] (int[0] = x and int[1] = y)
	 */
	private int[] getLocation(){
		int[] location = new int[2];
		location[0] = currentX;
		location[1] = currentY;
		return location;
	}
	
	/**
	 * Let the train move
	 * @return true if the train has moved
	 */
	private boolean updateLocation(){
		System.out.println("update Location");
		
		if(currentX > destinationX)
			currentX++;
		else if(currentX < destinationX)
			currentX--;
		
		if(currentY > destinationY)
			currentY++;
		else if(currentY < destinationY)
			currentY--;
		
		showRaster();
		
		return true;
		//TODO do some math
	}

	private void showRaster() {
		String map[][] = new String[20][20];
		
		map[currentX][currentY] = "c";
		map[destinationX][destinationY] = "d";
		
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				System.out.print("|" + map[i][j]);
			}
			System.out.println("|");
		}
		
	}

	/**
	 * Change the state of the train to "Waiting", "Driving" or "Stop"
	 * 
	 * @param state
	 *            State enum
	 */
	public void changeState(State state) {
		trainState = state;
		System.out.println("State changed " + state + " to "
				+ getAID().getLocalName());

	}

	/**
	 * 
	 * @author Michiel
	 *	A class 
	 */
	class ReceiveBehaviour extends CyclicBehaviour {

		public ReceiveBehaviour(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				
				String split[] = msg.getContent().split(";");
				for (int i = 0; i < split.length; i++) {
					System.out.println("received: " + split[i]);
				}
				
				switch(split[0]){
				case "LOCATION":
					//Create location reply
					String loc = currentX + ";" + currentY + ";";
					
					if(trainState.equals(State.AVAILABLE)){
						loc += "AVAILABLE";
					} else{
						loc += "UNAVAILABLE";
					}
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(loc);
					send(reply);
					
					break;
					
				case "REJECTED":
					//Do nothing
					break;
				case "GOTO":
					if (trainState.equals(State.AVAILABLE)) {
						changeState(State.UNAVAILABLE);
						destinationX = 4 ;// Integer.getInteger(split[1]);
						destinationY = 13 ; //Integer.getInteger(split[2]);
						
						// Add the TickerBehaviour (period 100 milsec)
					    myAgent.addBehaviour(new TickerBehaviour(myAgent, 100) {
					      protected void onTick() {
					        //System.out.println("Agent "+myAgent.getLocalName()+": tick="+getTickCount());
					    	  if(!updateLocation()){
					    		  changeState(State.AVAILABLE);
					    		  stop();
					    	  }
					      } 
					    });
						
					} else {
						// Replay an failure
						ACLMessage replyFailure = msg.createReply();
						replyFailure.setPerformative(ACLMessage.INFORM);
						replyFailure.setContent("FAILURE");
						send(replyFailure);
					}
					
					break;
				default:
					
					break;
				}

			}
			block();
		}
	}

	
}
