package tcmis.mainpackage;

/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import java.util.ArrayList;

import jade.core.Agent;

public class Train extends Agent {
	ArrayList<TrainUnit> trainUnits = new ArrayList<TrainUnit>();
	ArrayList<Integer> route = new ArrayList<Integer>();

	public enum State {
		Waiting, Driving, Stop
	}

	State trainState = State.Stop;

	protected void setup() {
		System.out.println("Hello World. I am an Train!");
		
		// Define the number of train units
		for (int i = 0; i < 6; i++) {
			trainUnits.add(new TrainUnit());
		}
		for (int i = 0; i < 6; i++) {
			addStationToRoute(i);
		}
		
		changeState(State.Driving);
		
	}

	/**
	 * Add an trainstation to the train route
	 * 
	 * @param station
	 *            The station id
	 */
	public boolean addStationToRoute(int station) {
		// search if station already exists
		for (int i = 0; i < route.size(); i++) {
			if (route.get(i) == station) {
				return false;
			}
		}
		route.add(station);
		System.out.println("Station " + station + " added to route " + getAID().getLocalName());
		return true;
	}

	/**
	 * Removes the station from the route
	 * 
	 * @param station
	 *            The station id
	 * @return True if station is deleted from route
	 */
	public boolean removeStationFromRoute(int station) {

		// search for station
		for (int i = 0; i < route.size(); i++) {
			if (route.get(i) == station) {
				route.remove(i);
				return true;
			}
		}
		// Station is not found
		System.out.println("Station " + station + " removed from route " + getAID().getLocalName());
		return false;
	}

	/**
	 * Change the state of the train to "Waiting", "Driving" or "Stop"
	 * 
	 * @param state
	 *            State enum
	 */
	public void changeState(State state) {
		trainState = state;
		System.out.println("State changed " + state+ " to " + getAID().getLocalName());
		
	}

	// TODO create some sort of method to return the state

}
