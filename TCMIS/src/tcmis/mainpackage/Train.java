package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import java.util.ArrayList;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Train extends Agent {
	int currentX, currentY, destinationX, destinationY;
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
			currentX = x;
			currentY = y;
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
	
	private void updateLocation(){
		//TODO do some math
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
				
				switch(msg.getContent()){
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
					
				default:
					
					break;
				}
				
				System.out.println(" - " + myAgent.getLocalName()
						+ " received: " + msg.getContent());

				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent(" Polo");
				send(reply);
			}
			block();
		}
	}

	
}
