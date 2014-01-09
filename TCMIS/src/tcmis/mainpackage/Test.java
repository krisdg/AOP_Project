package tcmis.mainpackage;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;

public class Test extends Agent {
	AMSAgentDescription[] stations = null;
	AMSAgentDescription[] agents = null;

	protected void setup() {
		// search all available agents.

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

				for (int i = 0; i < agents.length; i++)
					if (agents[i].getName().getLocalName().contains("STATION_"))
						stations[i] = agents[i];

				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

				/*
				 * Get a random station, chosen from the known stations.
				 */
				msg.setContent("totootje!");// moet nog ff juiste syntax..
				msg.addReceiver(stations[randStation(stations.length)]
						.getName());
				send(msg);
				try {
					Thread.sleep(5000);
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
		int randomNum = rand.nextInt((max) + 1);

		return randomNum;
	}
}