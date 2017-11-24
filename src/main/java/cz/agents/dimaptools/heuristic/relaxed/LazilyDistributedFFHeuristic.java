package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntHashSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicProtocol;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class LazilyDistributedFFHeuristic extends RelaxationHeuristic {

	private final Logger LOGGER;

	private final String id;
	private final int agentID;

	private final DistributedHeuristicProtocol protocol;
	private DistributedHeuristicProtocol otherProtocol = null;
	private final Communicator comm;
	
    private RelaxedPlan rp = new RelaxedPlan();
    RelaxationHeuristicRequest currentRequest;

    private LinkedList<LocalHeuristicRequest> localRequests = new LinkedList<LocalHeuristicRequest>();
    private TIntHashSet requestedActions = new TIntHashSet();

    private final int maxRecursionDepth;

    public LazilyDistributedFFHeuristic(DIMAPWorldInterface world,boolean receiveRequests) {
    	this(world, receiveRequests, Integer.MAX_VALUE);
    }

	public LazilyDistributedFFHeuristic(DIMAPWorldInterface world,boolean receiveRequests,int maxRecursionDepth) {
		super(world.getProblem(),new FFEvaluator(world.getProblem()),true);

		this.comm = world.getCommunicator();
		agentID = world.getAgentID();
		this.maxRecursionDepth = maxRecursionDepth > -1 ? maxRecursionDepth : Integer.MAX_VALUE;
		
		currentRequest = new RelaxationHeuristicRequest(agentID,null);

		protocol = new DistributedHeuristicProtocol(world.getCommunicator(),world.getAgentName(),world.getEncoder(),receiveRequests) {
			
			@Override
			public void receiveHeuristicRequestMessage(HeuristicRequestMessage req, String sender) {
				if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + "("+id+")" + " received request from "+sender+": " + req.getRequestHash());

            	processRequest(req,sender);
			}
			
			@Override
			public void receiveHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re, String sender) {
				if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + "("+id+")" + " received reply from "+sender+": " + re.getRequestHash());

            	processReply(re,sender);
			}
		};
		
		if(receiveRequests){
			id = "receive";
		}else{
			id = "send";
		}

		LOGGER = Logger.getLogger(problem.agent + "." + RelaxationHeuristic.class);
	}
	
	public DistributedHeuristicProtocol getProtocol() {
		return protocol;
	}
	
	public void setOtherProtocol(DistributedHeuristicProtocol otherProtocol){
		this.otherProtocol = otherProtocol;
	}
	
	

	public void mark(Proposition goal){
		if(!goal.marked){
			goal.marked = true;
			UnaryOperator op = goal.reachedBy;
			if(op != null){
				for(Proposition p : op.precondition){
					mark(p);
				}
				rp.add(op.actionHash);
			}
		}
	}



	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {

		if(currentRequest.waitingFor() > 0){
			localRequests.add(new LocalHeuristicRequest(state, callback));
			return;
		}

		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " get heuristic: " + domain.humanize(state.getValues()));

		currentState = state;

		getHeuristic(state, problem.goalSuperState, callback);

	}


	public void getHeuristic(State state, SuperState goal, HeuristicComputedCallback callback) {

		
		
		if(currentRequest.waitingFor() > 0){
				LOGGER.error(comm.getAddress() + "("+id+")" + " starting heuristic from scratch, but still waiting for: "+currentRequest.waitingFor());
		}
		
		rp = new RelaxedPlan();
		currentRequest = new RelaxationHeuristicRequest(agentID,callback);
		if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + "("+id+")" + " new currentRequest: "+currentRequest.hashCode());
		requestedActions.clear();
		
//		Trace.it("	",comm.getAddress(),"already requested actions(clear):",currentRequest.hashCode(),requestedActions.size());
		
