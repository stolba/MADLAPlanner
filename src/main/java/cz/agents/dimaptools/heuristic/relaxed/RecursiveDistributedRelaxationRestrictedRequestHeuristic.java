package cz.agents.dimaptools.heuristic.relaxed;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;

public class RecursiveDistributedRelaxationRestrictedRequestHeuristic extends RecursiveDistributedRelaxationRequestHeuristic {

    
	private final Logger LOGGER = Logger.getLogger(RecursiveDistributedRelaxationRestrictedRequestHeuristic.class);







	public RecursiveDistributedRelaxationRestrictedRequestHeuristic(
			DIMAPWorldInterface world, EvaluatorInterface evaluator,int maxRecursionDepth) {
		super(world, evaluator, maxRecursionDepth);
	}







	






    /**
     * Override the default enqueue operation
     * @param p
     * @param cost
     * @param op
     **/
    public void enqueueIfNecessary(final Proposition p, int cost, final UnaryOperator op){
//		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " enqueue if necessary ");


        //XXX: recursion depth is effectively never checked
        //final int recursionDepth = 0;

//		if(op != null)LOGGER.info(domain.agent + " ENQUEUE " + op.operatorsIndex + ", action:" + problem.getAction(op.actionHash));

        if(cost >= HeuristicInterface.LARGE_HEURISTIC){
            LOGGER.warn("IMPOSSIBLE!");
        }
        
        if(p.cost == -1 || p.cost > cost){

            //if operator is owned by other agent (maybe isProjection check should suffice)
            if(
            		op != null && 					//i.e. initial state
            		op.shouldRequest && 			//is projection and not pure
            		maxRecursionDepth > 0 &&		//should send any requests at all
            		cost==op.baseCost				//operator is in the first RPG layer
            		){
                if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " enqueue public operator: " + problem.getAction(op.actionHash));

                final RelaxationHeuristicRequest req = new RelaxationHeuristicRequest(agentID, new HeuristicComputedCallback() {

                    @Override
                    public void heuristicComputed(HeuristicResult result) {

                        //the request and all its sub-requests are finished - add the proposition to the EQ
                        if(result.getValue() < LARGE_HEURISTIC){
                            p.distance = result.getValue();
                            p.cost = result.getValue();
                            p.reachedBy = op;
                            explorationQueue.add(p);
                        }

                        //finish EQ with the new proposition
                        finishExplorationQueue();
                    }

                });

                req.waitForReply();
                Trace.it("increase", "'" + agentName, null, id, req.hashCode(), 1, currentState.hashCode(), req.waitingFor());

                sendRequest(op.operatorsIndex,cost,req,op,1);
            }else{
                //if the operator is public, consider it requested, so it is not requested when received from other agent
                if(op != null && problem.getAction(op.actionHash).isPublic()){
                    requestedActions.add(op.operatorsIndex);
                }

                //enqueue normally
                p.cost = cost;
                p.distance = cost;
                p.reachedBy = op;
                explorationQueue.add(p);
            }
        }
    }


}
