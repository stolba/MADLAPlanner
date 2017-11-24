package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.DistributedRequestHeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.State;

public class RecursiveDistributedRelaxationRequestHeuristic extends RelaxationHeuristic implements DistributedRequestHeuristicInterface {

    private final Logger LOGGER = Logger.getLogger(RecursiveDistributedRelaxationRequestHeuristic.class);

    protected final String id = "REQ";
    private final DistributedHeuristicRequestProtocol requestProtocol;
    private DistributedHeuristicReplyProtocol replyProtocol = null;

    private final Communicator comm;
    protected final String agentName;
    protected final int agentID;

    /**
     * Sent requests waiting for the reply
     */
    protected TIntObjectHashMap requests = new TIntObjectHashMap();
    protected HeuristicComputedCallback currentCallback;

    /**
     * Queue requests from the search which cannot be computed at the moment
     */
    private LinkedList<LocalHeuristicRequest> localRequests = new LinkedList<LocalHeuristicRequest>();

//	private int heuristicCounter = 0;

    protected final int maxRecursionDepth;
    
    private final boolean localRequestsLIFO;

    /**
     * Remember actions already requested
     */
    protected TIntHashSet requestedActions = new TIntHashSet();




    public RecursiveDistributedRelaxationRequestHeuristic(DIMAPWorldInterface world,EvaluatorInterface evaluator) {
        this(world,evaluator,Integer.MAX_VALUE);
    }
    
    public RecursiveDistributedRelaxationRequestHeuristic(DIMAPWorldInterface world,EvaluatorInterface evaluator,int maxRecursionDepth){
    	this(world,evaluator,maxRecursionDepth,true);
    }

    public RecursiveDistributedRelaxationRequestHeuristic(DIMAPWorldInterface world,EvaluatorInterface evaluator,int maxRecursionDepth,boolean localRequestsLIFO) {
        super(world.getProblem(),evaluator,true);

        comm = world.getCommunicator();
        agentName = world.getAgentName();
        agentID = world.getAgentID();

        this.maxRecursionDepth = maxRecursionDepth > -1 ? maxRecursionDepth : Integer.MAX_VALUE;
        this.localRequestsLIFO = localRequestsLIFO;

        requestProtocol = new DistributedHeuristicRequestProtocol(
                world.getCommunicator(),
                world.getAgentName(),
                world.getEncoder()){

            @Override
            public void receiveHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re, String sender) {
                if(LOGGER.isDebugEnabled())LOGGER.debug(requestProtocol.getAddress() + "("+id+")" + " handle reply from " + sender + ": " + re);
                processReply(re,sender);
            }

        };

    }

    /* (non-Javadoc)
	 * @see cz.agents.dimaptools.heuristic.relaxed.DistributedRequestHeuristicInterface#getRequestProtocol()
	 */
    @Override
	public DistributedHeuristicRequestProtocol getRequestProtocol(){
        return requestProtocol;
    }

    /* (non-Javadoc)
	 * @see cz.agents.dimaptools.heuristic.relaxed.DistributedRequestHeuristicInterface#setReplyProtocol(cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol)
	 */
    @Override
	public void setReplyProtocol(DistributedHeuristicReplyProtocol replyProtocol){
        this.replyProtocol = replyProtocol;
    }
    
    /* (non-Javadoc)
	 * @see cz.agents.dimaptools.heuristic.relaxed.DistributedRequestHeuristicInterface#isComputing()
	 */
    @Override
	public boolean isComputing(){
    	return requests.size() > 0;
    }
    
    /* (non-Javadoc)
	 * @see cz.agents.dimaptools.heuristic.relaxed.DistributedRequestHeuristicInterface#hasWaitingLocalRequests()
	 */
    @Override
	public boolean hasWaitingLocalRequests(){
    	return localRequests.size() > 0;
    }


    @Override
    public void getHeuristic(State state, HeuristicComputedCallback callback) {
        if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " get heuristic: " + domain.humanize(state.getValues()));

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
            		op != null && 			//i.e. initial state
            		op.shouldRequest && 	//is projection and not pure
            		maxRecursionDepth > 0  	//should send any requests at all
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




    protected void sendRequest(int action, int localCost, RelaxationHeuristicRequest req, UnaryOperator op, int recursionDepth) {
        if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " send request: " + problem.getAction(op.actionHash));

