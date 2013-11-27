package tcmis.mainpackage;
/**
 * Section 4.1, Page 51
 * Creating a JADE agent is as simple as defining a class that extends the jade.core.Agent 
 * class and implementing the setup() method as exemplified in the code below.
 **/

import jade.core.Agent;

public class Train extends Agent {

  protected void setup() { 
    // Printout a welcome message
    System.out.println("Hello World. I am an agent!");
  }
}
