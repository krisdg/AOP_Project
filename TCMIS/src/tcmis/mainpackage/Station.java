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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tcmis.mainpackage.Car.ReceiveBehaviour;

public class Station extends Agent {
	private int positionX, positionY;
	Dictionary<Car, Station> request;
	private String rememberAgent;
	private SearchConstraints c = new SearchConstraints();

	protected void setup() {
		c.setMaxResults (new Long(-1));
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
		if (height < 0) {
			height *= -1;// -490 * -1= 490
		}
		// stelling van pytagoras
		// als de breedte kleiner is dan 0 wordt de negatieve breedte positief.
		if (width < 0) {
			width *= -1; // -80 * -1 = 80;
		}
		// sqrt(490*490 + 80 * 80) = sqrt(240100 + 6400) = sqrt (246800) =
		result = Math.sqrt((height * height) + (width * width));

		return result;

	}

	public static void main(String[] args) {
		Station stat = new Station();
		System.out.println(stat.calculateDistance(10, 20, 500, 100));
	}


	private void addRequest(int x, int y) {

	}

	private void spawnTrain(int gotoX, int gotoY) {

	}
	
	private void addRecievers(ACLMessage msg, String agent){
		try {
			AMSAgentDescription [] agents = 
					AMSService.search( this, new AMSAgentDescription (), c );
			for (int i=0; i<agents.length;i++)
				if(agents[i].getName().getLocalName().toLowerCase().contains(agent))
					msg.addReceiver( agents[i].getName()); 
		} catch (FIPAException e) {
			System.out.println("HOUSTON WE HAVE A PROBLEM LOOKING FOR FEDERAL AGENTS.");
		};
	}
	
	class RecieveBehavior extends CyclicBehaviour{

		
        
		RecieveBehavior(Agent a){
			super(a);
		}
		
		@Override
		public void action() {
			ACLMessage msg = receive();
			receiveRequest(msg);
			// TODO Auto-generated method stub
			
		}

		/**
		 * receiveRequest handles all the requests that the station receives.
		 * @param request
		 */
		private void receiveRequest(ACLMessage msg) {
			// TODO: get requests and
			ACLMessage reply = msg.createReply();
			
			
			switch(requestSelector(msg.getContent())){
			case 0:
				//TODO: determine the car closest to the station.
				
				break;
			case 1:
				//TODO: send location to monitor
				break;
			case 2:
				//TODO: request locations of Cars
				reply.setPerformative(ACLMessage.INFORM);
				reply.setContent("LOCATION");
				addRecievers(msg, "CAR_");
				send(reply);
				break;
			case 3:
				//TODO: send car to his destination
				break;
			default:
				System.out.println("Recieved Command not recognized.");
				break;	
			}
		}
		
		/**
		 * 
		 */
		private AID getClosestCar(ACLMessage msg){
			AID aid = null;
			Map<Integer, AID> agentList = new HashMap<Integer, AID>();
			String params[] = msg.getContent().replace("LOCATION:", "").split(";");
			
			if("UNAVAILABLE".contains(params[2])){
				//TODO: Do not add to the list
			}else if ("AVAILABLE".contains(params[2])){
				
			}else {
			System.out.println("Something went wrong");
			
			}
			return aid;
		}
		
		
		
		/**
		 * send car to his locations
		 * @param x
		 * @param y
		 * @return GOTO:X;Y
		 */
		private String sendCarToDestination(int x, int y){
			return "GOTO:"+x+";"+y;
			
		}

		/**
		 * Method to determine what request is received.
		 * 
		 * @param str
		 * @return 0 till 3 or a -1 if request is not known.
		 */
		private int requestSelector(String str) {
			String list[] = { "LOCATION:", "LOCATION", "FAILURE", "ACCOMPLISHED"};
			if (":".contains(str)) {
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
