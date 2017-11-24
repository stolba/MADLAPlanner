package cz.agents.dimaptools;

import cz.agents.alite.communication.CommunicationPerformer;
import cz.agents.alite.communication.Communicator;
import cz.agents.alite.communication.PerformerCommunicator;
import cz.agents.dimaptools.communication.protocol.EncoderInterface;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.Problem;

public class DefaultDIMAPWorld implements DIMAPWorldInterface {
	
	private static int currentID = 0;
	
	public final String name;
	public final int id;
	public final Communicator communicator;
	public final CommunicationPerformer commPerformer;
	public final EncoderInterface encoder;
	public final Problem problem;
	public final Domain domain;
	public final int totalNumberOfAgents;
	
	
	

	public DefaultDIMAPWorld(
			String name, 
			Communicator communicator,
			CommunicationPerformer commPerformer, 
			EncoderInterface encoder,
			Problem problem, 
			Domain domain,
			int totalNumberOfAgents) {
		this.name = name;
		this.communicator = communicator;
		this.commPerformer = commPerformer;
		this.encoder = encoder;
		this.problem = problem;
		this.domain = domain;
		this.totalNumberOfAgents = totalNumberOfAgents;
		
		id = currentID++;
	}
	
	public DefaultDIMAPWorld(
			String name, 
			PerformerCommunicator communicator,
			EncoderInterface encoder,
			Problem problem, 
			Domain domain,
			int totalNumberOfAgents) {
		this.name = name;
		this.communicator = communicator;
		this.commPerformer = communicator;
		this.encoder = encoder;
		this.problem = problem;
		this.domain = domain;
		this.totalNumberOfAgents = totalNumberOfAgents;
		
		id = currentID++;
	}
	
	public DefaultDIMAPWorld(
			String name, 
			PerformerCommunicator communicator,
			EncoderInterface encoder,
			Problem problem,
			int totalNumberOfAgents) {
		this.name = name;
		this.communicator = communicator;
		this.commPerformer = communicator;
		this.encoder = encoder;
		this.problem = problem;
		this.domain = null;
		this.totalNumberOfAgents = totalNumberOfAgents;
		
		id = currentID++;
	}
	
	@Override
	public String getAgentName(){
		return name;
	}

	@Override
	public Communicator getCommunicator() {
		return communicator;
	}

	@Override
	public CommunicationPerformer getCommPerformer() {
		return commPerformer;
	}

	@Override
	public EncoderInterface getEncoder() {
		return encoder;
	}

	@Override
	public Problem getProblem() {
		return problem;
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public int getAgentID() {
		return id;
	}

	@Override
	public int getNumberOfAgents() {
		return totalNumberOfAgents;
	}

}