//		if(recursionDepth > 1){
//			if(LOGGER.isInfoEnabled())LOGGER.info(domain.agent + "("+id+")" + " sendRequest("+req.hashCode()+") to "+op.agent+",depth="+recursionDepth);
//		}

        //action was requested
        requestedActions.add(action);

        //store the request and wait for replies
        requests.put(req.hashCode(), req);

//		LOGGER.info(agentName + "("+id+")" + " SEND REQUEST("+req.hashCode()+"):"+requests.size() + ", rd:"+recursionDepth + ", current state hash:"+currentState.hashCode());
        Trace.it("send", "'" + agentName, "'" + op.agent, id, req.hashCode(), recursionDepth, currentState.hashCode(), req.waitingFor());

        String agent = op.agent;

        int[] reqOps = {op.operatorsIndex};

        HeuristicRequestMessage reqm = new HeuristicRequestMessage(req.hashCode(), currentState.getValues(), reqOps,recursionDepth);

        //request may be for self, or other agent
        if(replyProtocol!=null && agent.equals(domain.agent)){
            replyProtocol.receiveHeuristicRequestMessage(reqm, domain.agent);
        }else{
            if(LOGGER.isDebugEnabled())LOGGER.debug(agentName + "("+id+")" + " send request " + reqm.humanize(problem.getDomain()));

            DataAccumulator.getAccumulator().heuristicRequestMessages ++;
            DataAccumulator.getAccumulator().totalBytes += reqm.getBytes();

            requestProtocol.sendHeuristicRequestMessage(reqm, agent);
        }
    }





    /**
     * This method used to process messages, but now is used only to periodically check
     * if there are any local requests to process
     */
    @Override
    public void processMessages() {

        while(requests.size() == 0 && localRequests.size() > 0){
            LocalHeuristicRequest lr;
            
            if(localRequestsLIFO){
            	lr = localRequests.pollLast();	//LIFO
            }else{
            	lr = localRequests.pollFirst();	//FIFO
            }

            getHeuristic(lr.state, lr.callback);
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
                if(!(a.isProjection() && a.getOwner().equals(sender)) && !requestedActions.contains(opIndex)){

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


    /**
     * Finish the exploration queue if a new proposition was added thanks to a received reply
     */
    // XXX: this strongly resembles RelaxationHeuristic.relaxedExploration, why it cannot use one universal code
    //      (generally evaluation coming from different heuristics of from local heuristic should be treated equally)
    public void finishExplorationQueue(){
        /*
         * Check this! We need to add propositions which were skipped, but also update costs
         * of propositions which were added, but can now be achieved another way
         */

        int unsolvedGoals = 0;
        for(Proposition g : goalPropositions){
            if(g.cost == -1)++unsolvedGoals;
        }

        //continue with the exploration
        while(!explorationQueue.isEmpty()){
            Proposition p = explorationQueue.poll();

            if(p.cost < p.distance) continue;

            if(p.isGoal && --unsolvedGoals <= 0){ //cheaper but incomplete test
                boolean all_goals = true;
                for(Proposition g : goalPropositions){ //complete test
                    if(g.cost == -1){
                        all_goals = false;
                        ++unsolvedGoals;
                    }
                }
                if(all_goals)break;
            }

            //compute cost
            evaluator.evaluateOperators(p.preconditionOf, p.cost);

            //trigger operators
            for(UnaryOperator op : p.preconditionOf){
                --op.unsatisfied_preconditions;
                if(op.unsatisfied_preconditions <= 0){
                    //use the original enqueue - do not send requests
                    super.enqueueIfNecessary(op.effect,op.cost,op);
                }
            }


        }

//		LOGGER.info(domain.agent + "("+id+")" + "finish eq done, pending requests:"+requests.size());

        //if there are no waiting requests, finish the heuristic
        if(requests.size() == 0){
//    		LOGGER.info(domain.agent + "("+id+")" + "Computed H(" + (heuristicCounter --) + "): requestHandler.queueSize():"+requestHandler.queueSize()+", replyHandler.queueSize():"+replyHandler.queueSize()+", requests.size():"+requests.size());

            int totalCost = evaluator.getTotalCost(goalPropositions);

            HelpfulActions ha = totalCost < HeuristicInterface.LARGE_HEURISTIC ? evaluator.getHelpfulActions((State)currentState) : new HelpfulActions();
            currentCallback.heuristicComputed(new HeuristicResult(totalCost,ha));
        }else{
//    		LOGGER.warn(domain.agent + "("+id+") - waiting for "+requests.size()+"!");
        }
    }





}