//		Trace.it(comm.getAddress(),".getHeuristic:",currentRequest.hashCode(),state.hashCode(),goal.hashCode());
		

		buildGoalPropositions(goal);
		setupExplorationQueue();
		setupExplorationQueueState(state);
		relaxedExploration();

		for(Proposition p : goalPropositions){
			if(p.cost == -1){
				if(LOGGER.isDebugEnabled())LOGGER.debug(problem.agent + " h_ldFF: DEAD_END");
				callback.heuristicComputed(new HeuristicResult());
				return;
			}
		}

		for(Proposition p : goalPropositions){
			mark(p);
		}
		
		currentRequest.waitForReply();
		
		
		
		Map<String,List<Integer>> requests = new HashMap<>();
		int localRPcost = 0;

		for(int i : rp.toArray()){
			Action a = problem.getAction(i);
			if(a.isProjection()){
				if(!requestedActions.contains(a.hashCode())){
					if(!requests.containsKey(a.getOwner()))requests.put(a.getOwner(), new LinkedList<Integer>());
					requests.get(a.getOwner()).add(a.hashCode());
				}
			}else{
				requestedActions.add(a.hashCode());
				localRPcost += a.getCost();
			}
		}
		
		Trace.it(comm.getAddress(),".getHeuristic(localRPcost):",currentRequest.hashCode(),localRPcost);
		for(int id : rp.toArray()){
			Trace.it("	",comm.getAddress(),"rp:",id,problem.getAction(id).getLabel());
		}
		
//		Trace.it("	",comm.getAddress(),"already requested actions:",currentRequest.hashCode(),requestedActions.size());
		
		
		for(String agent : requests.keySet()){
			sendRequest(agent,currentRequest.hashCode(), requests.get(agent),1);
		}
		
		currentRequest.receiveReply(localRPcost, null);



	}

	public RelaxedPlan getLocalRP(State state, SuperState goal, RelaxedPlan foundRP) {

		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " get local RP: s:" + domain.humanize(state.getValues()) + ", goal: " + domain.humanize(goal.getValues()));

		buildGoalPropositions(goal);
		setupExplorationQueue();
		
		if(foundRP!=null){//to reflect already reached actions
			for(int aid : foundRP.toArray()){
				setupExplorationQueueState(problem.getAction(aid).getEffect());
			}
		}
		
		setupExplorationQueueState(state);
		relaxedExploration();

		for(Proposition p : goalPropositions){
			if(p.cost == -1){
				if(LOGGER.isTraceEnabled())LOGGER.trace(problem.agent + " h_ldFF: DEAD_END");
				return null;
			}
		}

		for(Proposition p : goalPropositions){
			mark(p);
		}

		return rp;

	}


	private void sendRequest(String agent, int reqHash, List<Integer> actionIDs, int recursionDepth) {
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + "("+id+")" + " send request: " + actionIDs);

		

		if(recursionDepth > maxRecursionDepth){
			return;
		}
		
//		Trace.it(comm.getAddress(),".sendRequest:",agent,reqHash,actionIDs,recursionDepth);
		
//		for(int id : actionIDs){
//			Trace.it("	",comm.getAddress(),"requesting:",id,problem.getAction(id).getLabel());
//		}

//		if(recursionDepth > 1){
//			LOGGER.info(domain.agent + "("+id+")" + " sendRequest depth="+recursionDepth);
//		}
		
		int[] reqOps = new int[actionIDs.size()];
		for(int i=0; i<reqOps.length; ++i)reqOps[i]=actionIDs.get(i);

		currentRequest.waitForReply();
		requestedActions.addAll(reqOps);
		
