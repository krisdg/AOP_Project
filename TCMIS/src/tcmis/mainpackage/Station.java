package tcmis.mainpackage;

/**
 * Section 4.1.1, Page 52

 * The AID class provides methods to retrieve the local name (getLocalName()), 
 * the GUID (getName()) and the addresses (getAllAddresses()). We can therefore 
 * enrich the welcome message of our HelloWorldAgent as in this example.
 **/

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class Station extends Agent {
	private int positionX = 0, positionY = 0;
	private int destinationX = 0, destinationY = 0;
	private boolean showDebugInfo = true;
	// private List<AID> memento = new ArrayList<AID>();

	// private String rememberAgent;
	private SearchConstraints c = new SearchConstraints();

	protected void setup() {
		c.setMaxResults(new Long(-1));
		this.addBehaviour(new RecieveBehavior(this));

		// Set location: (Give arguments: x;y on creation)
		Object[] args = getArguments();
		positionX = Integer.parseInt(args[0].toString().split(";")[0]);
		positionY = Integer.parseInt(args[0].toString().split(";")[1]);
		
		

//		createCar();
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
					// System.out.println(agents[i].getName().getName());
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
		int numberOfCars = 1;
		int test = 0;
		boolean available = false;

		public RecieveBehavior(Agent a) {
			super(a);
		}

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
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
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.INFORM);

			String content = "" + msg.getContent();

			System.out.println("received content: " + this.getAgent().getName()
					+ " : " + content);

			switch (requestSelector(content)) {
			case 0:
				// LOCATION:
				if (msg.getSender().getName().contains("STATION_")) {
					System.out.println(msg.getSender().getName());
					String str[] = msg.getContent().replace("LOCATION:", "")
							.split(";");
					destinationX = Integer.parseInt(str[0]);
					destinationY = Integer.parseInt(str[1]);
					System.out.println("Destination" + destinationX + " "
							+ destinationY);
					if(getTotalRecievers("CAR_")==0){
						createCar();
					}
					
				} else {

					addAvailableCars(msg);
					numberOfCars++;
					if (numberOfCars >= getTotalRecievers("CAR_")) {
						available = true;
						sendRejectedAndAccepted();
						carList.clear(); // maakt de lijsten leeg zodat ze weer
											// opnieuw gebruikt kunnen worden.
						numberOfCars = 1;
						break;
					}
				}
				break;
			case 1:
				// LOCATION
				// send location to the monitor agent
				reply.setContent("LOCATION:" + positionX + ";" + positionY);
				send(reply);
				break;
			case 2:
				// FAILURE

				// request locations of Cars
				reply.setContent("LOCATION");
				addRecievers(reply, "CAR_");
				send(reply);

				break;
			case 3:
				// ACCOMPLISHED

				// TODO: send car to his destination
				// reply.setPerformative(ACLMessage.INFORM);
				// reply.setContent(sendCarToDestination(x, y));

				break;
			case 4:
				// ADDREQUEST

				// request locations of Cars
				String[] request = msg.getContent().split("[;:]+");

				String station = request[1];
			    ACLMessage sendStation = new ACLMessage(ACLMessage.INFORM);
			    sendStation.setContent("LOCATION");
			    
			    addRecievers(sendStation, station);
			    send(sendStation);

				reply.setContent("LOCATION");
				addRecievers(reply, "CAR_");
				send(reply);

				break;
			default:
				System.out.println("Recieved Command not recognized.");
				System.out.println(content);
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

			if ("AVAILABLE".contentEquals(params[2])) {// Check of beschikbaar
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
		 */
		private void sendRejectedAndAccepted() {

			double closestCarKey = getClosestCar(); // pakt de dichtsbeizijnde
													// Car

			Iterator<Double> it = carList.keySet().iterator();
			AID acceptedAID = carList.get(closestCarKey);

			ACLMessage rejectedReply = new ACLMessage(ACLMessage.INFORM);
			rejectedReply.setPerformative(ACLMessage.INFORM);

			if (acceptedAID != null) {
				ACLMessage acceptedReply = new ACLMessage(ACLMessage.INFORM);
				acceptedReply.setPerformative(ACLMessage.INFORM);
				acceptedReply.addReceiver(acceptedAID);
				acceptedReply.setContent(sendGoto());
				send(acceptedReply);
			} else {
				createCar();

//				ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
//				reply.setContent("LOCATION");
//				addRecievers(reply, "CAR_");
//				send(reply);
			}

			for (; it.hasNext();) {
				double value = it.next();
				if (value != closestCarKey && carList.get(value) != null) {
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
		private String sendGoto() {
			return "GOTO:" + positionX + ";" + positionY + ";" + destinationX
					+ ";" + destinationY;

		}

		/**
		 * Method to determine what request is received.
		 * 
		 * @param str
		 * @return 0 tot 4 of een -1 als de request niet bekend is.
		 */
		private int requestSelector(String str) {
			try {
				String list[] = { "LOCATION:", "LOCATION", "FAILURE",
						"ACCOMPLISHED", "ADDREQUEST" };
				for (int i = 0; i < list.length; i++) {
					if (str.startsWith(list[i])) {
						return i;
					}
				}
				return -1;
			} catch (NullPointerException e) {
				e.printStackTrace();
				return -1;
			}
		}
	}

	/**
	 * create a car
	 */
	private void createCar() {
		CreateAgent ca = new CreateAgent();

		ca.setAgentName("CAR_" + (getTotalRecievers("CAR") + 1));
		ca.addArguments(positionX+";"+positionY+";"+destinationX+";"+destinationY);
		ca.setClassName(Car.class.getName());
		ca.setContainer(new ContainerID("Main-Container", null));
		Action actExpr = new Action(getAMS(), ca);
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(getAMS());
		request.setOntology(JADEManagementOntology.getInstance().getName());

		getContentManager().registerLanguage(new SLCodec(),
				FIPANames.ContentLanguage.FIPA_SL);

		getContentManager().registerOntology(
				JADEManagementOntology.getInstance());

		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		try {
			getContentManager().fillContent(request, actExpr);
			addBehaviour(new AchieveREInitiator(this, request) {
				protected void handleInform(ACLMessage inform) {
					System.out.println("Car Agent successfully created");
				}

				protected void handleFailure(ACLMessage failure) {
					System.out.println("Error creating Car agent.");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
