package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

public class Car extends Agent {
	double currentX = 1, currentY = 1, destinationX = 1, destinationY = 1,
			destinationX2 = 0, destinationY2 = 0;
	State carState = State.AVAILABLE;
	Behaviour gotoBehaviour, goBackBehaviour;
	ACLMessage saveMessageForReply;
	boolean isDriving = false;

	// <Settings>
	boolean showDebugInfo = false;
	int carSpeedInMil = 50; // The update time in milliseconds
	int backToGarageTime = 10000; // The time until the car goes back to the
									// garage
	// </Settings>

	long waitTime = 0;

	public enum State {
		AVAILABLE, UNAVAILABLE
	}

	protected void setup() {
		// Set location: (Give arguments: x;y on creation)
		Object[] args = getArguments();

		if (args.length > 0) {
			String[] positions = args[0].toString().split(";");

			if (positions.length == 2) {
				goTo(Integer.parseInt(positions[0]),
						Integer.parseInt(positions[1]));
				changeState(State.UNAVAILABLE);
			}
			if (positions.length == 4) {
				goTo(Integer.parseInt(positions[0]),
						Integer.parseInt(positions[1]),
						Integer.parseInt(positions[2]),
						Integer.parseInt(positions[3]));
				changeState(State.UNAVAILABLE);
			}
		}

		// Create a new communication behaviour for receiving commands
		addBehaviour(new ReceiveBehaviour(this));

	}

	public boolean goTo(int x, int y, int nextX, int nextY) {
		if (carState.equals(State.UNAVAILABLE))
			return false;

		destinationX2 = nextX;
		destinationY2 = nextY;

		return goTo(x, y);
	}

	public void secondGoTo() {
		if (destinationX2 != 0 || destinationY2 != 0) {
			goTo((int) destinationX2, (int) destinationY2);
			destinationX2 = 0;
			destinationY2 = 0;
			changeState(State.UNAVAILABLE);
		}

	}

	/**
	 * Set the destination
	 * 
	 * @param x
	 *            the X destination
	 * @param y
	 *            the Y destination
	 * @return Boolean
	 */
	public boolean goTo(int x, int y) {
		// This method doesn't work when car is unavailable
		if (carState.equals(State.UNAVAILABLE))
			return false;

		// Update the destination
		destinationX = x;
		destinationY = y;

		// When the gotoBehaviour is still working, just update the destination
		// and return true (in case the car is on its way to the garage).
		 try {
		 if (isDriving) {
			 changeState(State.UNAVAILABLE);
			 return true;
		 }
		 } catch (NullPointerException e) {
		 }

		// Add the TickerBehaviour (period 100 milsec)
		gotoBehaviour = new TickerBehaviour(this, carSpeedInMil) {
			protected void onTick() {
				isDriving = true;

				// true when car is arrived at destination
				if (updateLocation()) {
					isDriving = false;

					changeState(State.AVAILABLE);

					// Checks if car is back to base
					if (currentX == 0 && currentY == 0) {
						if (showDebugInfo)
							System.out
									.println(getName()
											+ " is arrived in the garage, commits suicide.");
						myAgent.doDelete();
					}

					if (System.currentTimeMillis() >= waitTime) {
						secondGoTo();
						stop();
					} else {
						changeState(State.UNAVAILABLE);
					}
				}
			}
		};

		addBehaviour(gotoBehaviour);

		return true;

	}

	/**
	 * Reset the goback behaviour, the goback behaviour is used when the car
	 * isn't used for 120 seconds, and sends the car back to the garage.
	 */
	public void resetGoBackBehaviour() {

		try {
			goBackBehaviour.reset();
			if (showDebugInfo)
				System.out.println(getName() + " goBackBehaviour is resetted");

		} catch (NullPointerException e) {
			if (showDebugInfo)
				System.out
						.println(getName()
								+ " Create a whole new goBackBehaviour (only one time)");

			goBackBehaviour = new WakerBehaviour(this, backToGarageTime) {
				protected void handleElapsedTimeout() {
					if (showDebugInfo)
						System.out.println(myAgent.getName()
								+ " Goes back to base.");

					if (!goTo(0, 0))
						this.reset();
					else
						killGoBackBehaviour();
				}
			};
			addBehaviour(goBackBehaviour);
		}

	}
	
	void killGoBackBehaviour() {
		goBackBehaviour = null;
	}

	/**
	 * Return the location, this method is used to respond to a Station if he
	 * request LOCATION.
	 * 
	 * @return String LOCATION:X;Y;AVAILABLE/UNAVAILABLE
	 */
	private String getLocation() {
		// Create location reply
		String location = "LOCATION:" + (int) currentX + ";" + (int) currentY
				+ ";";

		if (carState.equals(State.AVAILABLE)) {
			location += "AVAILABLE";
		} else {
			location += "UNAVAILABLE";
		}

		return location;
	}

