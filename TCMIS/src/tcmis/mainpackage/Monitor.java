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

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

public class Monitor extends GuiAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient protected MonitorGUI gui;

	// These constants are used by the Gui to post Events to the Agent
	public static final int EXIT = 1000;
	public static final int MOVE_EVENT = 1001;
	public static final int STOP_EVENT = 1002;
	public static final int CONTINUE_EVENT = 1003;
	public static final int REFRESH_EVENT = 1004;
	public static final int CLONE_EVENT = 1005;

	public void setup() {
		// creates and shows the GUI
		gui = new MonitorGUI(this);
		
		addBehaviour(new CyclicBehaviour(this) 
		{
			private static final long serialVersionUID = 1L;

			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					System.out.println(" - " + myAgent.getLocalName()
							+ " received: " + msg.getContent());

					if (msg.getSender().getLocalName().startsWith("CAR_")) {
						if (msg.getContent().startsWith("LOCDES:")) {
							String[] message = msg.getContent().replaceAll("LOCDES:", "").split(":");
							String[] locdes = message[0].split(";");
							
							int locx = Integer.parseInt(locdes[0]);
							int locy = Integer.parseInt(locdes[1]);
							int desx = Integer.parseInt(locdes[2]);
							int desy = Integer.parseInt(locdes[3]);
							
							Color color;
							
							if (message[1].equals("AVAILABLE")) {
								color = new Color(0, 190, 0);
							} else {
								color = new Color(190, 0, 0);
							}
							
							gui.addCar(locx, locy, desx, desy, color, msg.getSender().getLocalName());
						}
					}
					if (msg.getSender().getLocalName().startsWith("STATION_")) {
						if (msg.getContent().startsWith("LOCATION:")) {
							String[] loc = msg.getContent().replaceAll("LOCATION:", "").split(";");
							
							String stationID = msg.getSender().getLocalName().replaceAll("STATION_", "");
							
							int x = Integer.parseInt(loc[0]);
							int y = Integer.parseInt(loc[1]);
							
							gui.addStation(x, y, Color.BLACK, stationID);
						}
					}
				}

				block();
			}
		});
		
		addBehaviour(new CyclicBehaviour(this) 
		{
			private static final long serialVersionUID = 1L;

			public void action() {
				System.out.println("refresh");
        		broadCastLocation();
        		
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					System.out.println("Problem sleeping: " + e);
					e.printStackTrace();
				}
				block();
			}
		});
	}
	
	public void broadCastLocation() {
		gui.clearOverview();
		AID[] agents = getAgents();
		
		//Ask all cars and stations for their location
		ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
		msg1.setContent("LOCATION");
		
		int receivers = 0;
		
		//Broadcast to all cars and stations.
		for (int i=0; i<agents.length;i++) {
			if(agents[i].getLocalName().startsWith("STATION_")) {
				msg1.addReceiver(agents[i]); 
				receivers++;
			}
		}
		
		if (receivers > 0)
			send(msg1);
		
		receivers = 0;
		
		//Ask all cars for their destination
		ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
		msg2.setContent("LOCDES");

		//Broadcast to all cars and stations.
		for (int i=0; i<agents.length;i++) {
			if(agents[i].getLocalName().startsWith("CAR_")) {
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

				if (agentID.getName().startsWith("CAR_") || agentID.getName().startsWith("STATION_")) {
					agents.add(agentID);
				}
			}
		} catch (Exception e) {
			System.out.println("Problem searching AMS: " + e);
			e.printStackTrace();
		}

		return agents.toArray(new AID[agents.size()]);
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

}
