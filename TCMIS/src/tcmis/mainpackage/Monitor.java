package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tcmis.mainpackage.Car.ReceiveBehaviour;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;

public class Monitor extends GuiAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient protected MonitorOverview overview;
	transient protected MonitorGUI gui;

	// These constants are used by the Gui to post Events to the Agent
	public static final int EXIT = 1000;
	public static final int MOVE_EVENT = 1001;
	public static final int STOP_EVENT = 1002;
	public static final int CONTINUE_EVENT = 1003;
	public static final int REFRESH_EVENT = 1004;
	public static final int CLONE_EVENT = 1005;

	public List<String[]> elementsToDraw = new ArrayList<>();
	public List<String> elementsDrawn = new ArrayList<>();
	public List<String> stations = new ArrayList<>();

	public void setup() {
		// creates and shows the GUI
		overview = new MonitorOverview(this);
		gui = new MonitorGUI(this);

		// Add static stations for simulation
		createStation("STATION_A", 800, 300);
		createStation("STATION_B", 100, 200);
		createStation("STATION_C", 400, 100);

		addBehaviour(new ReceiveBehaviour(this));
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;

			public void action() {
				broadCastLocation();

				try {
					Thread.sleep(100);
				} catch (Exception e) {
					System.out.println("Problem sleeping: " + e);
					e.printStackTrace();
				}
				block();
			}
		});
	}

	public void broadCastLocation() {
		refreshGUI();

		AID[] agents = getAgents();

		// Ask all cars and stations for their location
		ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
		msg1.setContent("LOCATION");

		int receivers = 0;

		// Broadcast to all cars and stations.
		for (int i = 0; i < agents.length; i++) {
			if (agents[i].getLocalName().startsWith("STATION_")) {
				msg1.addReceiver(agents[i]);
				receivers++;
			}
		}

		if (receivers > 0)
			send(msg1);

		receivers = 0;

		// Ask all cars for their destination
		ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setContent("LOCDES");

		// Broadcast to all cars and stations.
		for (int i = 0; i < agents.length; i++) {
			if (agents[i].getLocalName().startsWith("CAR_")) {
				msg2.addReceiver(agents[i]);
				receivers++;
			}
		}

		if (receivers > 0)
			send(msg2);

	}

	public AID[] getAgents() {
		AMSAgentDescription[] AMSAgents = null;
		List<AID> agents = new ArrayList<AID>();
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			AMSAgents = AMSService.search(this, new AMSAgentDescription(), c);

			AID myID = getAID();
			for (int i = 0; i < AMSAgents.length; i++) {
				AID agentID = AMSAgents[i].getName();

				if (agentID.getName().startsWith("CAR_")
						|| agentID.getName().startsWith("STATION_")) {
					agents.add(agentID);
				}
			}
		} catch (Exception e) {
			System.out.println("Problem searching AMS: " + e);
			e.printStackTrace();
		}

		return agents.toArray(new AID[agents.size()]);
	}

	void refreshGUI() {
		if (elementsToDraw.size() > 0) {
			overview.clearOverview();

			for (int x = 0; x < elementsToDraw.size(); x++) {
				String[] message = elementsToDraw.get(x)[1].split(":");

				if (elementsDrawn.contains(elementsToDraw.get(x)[0]) == false) {
					if (message[0].startsWith("CAR")) {
						String[] locdes = message[2].split(";");

						int locx = Integer.parseInt(locdes[0]);
						int locy = Integer.parseInt(locdes[1]);
						int desx = Integer.parseInt(locdes[2]);
						int desy = Integer.parseInt(locdes[3]);

						Color color;

						if (message[3].equals("AVAILABLE")) {
							color = new Color(0, 190, 0);
						} else {
							color = new Color(190, 0, 0);
						}

						overview.addCar(locx, locy, desx, desy, color,
								message[0]);
					}

					if (message[0].startsWith("STATION")) {
						String stationID = message[0]
								.replaceAll("STATION_", "");
						String[] loc = message[2].split(";");

						int locx = Integer.parseInt(loc[0]);
						int locy = Integer.parseInt(loc[1]);

						overview.addStation(locx, locy, Color.BLACK, stationID);
					}
					elementsDrawn.add(elementsToDraw.get(x)[0]);
				}
			}

			overview.redraw();

			elementsToDraw.clear();
			elementsDrawn.clear();
		}

		List<String> newStations = new ArrayList<>();
		AID[] agents = getAgents();
		for (int x = 0; x < agents.length; x++) {
			if (agents[x].getLocalName().startsWith("STATION_"))
				newStations.add(agents[x].getLocalName());
		}
		if (newStations.size() != stations.size()) {
			stations = newStations;
			gui.refreshStations(stations.toArray(new String[stations.size()]));
		}

	}

	void createStation(String name, int posX, int posY) {
		// Create new station
		CreateAgent ca = new CreateAgent();

		ca.addArguments(posX + ";" + posY);
		ca.setAgentName(name);
		ca.setClassName(Station.class.getName());
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

	public void createSimulatorAgent() {
		CreateAgent ca = new CreateAgent();

		ca.setAgentName("SIMULATOR");
		ca.setClassName(Test.class.getName());
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

	public void addRequest(String goFrom, String goTo) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("ADDREQUEST:" + goTo);

		AID[] agents = getAgents();

		// Sending request
		for (int i = 0; i < agents.length; i++)
			if (agents[i].getLocalName().equals(goFrom))
				msg.addReceiver(agents[i]);

		send(msg);
	}

	public void setTimeInterval(int crowdness) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent("SETINTERVAL:" + crowdness);

		AID[] agents = getAgents();

		// Sending request
		for (int i = 0; i < agents.length; i++)
			if (agents[i].getLocalName().startsWith("SIMULATOR"))
				msg.addReceiver(agents[i]);

		send(msg);
	}

	// AGENT OPERATIONS FOLLOWING GUI EVENTS
	protected void onGuiEvent(GuiEvent ev) {
		switch (ev.getType()) {
		case EXIT:
			break;
		case MOVE_EVENT:
			break;
		case CLONE_EVENT:
			break;
		case STOP_EVENT:
			break;
		case CONTINUE_EVENT:
			break;
		case REFRESH_EVENT:
			break;
		}
	}

	class ReceiveBehaviour extends CyclicBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ReceiveBehaviour(GuiAgent a) {
			super(a);
		}

		public void action() {
			while (true) {
				ACLMessage msg = receive();

				if (msg != null) {
					String content = "" + msg.getContent();

					if (msg.getSender().getName().startsWith("CAR_")) {
						if (content.startsWith("LOCDES:")) {
							elementsToDraw.add(new String[] {
									msg.getSender().getLocalName(),
									msg.getSender().getLocalName() + ":"
											+ msg.getContent() });
						}
					}
					if (msg.getSender().getName().startsWith("STATION_")) {
						if (content.startsWith("LOCATION:")) {
							elementsToDraw.add(new String[] {
									msg.getSender().getLocalName(),
									msg.getSender().getLocalName() + ":"
											+ msg.getContent() });
						}
					}
				} else {
					break;
				}
			}

			block();
		}
	}
}