	/**
	 * Let the car move (the shortest path)
	 * 
	 * @return true if arrived at destination
	 */
	private boolean updateLocation() {
		
		double diffx = 0, diffy = 0;

		diffx = currentX - destinationX;
		diffy = currentY - destinationY;

		if (diffx != 0 && diffy != 0) {

			if (Math.abs(diffx) < Math.abs(diffy)) {
				// Devide only with positive numbers
				double smalStep = Math.abs(diffx) / Math.abs(diffy);

				// Decide what way the car is moving
				if (diffx < 0)
					currentX += smalStep;
				else if (diffx > 0)
					currentX -= smalStep;

				if (diffy < 0)
					currentY += 1;
				else if (diffy > 0)
					currentY -= 1;

			} else {
				// Devide only with positive numbers
				double d = Math.abs(diffy) / Math.abs(diffx);

				// Decide what way the car is moving
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
			// Decide what way the car is moving
			if (diffy < 0)
				currentY += 1;
			else if (diffy > 0)
				currentY -= 1;

		} else if (diffy == 0) {
			// Decide what way the car is moving
			if (diffx < 0)
				currentX += 1;
			else if (diffx > 0)
				currentX -= 1;

		}
		
		// Show car in an field (test purposes)
		// showRaster();
		if ((currentX >= (destinationX-5) && currentX <= (destinationX+5)) && (currentY >= (destinationY -5) && currentY <= (destinationY+5))){
			currentX = destinationX;
			currentY = destinationY;
		}
		
		if (currentX == destinationX && currentY == destinationY)
			return true;
		
		waitTime = System.currentTimeMillis() + 5000;
		resetGoBackBehaviour();
		
		return false;
	}

	/**
	 * Show an field in the commandline of 40 by 40 tiles, just for test
	 * purposes.
	 */
	private void showRaster() {
		String map[][] = new String[40][40];

		for (int h = 0; h < map.length; h++) {
			for (int g = 0; g < map.length; g++) {
				map[h][g] = " ";
			}
		}
		map[(int) destinationX][(int) destinationY] = "d";
		map[(int) currentX][(int) currentY] = "c";

		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map.length; j++) {
				System.out.print("|" + map[j][i]);
			}
			System.out.println("|");
		}

	}

	/**
	 * Change the state of the car to AVAILABLE, UNAVAILABLE
	 * 
	 * @param state
	 *            State enum
	 */
	public void changeState(State state) {
		carState = state;
	}

	/**
	 * 
	 * @author Michiel A CyclicBehaviour class that handles all the received
	 *         messages.
	 */
	class ReceiveBehaviour extends CyclicBehaviour {

		public ReceiveBehaviour(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {

				// Split the message so we can use the variables
				String split[] = msg.getContent().split("[;:]+");

				switch (split[0]) {
				case "LOCATION":

					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(getLocation());
					send(reply);
					if (showDebugInfo)
						System.out.println(myAgent.getName()
								+ " reply on \"LOCATION\": "
								+ reply.getContent());
					break;
				case "DESTINATION":
					String des = "";
					if (currentX == destinationX && currentY == destinationY)
						des = "NONE";
					else
						des = (int) destinationX + ";" + (int) destinationY;

					ACLMessage replyDestination = msg.createReply();
					replyDestination.setPerformative(ACLMessage.INFORM);
					replyDestination.setContent("DESTINATION:" + des);
					send(replyDestination);
					if (showDebugInfo)
						System.out.println(myAgent.getName()
								+ " reply on \"DESTINATION\": "
								+ replyDestination.getContent());

					break;

				case "LOCDES":
					ACLMessage replyLocDes = msg.createReply();
					replyLocDes.setPerformative(ACLMessage.INFORM);

					String locDes = "LOCDES:";
					locDes += (int) currentX + ";" + (int) currentY + ";"
							+ (int) destinationX + ";" + (int) destinationY
							+ ":";
					if (carState.equals(State.AVAILABLE))
						locDes += "AVAILABLE";
					else if (carState.equals(State.UNAVAILABLE))
						locDes += "UNAVAILABLE";

					replyLocDes.setContent(locDes);
					send(replyLocDes);
					if (showDebugInfo)
						System.out.println(myAgent.getName()
								+ " reply on \"LOCDES\": "
								+ replyLocDes.getContent());
					break;

				case "REJECTED":
					// Buhuhuhu :'(
					if (showDebugInfo)
						System.out.println(getName() + " is Rejected");

					break;
				case "GOTO":

					if (!goTo(Integer.parseInt(split[1]),
							Integer.parseInt(split[2]),
							Integer.parseInt(split[3]),
							Integer.parseInt(split[4]))) {

						// Replay an failure
						ACLMessage replyFailure = msg.createReply();
						replyFailure.setPerformative(ACLMessage.INFORM);
						replyFailure.setContent("FAILURE");
						send(replyFailure);

						if (showDebugInfo) {
							String state = "";
							if (carState.equals(State.UNAVAILABLE))
								state = "UNAVAILABLE";
							else
								state = "AVAILABLE";

							System.out.println(myAgent.getName()
									+ " reply on \"GOTO\": "
									+ replyFailure.getContent()
									+ " (CAR state is: " + state + ")");
						}
					} else {
						changeState(State.UNAVAILABLE);
						saveMessageForReply = msg;
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
