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
import jade.core.AID;
import jade.core.Agent;
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
	
	public List<String[]> elementsToDraw = new ArrayList<>();
	public List<String> elementsDrawn = new ArrayList<>();

	public void setup() {
		// creates and shows the GUI
		gui = new MonitorGUI(this);
		
		addBehaviour(new ReceiveBehaviour(this));
		
		addBehaviour(new CyclicBehaviour(this) 
		{
			private static final long serialVersionUID = 1L;

			public void action() {
        		broadCastLocation();
        		
				try {
					Thread.sleep(200);
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
	
	void refreshGUI() {
		if (elementsToDraw.size() > 0) {
			gui.clearOverview();
			
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
						
						gui.addCar(locx, locy, desx, desy, color, message[0]);
					}
		
					if (message[0].startsWith("STATION")) {
						String stationID = message[0].replaceAll("STATION_", "");
						String[] loc = message[2].split(";");
						
						int locx = Integer.parseInt(loc[0]);
						int locy = Integer.parseInt(loc[1]);
						
						gui.addStation(locx, locy, Color.BLACK, stationID);
					}
					elementsDrawn.add(elementsToDraw.get(x)[0]);
				}
			}
			
			gui.redraw();
			
			elementsToDraw.clear();
			elementsDrawn.clear();
		}
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
			while(true) {
				ACLMessage msg = receive();
				
				if (msg != null) {
					System.out.println(" - " + myAgent.getLocalName()
							+ " received: " + msg.getContent() + " from: " + msg.getSender().getLocalName());
	
					if (msg.getSender().getLocalName().startsWith("CAR_")) {
						if (msg.getContent().startsWith("LOCDES:")) {
							elementsToDraw.add(new String[] {msg.getSender().getLocalName(), msg.getSender().getLocalName() + ":" + msg.getContent()});
						}
					}
					if (msg.getSender().getLocalName().startsWith("STATION_")) {
						if (msg.getContent().startsWith("LOCATION:")) {
							elementsToDraw.add(new String[] {msg.getSender().getLocalName(), msg.getSender().getLocalName() + ":" + msg.getContent()});
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
