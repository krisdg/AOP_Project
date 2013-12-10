package tcmis.mainpackage;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;


public class Sender extends Agent {
	String sender = null;
	
    protected void setup() 
    {
		//search all available agents.
		AMSAgentDescription [] agents = null;
      	try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (new Long(-1));
			agents = AMSService.search( this, new AMSAgentDescription (), c );
		}
		catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
		}
		
		//msg obj
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent( "Marco" );

		//Sending all trains.
		for (int i=0; i<agents.length;i++)
			if(agents[i].getName().getLocalName().toLowerCase().contains("train"))
				msg.addReceiver( agents[i].getName()); 

		send(msg);
		
		addBehaviour(new CyclicBehaviour(this) 
		{
			 public void action() {
				//acquire sender ID
				ACLMessage msg= receive();
				if (msg!=null){
					sender = msg.getSender().getName();
					System.out.println( " - " +
							   myAgent.getLocalName() + " received: " +
							   msg.getContent() + " from " +  
							   msg.getSender().getName() );
					System.out.println(sender);
					}
				block();
			 }
		});

    	
	}
}