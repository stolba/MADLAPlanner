package cz.agents.dimaptools.heuristic.relaxed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.InitializableHeuristic;
import cz.agents.dimaptools.util.SharedProblemInfoProvider;

public class LazilyDistributedSAFFPersonalizedRequestHeuristic extends LazilyDistributedSAFFRequestHeuristic implements InitializableHeuristic {

	
	private final SharedProblemInfoProvider provider;
    private final int requestThreshold;
    private final Set<String> shouldSendRequestsTo = new HashSet<>();
	
	public LazilyDistributedSAFFPersonalizedRequestHeuristic(DIMAPWorldInterface world, SharedProblemInfoProvider provider,int maxRecursionDepth, int requestThreshold) {
		super(world, maxRecursionDepth);

		shouldSendRequestsTo.add(world.getAgentName());	//necessary for self-requests
        
        this.provider = provider;
        this.requestThreshold=requestThreshold;
	}

	@Override
	public void initialize() {
		provider.sendInfoAndWait();
        for(String agent : provider.getKnownAgents()){
			if(provider.getCoupling(agent) <= requestThreshold){
				shouldSendRequestsTo.add(agent);
			}
		}
	}
	
	@Override
	protected void sendRequest(String agent, int reqHash, List<Integer> actionIDs, int recursionDepth) {
		if(shouldSendRequestsTo.contains(agent)){
			super.sendRequest(agent, reqHash, actionIDs, recursionDepth);
		}
	}

}
