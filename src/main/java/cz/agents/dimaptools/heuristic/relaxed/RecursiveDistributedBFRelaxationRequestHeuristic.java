package cz.agents.dimaptools.heuristic.relaxed;

import java.util.PriorityQueue;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.State;

public class RecursiveDistributedBFRelaxationRequestHeuristic extends RecursiveDistributedRelaxationRequestHeuristic {

    public RecursiveDistributedBFRelaxationRequestHeuristic(
			DIMAPWorldInterface world, EvaluatorInterface evaluator,
			int maxRecursionDepth) {
		super(world, evaluator, maxRecursionDepth);
		// TODO Auto-generated constructor stub
	}



    

    /**
     * Queue requests from the search which cannot be computed at the moment
     */
    private PriorityQueue<LocalHeuristicRequest> localRequests = new PriorityQueue<LocalHeuristicRequest>();



    @Override
    public void getHeuristic(State state, HeuristicComputedCallback callback) {
        
//		LOGGER.info(domain.agent + "("+id+")" + "Get H(" + (heuristicCounter ++) + "): requestHandler.queueSize():"+requestHandler.queueSize()+", replyHandler.queueSize():"+replyHandler.queueSize()+", requests.size():"+requests.size());

        //if waiting for some replies, queue the local request
        if(requests.size() > 0){
            localRequests.add(new LocalHeuristicRequest(state, callback));
//			LOGGER.info(agentName + "("+id+") localRequests: " + localRequests.size());
            return;
        }

        //build the EQ
//        LOGGER.info(agentName + "("+id+") compute h for: " + state.hashCode());

        currentCallback = callback;
        currentState = state;
        requestedActions.clear();

        buildGoalPropositions(problem.goalSuperState);
        setupExplorationQueue();
        setupExplorationQueueState(state);
        relaxedExploration();

        //if not waiting for replies, finish the heuristic
        if(requests.size() == 0){
//			LOGGER.info(domain.agent + "("+id+")" + " Computed H(" + (heuristicCounter --) + "): requestHandler.queueSize():"+requestHandler.queueSize()+", replyHandler.queueSize():"+replyHandler.queueSize()+", requests.size():"+requests.size());
            int totalCost = evaluator.getTotalCost(goalPropositions);
            HelpfulActions ha = totalCost < HeuristicInterface.LARGE_HEURISTIC ? evaluator.getHelpfulActions(state) : new HelpfulActions();
            callback.heuristicComputed(new HeuristicResult(totalCost,ha));
        }
    }



    /**
     * This method used to process messages, but now is used only to periodically check
     * if there are any local requests to process
     */
    @Override
    public void processMessages() {

        while(requests.size() == 0 && localRequests.size() > 0){
            LocalHeuristicRequest lr = localRequests.poll();
            
            getHeuristic(lr.state, lr.callback);
        }

    }




}
