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
						

//						Object [] args = new Object[2];
//				        args[0] = ""+i;
//				        args[1] = "Gimmie koekjes";
//					 
//				        String name = "Zoekmachine_"+i ;
//				        jade.wrapper.AgentContainer c = getContainerController();
//				        try {
//				            AgentController a = c.createNewAgent( name, "Receiver", args );
//				            a.start();
//				        }
//				        catch (Exception e){
//				        	System.out.println("MEEEEH 	" + e);
//				        }
						
						
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
