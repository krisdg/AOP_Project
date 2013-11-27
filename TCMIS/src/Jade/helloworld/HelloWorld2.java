package Jade.helloworld;
/**
 * Section 4.1.1, Page 52

 * The AID class provides methods to retrieve the local name (getLocalName()), 
 * the GUID (getName()) and the addresses (getAllAddresses()). We can therefore 
 * enrich the welcome message of our HelloWorldAgent as in this example.
 **/

import jade.core.Agent;

import java.util.Iterator;

public class HelloWorld2 extends Agent {

  protected void setup() { 
    // Printout a welcome message
    System.out.println("Hello World. I am an agent!");
    System.out.println("Hello World. I am an agent!");
    System.out.println("My local-name is "+getAID().getLocalName());
    System.out.println("My GUID is "+getAID().getName());
    System.out.println("My addresses are:");
    Iterator it = getAID().getAllAddresses();
    while (it.hasNext()) {
      System.out.println("- "+it.next());
    }
  }
}
