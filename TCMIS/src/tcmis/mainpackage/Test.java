package tcmis.mainpackage;

import java.util.ArrayList;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;

public class Test extends Agent {
	//AMSAgentDescription[] stations = null;
	AMSAgentDescription[] agents = null;
	ArrayList<AID> stations = new ArrayList<>();
	int interval = 10;

	protected void setup() {
		// Create a new communication behaviour for receiving commands		
		addBehaviour(new CyclicBehaviour(this){
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					System.out.println(" - " + myAgent.getLocalName()
							+ " received: " + msg.getContent());

					if (msg.getContent().startsWith("SETINTERVAL:")) {
						String split[] = msg.getContent().split(":");
						interval = Integer.parseInt(split[1]);
					}
				}
				block();
			}
		});
		
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				// acquire sender ID

				try {
					SearchConstraints c = new SearchConstraints();
					c.setMaxResults(new Long(-1));
					agents = AMSService.search(this.getAgent(),
							new AMSAgentDescription(), c);
				} catch (Exception e) {
					System.out.println("Problem searching AMS: " + e);
					e.printStackTrace();
				}
				stations.clear();
				for (int i = 0; i < agents.length; i++)
					if (agents[i].getName().getLocalName().startsWith("STATION"))
						stations.add(agents[i].getName());

				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

				/*
				 * Get a random station, chosen from the known stations.
				 */
				AID startStation = stations.get(randStation(stations.size()-1));
				AID destinationStation = stations.get(randStation(stations.size()-1));
				
				do{
					startStation = stations.get(randStation(stations.size()-1));
					destinationStation = stations.get(randStation(stations.size()-1));
				}
				while(startStation.getLocalName().equals(destinationStation.getLocalName()));
				
					
				msg.setContent("ADDREQUEST:"+ destinationStation.getLocalName());
				
				msg.addReceiver(startStation);
				send(msg);
				try {
					Thread.sleep(interval*1000);
				} catch (Exception e) {
					System.out.println("Problem sleeping: " + e);
					e.printStackTrace();
				}
				block();
			}
		});

	}

	public static int randStation(int max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();
		
		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt(max+1);

		return randomNum;
	}
}
