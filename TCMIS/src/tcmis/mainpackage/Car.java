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
	double currentX = 0, currentY = 0, destinationX, destinationY;
	State trainState = State.AVAILABLE;

	public enum State {
		AVAILABLE, UNAVAILABLE
	}

	
	protected void setup() {
		System.out.println("Hello World. I am an Car!");

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
			
			// Add the TickerBehaviour (period 100 milsec)
		    addBehaviour(new TickerBehaviour(this, 1000) {
		      protected void onTick() {
		        //System.out.println("Agent "+myAgent.getLocalName()+": tick="+getTickCount());
		    	  if(!updateLocation()){
		    		  changeState(State.AVAILABLE);
		    		  stop();
		    	  }
				}
			});

			return true;
		}
		return false;

	}
	
	/**
	 * Return the location
	 * @return int[] (int[0] = x and int[1] = y)
	 */
	private String getLocation(){
		
		//Create location reply
		String loc = "LOCATION:" + currentX + ";" + currentY + ";";
		
		if(trainState.equals(State.AVAILABLE)){
			loc += "AVAILABLE";
		} else{
			loc += "UNAVAILABLE";
		}
		
		return loc;
	}
	
	/**
	 * Let the car move (the shortest path)
	 * @return true if the train has moved
	 */
	private boolean updateLocation(){
		System.out.println("update Location");
		
		double diffx = 0, diffy = 0; 
		
		diffx = currentX - destinationX;
		diffy = currentY - destinationY;
		
		if (diffx != 0 && diffy != 0) {
			
			if (diffx > diffy) {
				//Devide only with positive numbers
				double smalStep = Math.abs(diffx)/Math.abs(diffy);
				
				//Decide what way the car is moving
				if (diffx < 0)
					currentX += smalStep;
				else if (diffx > 0)
					currentX -= smalStep;
				
				if (diffy < 0)
					currentY += 1;
				else if (diffy > 0)
					currentY -= 1;
				
				
			} else {
				//Devide only with positive numbers
				double d = Math.abs(diffy)/Math.abs(diffx);
				
				//Decide what way the car is moving
				if (diffy < 0)
					currentY += d;
				else if (diffy > 0)
					currentY -= d;
				
				if (diffx < 0)
					currentX += 1;
				else if (diffx > 0)
					currentX -= 1;
				
			}
		} else if (diffx == 0) {
			//Decide what way the car is moving
			if (diffy < 0)
				currentY += 1;
			else if (diffy > 0)
				currentY -= 1;
			
		} else if (diffy == 0) {
			//Decide what way the car is moving
			if (diffx < 0)
				currentX += 1;
			else if (diffx > 0)
				currentX -= 1;
			
		}
		System.out.println("Diff x: " + diffx + " - Diff y: " + diffy);
		
		showRaster();
		
		if(currentX == destinationX && currentY == destinationY)
			return false;
		
		return true;
	}

	private void showRaster() {
		String map[][] = new String[40][40];
		
		for (int h = 0; h < map.length; h++) {
			for (int g = 0; g < map.length; g++) {
				map[h][g] = " ";
			}
		}
		map[(int)currentX][(int)currentY] = "c";
		map[(int)destinationX][(int)destinationY] = "d";
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++) {
				System.out.print("|" + map[j][i]);
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
				
				String split[] = msg.getContent().split("[;:]+");
				for (int i = 0; i < split.length; i++) {
					System.out.println("received: " + split[i]);
				}
				
				switch(split[0]){
				case "LOCATION":
					
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(getLocation());
					send(reply);
					
					System.out.println(reply.getContent());
					break;
					
				case "REJECTED":
					//Buhuhuhu :'(
					break;
				case "GOTO":
					
					if ( !goTo(Integer.parseInt(split[1]),
							Integer.parseInt(split[2]))) {
						
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