//		Trace.it("	",comm.getAddress(),"already requested actions:",reqHash,requestedActions.size());
		

		HeuristicRequestMessage req = new HeuristicRequestMessage(reqHash, currentState.getValues(), reqOps, recursionDepth);

		if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + "("+id+")" + " send request to "+agent+": " + reqHash +"("+ currentRequest.hashCode()+"), waiting for: " + currentRequest.waitingFor());

		DataAccumulator.getAccumulator().heuristicRequestMessages ++;
		DataAccumulator.getAccumulator().totalBytes += req.getBytes();

		if(otherProtocol != null && agent.equals(comm.getAddress())){
			otherProtocol.receiveHeuristicRequestMessage(req, comm.getAddress());
		}else{
			protocol.sendHeuristicRequestMessage(req, agent);
		}
		
	}

	@Override
	public void processMessages() {
		if(!initialized){
			LOGGER.warn("NOT INITIALIZED YET!");
		}

		while(currentRequest.waitingFor() == 0 && localRequests.size() > 0){
			LocalHeuristicRequest lr = localRequests.pollLast();
//			LOGGER.info(comm.getAddress() + "("+id+") localRequests: " + localRequests.size());
			getHeuristic(lr.state, lr.callback);
		}
	}

	private void processReply(HeuristicReplyWithPublicActionsMessage re,String sender) {
		if(re.getRequestHash() != currentRequest.hashCode()){
    		LOGGER.warn(comm.getAddress() + "("+id+")" + " received reply to: " + re.getRequestHash() + ", but currently computing: " + currentRequest.hashCode());

    	}
		
//		Trace.it(comm.getAddress(),".processReply:",sender,re.getRequestHash(),re.heuristicValue);
		
		Map<String,List<Integer>> requests = new HashMap<>();
		
		for(int i : re.usedPublicActionIDs){
			Action a = problem.getAction(i);
			if(a.isProjection()){
				if(!a.getOwner().equals(sender) && !requestedActions.contains(i)){
					//someone else's
					if(!requests.containsKey(a.getOwner()))requests.put(a.getOwner(), new LinkedList<Integer>());
					requests.get(a.getOwner()).add(a.hashCode());
				}
			}else{
//				//my public action
//				if(!rp.contains(a.hashCode()) && !requestedActions.contains(i)){
//					if(!requests.containsKey(a.getOwner()))requests.put(a.getOwner(), new LinkedList<Integer>());
//					requests.get(a.getOwner()).add(a.hashCode());
//				}
			}
		}
		
		for(String agent : requests.keySet()){
			sendRequest(agent,currentRequest.hashCode(), requests.get(agent),1);
		}

		currentRequest.receiveReply(re.heuristicValue,null);

	}

	private void processRequest(HeuristicRequestMessage req, String sender) {
		
//		Trace.it(comm.getAddress(),".processRequest:",req.getRequestHash());
		
		RelaxedPlan locrp = new RelaxedPlan();
		
		State reqState = req.getState(domain);
		
		for(int a : req.getRequestedValues()){
			
//			Trace.it(" ",comm.getAddress(),".processRequest(action):",req.getRequestHash(),problem.getAction(a).getLabel());

			
				SuperState privatePre = new SuperState(problem.getAction(a).getPrecondition());
				for(int var : privatePre.getSetVariableNames().toArray()){
					if(domain.isPublicVar(var) && domain.isPublicVal(privatePre.getValue(var))){
						privatePre.forceSetValue(var, Domain.UNDEFINED);
					}
				}
				
			RelaxedPlan rp = getLocalRP(reqState,privatePre,locrp);
			
			if(rp == null){
				locrp=null;
				break;
			}
			
			locrp.addAll(rp.toArray());
			
		}
		
//		for(int id : rp.toArray()){
//			Trace.it("	",comm.getAddress(),"rp:",id,problem.getAction(id).getLabel());
//		}
//		
//		Trace.it("	",comm.getAddress(),"already requested actions:",req.getRequestHash(),requestedActions.size());
//		
//		for(int id : requestedActions.toArray()){
//			Trace.it("	",comm.getAddress(),"already requested:",id,problem.getAction(id).getLabel());
//		}
		
		RelaxedPlan pubrp = new RelaxedPlan();
		int localCost = 1;

		if(locrp == null){
			localCost = LARGE_HEURISTIC;
		}else{
			for(int i : locrp.toArray()){
				if(!problem.getAction(i).getOwner().equals(sender)){
					if(problem.getAction(i).isProjection()){
						pubrp.add(i);
					}else if (!requestedActions.contains(i)){
						++localCost;
					}
				}
			}
		}
		
//		Trace.it(comm.getAddress(),".processRequest(localCost):",req.getRequestHash(),localCost);
		
//		LOGGER.info(comm.getAddress() + " local cost: " + localCost);

		// send reply
		HeuristicReplyWithPublicActionsMessage re = new HeuristicReplyWithPublicActionsMessage(req.getRequestHash(), req.getState(domain).hashCode(), 0,localCost, pubrp.toArray(),req.getRecursionDepth());


		if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + "("+id+")" + " send reply to "+sender+": " + req.getRequestHash());

		DataAccumulator.getAccumulator().heuristicReplyMessages ++;
		DataAccumulator.getAccumulator().totalBytes += re.getBytes();

		if(otherProtocol != null && sender.equals(comm.getAddress())){
			otherProtocol.receiveHeuristicReplyWithPublicActionsMessage(re, comm.getAddress());
		}else{
			protocol.sendHeuristicReplyWithPublicActionsMessage(re, sender);
		}

	}

	

}
