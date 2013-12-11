package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

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
		gui.setVisible(true);
		
		
	}
	
	public AID[] getAgents() {
		AMSAgentDescription[] AMSAgents = null;
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults(new Long(-1));
			AMSAgents = AMSService.search(this, new AMSAgentDescription(), c);
		} catch (Exception e) {
			System.out.println("Problem searching AMS: " + e);
			e.printStackTrace();
		}

		List<AID> agents = new ArrayList<AID>();
		
		AID myID = getAID();
		for (int i = 0; i < AMSAgents.length; i++) {
			AID agentID = AMSAgents[i].getName();

			if (agentID.getName().startsWith("train_") || agentID.getName().startsWith("station_")) {
				agents.add(agentID);
			}
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
