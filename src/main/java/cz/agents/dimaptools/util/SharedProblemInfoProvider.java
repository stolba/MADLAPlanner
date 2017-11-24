package cz.agents.dimaptools.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.ReconstructRPMessage;
import cz.agents.dimaptools.communication.message.SharedProblemInfoMessage;
import cz.agents.dimaptools.communication.protocol.DistributedProblemInfoSharingProtocol;
import cz.agents.dimaptools.model.Problem;

public class SharedProblemInfoProvider {
	
	 private static final Logger LOGGER = Logger.getLogger(SharedProblemInfoProvider.class);
	
	private DistributedProblemInfoSharingProtocol protocol;
	
	private Map<String,Integer> couplings = new HashMap<>();
	private int waitingFor;
	final DIMAPWorldInterface world;
	
	public SharedProblemInfoProvider(final DIMAPWorldInterface world, int totalAgents){
		
		waitingFor = totalAgents-1;
		
		protocol = new DistributedProblemInfoSharingProtocol(world.getCommunicator(),world.getAgentName(),world.getEncoder()) {
			
			@Override
			public void process(SharedProblemInfoMessage msg, String sender) {
//				LOGGER.info(world.getAgentName() + " received send info from " + sender);
				couplings.put(sender, msg.getCouplingEstimate());
				--waitingFor;
			}

			
		};
		
		this.world = world;
		
	}
	
	public void sendInfoAndWait(){
		LOGGER.info(world.getAgentName() + " send info and wait...");
		protocol.sendSharedProblemInfoMessage(new SharedProblemInfoMessage(computeMyCoupling(world.getProblem())));
		
		while(waitingFor > 0){
			world.getCommPerformer().performReceiveNonblock();
		}
	}
	
	
	public int computeMyCoupling(Problem problem){
		return (int)(((double)problem.getMyPublicActions().size()) / ((double)problem.getMyActions().size()) * 100);
	}
	
	public Set<String> getKnownAgents(){
		return couplings.keySet();
	}
	
	public int getCoupling(String agent){
		return couplings.get(agent);
	}

}
