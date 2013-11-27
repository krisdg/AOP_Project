package tcmis.mainpackage;

/**
 * Section 4.1.1, Page 52

 * The AID class provides methods to retrieve the local name (getLocalName()), 
 * the GUID (getName()) and the addresses (getAllAddresses()). We can therefore 
 * enrich the welcome message of our HelloWorldAgent as in this example.
 **/

import jade.core.Agent;

import java.util.ArrayList;
import java.util.Iterator;

public class Station extends Agent {
	ArrayList<Integer> platforms = new ArrayList<Integer>();
	int busyIndicator = 1;
	
	int numberOfTest = 0;
	
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello World. I am an agent!");
		System.out
				.println("Hello World. I am an agent! En Michiel is de beste!!");
		System.out.println("My local-name is " + getAID().getLocalName());
		System.out.println("My GUID is " + getAID().getName());
		System.out.println("My addresses are:");
		Iterator it = getAID().getAllAddresses();
		while (it.hasNext()) {
			System.out.println("- " + it.next());
		}
		
		unitTestPlatform();
	}
	
	public boolean addPlatform(int platform){
		for (int i = 0; i < platforms.size(); i++) {
			if(platforms.get(i) == platform){
				return false; //if platform already exitst, stop method.
			}
		}
		platforms.add(platform);
		return true;
	}
	
	/**
	 * Delete platform from station
	 * @param platform the platform id
	 * @return true if the platform is deleted succesfull
	 */
	public boolean deletePlatform(int platform){
		for (int i = 0; i < platforms.size(); i++) {
			if(platforms.get(i) == platform){
				platforms.remove(i);
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Test if the add and delete methods work as they should
	 */
	private void unitTestPlatform(){
		unitTest(addPlatform(1), true);
		unitTest(addPlatform(1), false);
		unitTest(addPlatform(2), true);
		unitTest(addPlatform(3), true);
		unitTest(addPlatform(4), true);
		unitTest(addPlatform(2), false);
		
		unitTest(deletePlatform(1), true);
		unitTest(deletePlatform(1), false);
		unitTest(addPlatform(1), true);
		unitTest(addPlatform(1), false);
		
		unitTest(deletePlatform(1), true);
		unitTest(addPlatform(1), true);
		unitTest(deletePlatform(1), true);
		unitTest(deletePlatform(1), false);
		unitTest(deletePlatform(4), true);
		unitTest(addPlatform(1), true);
		
	}
	
	/**
	 * Print if the result is the same as the answer
	 * @param result
	 * @param answer
	 */
	private void unitTest(boolean result, boolean answer){
		if((result && answer) || (!result && !answer))
			System.out.println(numberOfTest++ + " Successfull");
		else
			System.err.println(numberOfTest++ + " Unsuccessfull");
	}
	
}
