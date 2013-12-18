package tcmis.mainpackage;

import java.util.ArrayList;

import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.ContainerID;
import jade.lang.acl.*;
import jade.proto.AchieveREInitiator;
import jade.wrapper.AgentController;
import jade.core.behaviours.*;
import jade.domain.AMSService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;

public class Receiver extends Agent {

	
	
/*
 *	Get a valid name for a new Car. 
 */	
	String getValidCarName() {
		int availableCarNumber = 0;
		ArrayList<String> cars = new ArrayList<String>();
		AMSAgentDescription[] agents = null;
		String nameAgent = "Car_";

		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			agents = AMSService.search(this, new AMSAgentDescription(), c);
		} catch (Exception e) {
			System.out.println("Problem searching AMS: " + e);
			e.printStackTrace();
		}

		for (int i = 0; i < agents.length; i++)
			if (agents[i].getName().getLocalName().contains(nameAgent))
				cars.add(agents[i].getName().getLocalName());

		if (cars.isEmpty())
			availableCarNumber = 0;
		else
			for (int i = 0; i < cars.size(); i++)
				for (int j = 0; j <= cars.size(); j++)
					if (cars.get(i).equals(nameAgent + j))
						availableCarNumber = i + 1;

		nameAgent = nameAgent + availableCarNumber;
		return nameAgent;
	}

/*
 *	Create a new Car Agent. 
 *
 */
	void createCar() {
		CreateAgent ca = new CreateAgent();
		ca.setAgentName(getValidCarName());
		ca.setClassName(this.getClass().getName());
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
					System.out.println("Agent successfully created");
				}

				protected void handleFailure(ACLMessage failure) {
					System.out.println("Error creating agent.");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void setup() {
		addBehaviour(new CyclicBehaviour(this) {
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					System.out.println(" - " + myAgent.getLocalName()
							+ " received: " + msg.getContent());

					if (msg.getContent().equals("derp")) {
						createAgent();
					}
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(" Polo");
					send(reply);
				}
				block();
			}
		});

	}

}
