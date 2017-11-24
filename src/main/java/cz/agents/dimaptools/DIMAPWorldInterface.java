package cz.agents.dimaptools;

import cz.agents.alite.communication.CommunicationPerformer;
import cz.agents.alite.communication.Communicator;
import cz.agents.dimaptools.communication.protocol.EncoderInterface;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.Problem;

public interface DIMAPWorldInterface {
	
	public String getAgentName();
	
	public int getAgentID();
	
	public Communicator getCommunicator();
	
	public CommunicationPerformer getCommPerformer();
	
	public EncoderInterface getEncoder();
	
	public Problem getProblem();
	
	public Domain getDomain();

	public int getNumberOfAgents();

}
