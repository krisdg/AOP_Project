package tcmis.mainpackage;

import jade.core.Agent;
import jade.core.AgentContainer;
import jade.lang.acl.*;
import jade.wrapper.AgentController;
import jade.core.behaviours.*;

public class Receiver extends Agent{

	int i = 0;
	
	 protected void setup() 
	    {
			addBehaviour(new CyclicBehaviour(this) 
			{
				 public void action() 
				 {
					ACLMessage msg = receive();
					if (msg!=null) {
						System.out.println( " - " +
						   myAgent.getLocalName() + " received: " +
						   msg.getContent() );
						
						ACLMessage reply = msg.createReply();
						reply.setPerformative( ACLMessage.INFORM );
						reply.setContent(" Polo" );
						send(reply);
					 }
					 block();
				 }
			});
			

		}
	
}
