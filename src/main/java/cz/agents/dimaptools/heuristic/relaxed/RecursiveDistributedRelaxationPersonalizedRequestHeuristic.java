package cz.agents.dimaptools.heuristic.relaxed;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.InitializableHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.util.SharedProblemInfoProvider;

public class RecursiveDistributedRelaxationPersonalizedRequestHeuristic extends RecursiveDistributedRelaxationRequestHeuristic implements InitializableHeuristic {

    private final Logger LOGGER = Logger.getLogger(RecursiveDistributedRelaxationPersonalizedRequestHeuristic.class);

    
    private final SharedProblemInfoProvider provider;
    private final int requestThreshold;
    private final Map<String,Boolean> shouldSendRequestsTo = new HashMap<>();




    public RecursiveDistributedRelaxationPersonalizedRequestHeuristic(DIMAPWorldInterface world,EvaluatorInterface evaluator, int requestThreshold) {
        this(world,evaluator,new SharedProblemInfoProvider(world,world.getNumberOfAgents()),Integer.MAX_VALUE, requestThreshold);
    }

    public RecursiveDistributedRelaxationPersonalizedRequestHeuristic(DIMAPWorldInterface world,EvaluatorInterface evaluator, SharedProblemInfoProvider provider,int maxRecursionDepth, int requestThreshold) {
        super(world,evaluator,maxRecursionDepth);
        
        shouldSendRequestsTo.put(world.getAgentName(), true);	//necessary for self-requests
        
        this.provider = provider;
        this.requestThreshold=requestThreshold;
        

    }
    
    public void shareKnowledge(){
    	provider.sendInfoAndWait();
        for(String agent : provider.getKnownAgents()){
			if(provider.getCoupling(agent) > requestThreshold){
				shouldSendRequestsTo.put(agent, false);
				if(LOGGER.isInfoEnabled())LOGGER.info("  " + agentName + " will not send requests to " + agent + "(" + provider.getCoupling(agent) + ")");
			}else{
				shouldSendRequestsTo.put(agent, true);
				if(LOGGER.isInfoEnabled())LOGGER.info("  " + agentName + " will send requests to " + agent + "(" + provider.getCoupling(agent) + ")");
			}
		}
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
            		op != null && 						//i.e. initial state
            		op.shouldRequest && 				//is projection and not pure
            		maxRecursionDepth > 0 &&  			//should send any requests at all
            		shouldSendRequestsTo.get(op.agent)	//should send requests to the action's owner
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




    /**
     * When a reply is received, it is either directly submitted to the waiting request, or first requests to determine costs of used public actions are sent.
     * @param re
     * @param sender
     */
    public void processReply(final HeuristicReplyWithPublicActionsMessage re, String sender){

        if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " process reply: " + re);

//		LOGGER.info(domain.agent + "("+id+")" + " receiveReply("+re.getRequestHash()+")");

        //obtain the waiting request
        final RelaxationHeuristicRequest req = (RelaxationHeuristicRequest) requests.get(re.getRequestHash());

//		requests.remove(re.getRequestHash());

//    	LOGGER.info(agentName + "("+id+")" + " RECEVIED REPLY("+re.getRequestHash()+"):"+requests.size() + " with "+re.usedPublicActionIDs.length+ " public actions");

        if(re.getRecursionDepth()>DataAccumulator.getAccumulator().maxRecursionDepth)DataAccumulator.getAccumulator().maxRecursionDepth=re.getRecursionDepth();
//        LOGGER.info("RECURSION DEPTH:" + re.getRecursionDepth());

        if(req==null){
            LOGGER.error(agentName + "("+id+")" + " request "+re.getRequestHash()+" does not exist!");
            Trace.it("error-DNE", "'" + agentName, null, id, re.getRequestHash(), re.getRecursionDepth(), currentState.hashCode());
            return;
        }
        if(currentState.hashCode() != re.getStateHash()){
            LOGGER.error(agentName + "("+id+")" + " reply state hash ("+re.getStateHash()+") does not equal current state hash ("+currentState.hashCode()+")!");
        }
        //TODO: when receiving reply, the cost of an operator should probably be updated,
        //      so if it is used again (and no request is sent), the updated cost is used
        //      but it may be more complicated

        //If there are no public actions to be requested, submit the reply
        if(re.usedPublicActionIDs.length > 0 && re.getRecursionDepth() < maxRecursionDepth){
//    		LOGGER.info(domain.agent + "("+id+")" + " req "+re.getRequestHash()+" waiting for "+req.waitingFor());


            //else, send requests for all the used actions
            for(final int opIndex : re.usedPublicActionIDs){
                final UnaryOperator op = operators.get(opIndex);
                Action a = problem.getAction(op.actionHash);

                //XXX: prospectively improve naming -> "registerPendingReply"?
                req.waitForReply();
                Trace.it("increase", "'" + agentName, null, id, req.hashCode(), re.getRecursionDepth(), currentState.hashCode(), req.waitingFor());

                
                //if the action is from the agent who sent the reply, or if the action was already requested, ignore it
                if(!(a.isProjection() && a.getOwner().equals(sender)) && !requestedActions.contains(opIndex) && shouldSendRequestsTo.get(a.getOwner())){

//	    			LOGGER.info(domain.agent + "("+id+")" + " prepare inner request for op-"+opIndex);

                    //prepare the request
                    RelaxationHeuristicRequest newReq = new RelaxationHeuristicRequest(agentID, new HeuristicComputedCallback() {

                        @Override
                        public void heuristicComputed(HeuristicResult result) {
                            //submit the reply

                            //XXX: prospectively improve naming -> "processRequests"?
                            req.receiveReply(re.heuristicValue + result.getValue(),requests);

                            Trace.it("receive1", "'" + agentName, "'" + op.agent, id, req.hashCode(), re.getRecursionDepth(), currentState.hashCode(), req.waitingFor(), re.heuristicValue + result.getValue());
                            Trace.it("decrease", "'" + agentName, null, id, req.hashCode(), re.getRecursionDepth(), currentState.hashCode(), req.waitingFor());
                        }

                    });

                    newReq.waitForReply();
                    Trace.it("increase", "'" + agentName, null, id, newReq.hashCode(), re.getRecursionDepth() + 1, currentState.hashCode(), newReq.waitingFor());

                    //TODO: check the cost
                    sendRequest(op.operatorsIndex,op.cost,newReq,op,re.getRecursionDepth()+1);

                }else{
                    //XXX: prospectively improve naming -> "processRequests", "processReply"?
                    req.receiveReply(0,requests);
                    Trace.it("decrease", "'" + agentName, null, id, req.hashCode(), re.getRecursionDepth(), currentState.hashCode(), req.waitingFor());
                }

            }
        }


        Trace.it("receive0", "'" + agentName, "'" + sender, id, req.hashCode(), re.getRecursionDepth(), currentState.hashCode(), req.waitingFor()-1, re.heuristicValue);

        //XXX: prospectively improve naming -> "processRequests"?
        req.receiveReply(re.heuristicValue,requests);
        Trace.it("decrease", "'" + agentName, null, id, req.hashCode(), re.getRecursionDepth(), currentState.hashCode(), req.waitingFor());
    }

	@Override
	public void initialize() {
		shareKnowledge();
	}


   





}
