package tcmis.mainpackage;

/**
 * Section 4.1.1, Page 52

 * The AID class provides methods to retrieve the local name (getLocalName()), 
 * the GUID (getName()) and the addresses (getAllAddresses()). We can therefore 
 * enrich the welcome message of our HelloWorldAgent as in this example.
 **/

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class Station extends Agent {
	private int positionX = 0, positionY = 0, derp = 0;
	private boolean showDebugInfo = true;

	// private String rememberAgent;
	private SearchConstraints c = new SearchConstraints();

	protected void setup() {
		c.setMaxResults(new Long(-1));
		this.addBehaviour(new RecieveBehavior(this));

	}

	/**
	 * berekend de afstand van de station naar een car/garage
	 * 
	 * @param x1
	 *            De station's eigen x waarde
	 * @param y1
	 *            De station's eigen y waarde
	 * @param x2
	 *            De car/garage's x waarde
	 * @param y2
	 *            De car/garage's y waarde
	 * @return de werkelijke afstand van station tot car berekend met pythagoras
	 */
	double calculateDistance(int x1, int y1, int x2, int y2) {
		int width, height;
		double result;
		// calculate width
		width = x1 - x2;// 10 - 500 = -490
		height = y1 - y2; // 20 - 100 = -80
		// als de hoogte kleiner is dan 0 wordt de negatieve breedte positief.
		// stelling van pytagoras
		// sqrt((-490*-490) + (-80 * -80)) = sqrt(240100 + 6400) = sqrt (246800)
		// =
		result = Math.sqrt((height * height) + (width * width));

		return result;

	}

	/**
	 * Stopt de reciever aan de ACL waardoor je het bericht makkelijk aan
	 * meerdere mensen kan sturen.
	 * 
	 * @param msg
	 *            de message
	 * @param agent
	 *            de agent naam of agent groep waar je het bericht naar stuurt.
	 */
	private void addRecievers(ACLMessage msg, String agent) {
		try {
			AMSAgentDescription[] agents = AMSService.search(this,
					new AMSAgentDescription(), c);
			for (int i = 0; i < agents.length; i++) {
				if (agents[i].getName().getLocalName().toUpperCase()
						.contains(agent)) {// checkt of de agents bij
											// bijvoorbeeld CAR_ horen. of hij
											// checkt de garage
					msg.addReceiver(agents[i].getName());
					System.out.println(agents[i].getName().getName());
					derp++;
				}
			}
		} catch (FIPAException e) {
			System.out
					.println("HOUSTON WE HAVE A PROBLEM LOOKING FOR FEDERAL AGENTS.");
		}
	}

	/**
	 * gets the total amount of a type of agent.
	 * 
	 * @param agent
	 * @return 0 - infinity
	 */
	private int getTotalRecievers(String agent) {
		int length = 0;
		try {
			AMSAgentDescription[] agents = AMSService.search(this,
					new AMSAgentDescription(), c);
			for (int i = 0; i < agents.length; i++) {
				if (agents[i].getName().getLocalName().toUpperCase()
						.contains(agent)) {
					length++;
				}
			}

		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return length;

	}

	private class RecieveBehavior extends CyclicBehaviour {

		// List<Double> pythagorasList = new ArrayList<Double>();
		Map<Double, AID> carList = new HashMap<Double, AID>();
		int numberOfCars = 0;
		int test = 0;

		public RecieveBehavior(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				System.out.println("DERP" + test++);
				receiveRequest(msg);
			}
			block();
			// TODO Auto-generated method stub

		}

		/**
		 * receiveRequest handles all the requests that the station receives.
		 * 
		 * @param carList
		 */
		private void receiveRequest(ACLMessage msg) {
			// TODO: get requests and
			ACLMessage reply = new ACLMessage(ACLMessage.INFORM);

			switch (requestSelector(msg.getContent())) {
			case 0:
				
				// TODO: determine the car closest to the station.
				addAvailableCars(msg);
				numberOfCars++;
				if (numberOfCars >= getTotalRecievers("CAR_")) {
					sendRejectedAndAccepted(msg);
					carList.clear(); // maakt de lijsten leeg zodat ze weer
										// opnieuw gebruikt kunnen worden.
					numberOfCars = 0;
					break;
				}
				break;
			case 1:
				// reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("LOCATION:");
				addRecievers(msg, "MONITOR_");
				send(reply);
				// send(reply);
				break;
			case 2:
				// request locations of Cars
				reply.setContent("LOCATION");
				addRecievers(reply, "CAR_");
				if (showDebugInfo) {
					System.out.println(myAgent.getName()
							+ " reply on: \"FAILURE\" " + reply.getContent());
					Iterator<AID> it = reply.getAllReceiver();
					for (; it.hasNext();) {
						System.out.println(it.next().getName());
					}
				}
				send(reply);
				break;
			case 3:
				// TODO: send car to his destination
				// reply.setPerformative(ACLMessage.INFORM);
				// reply.setContent(sendCarToDestination(x, y));
				break;
			default:
				System.out.println("Recieved Command not recognized.");
				break;
			}
		}

		/**
		 * In deze methode wordt er gesplitst op ';' karakter. De gesplitse
		 * waardes worden gebruikt om de pythagoras waarde van de car naar het
		 * station te berekenen. deze worden ook in een apparte pythagoras lijst
		 * gezet. deze lijst wordt gebruikt om het meest dichtsbij- zijnde car
		 * te berekenen
		 * 
		 * @param msg
		 *            de acl message die binnen is gekomen.
		 */
		private void addAvailableCars(ACLMessage msg) {
			// Splits de string in de juiste variabelen.
			String params[] = msg.getContent().replace("LOCATION:", "")
					.split(";");
			if (showDebugInfo) {
				for (int i = 0; i < params.length; i++) {
					System.out.append(params[i] + " ");
				}
			}

			if (carList.isEmpty()) {
				carList.put(calculateDistance(positionX, positionY, 0, 0), null);
			}

			if ("AVAILABLE".contentEquals(params[2])) {
				double pythagoras = calculateDistance(positionX, positionY,
						Integer.valueOf(params[0]), Integer.valueOf(params[1]));
				if (!carList.containsValue(msg.getSender())) {
					// carList gebruikt de pythagoras waardes als een sleutel
					// waarmee
					// je de verzender kunt opvragen.
					carList.put(pythagoras, msg.getSender());
				}
			} else {
				System.out.println("Something went wrong");
			}
		}

		/**
		 * berekend de dichsbijzijnste car aan de hand van de pythagorasList.
		 * 
		 * @return keyvalue van de dichtsbijzijnde car
		 */
		private double getClosestCar() {
			Double currentKey = null;
			Iterator<Double> it = carList.keySet().iterator();
			for (; it.hasNext();) {
				double value = it.next();
				if (currentKey == null) {
					currentKey = value;
				} else {
					if (value < currentKey) {
						currentKey = value;
					}
				}
			}
			return currentKey;
		}

		/**
		 * Deze methode stuurt één car naar de coördinaten van het station.
		 * waarna hij naar de andere cars REJECTED
		 * 
		 * @param msg
		 */
		private void sendRejectedAndAccepted(ACLMessage msg) {

			double closestCarKey = getClosestCar(); // pakt de dichtsbeizijnde Car

			Iterator<Double> it = carList.keySet().iterator();
			AID acceptedAID = carList.get(closestCarKey);

			ACLMessage rejectedReply = msg.createReply();
			rejectedReply.setPerformative(ACLMessage.INFORM);
			
			if(acceptedAID != null){
				ACLMessage acceptedReply = msg.createReply();
				acceptedReply.setPerformative(ACLMessage.INFORM);
				acceptedReply.addReceiver(acceptedAID);
				acceptedReply.setContent(sendCarToDestination(positionX, positionY));
				send(acceptedReply);
			}else{
				//TODO: create car
			}
			
			for (; it.hasNext();) {
				double value = it.next();
				if (value != closestCarKey && carList.get(value)!= null) {
					rejectedReply.addReceiver(carList.get(value));
					
				} 
			}
			rejectedReply.setContent("REJECTED");
			send(rejectedReply);
		}

		/**
		 * send car to his locations
		 * 
		 * @param x
		 * @param y
		 * @return GOTO:X;Y
		 */
		private String sendCarToDestination(int x, int y) {
			return "GOTO:" + x + ";" + y;

		}

		/**
		 * Method to determine what request is received.
		 * 
		 * @param str
		 * @return 0 till 3 or a -1 if request is not known.
		 */
		private int requestSelector(String str) {
			String list[] = { "LOCATION:", "LOCATION", "FAILURE",
					"ACCOMPLISHED" };
			if (str.contains(":")) {
				return 0;
			} else {
				for (int i = 1; i < list.length; i++) {
					if (str.startsWith(list[i])) {
						return i;
					}
				}
			}
			return -1;
		}

	}
}
