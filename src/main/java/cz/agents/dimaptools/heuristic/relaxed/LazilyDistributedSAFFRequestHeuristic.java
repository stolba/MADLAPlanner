package cz.agents.dimaptools.heuristic.relaxed;

import gnu.trove.TIntHashSet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.heuristic.DistributedRequestHeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class LazilyDistributedSAFFRequestHeuristic extends RelaxationHeuristic implements DistributedRequestHeuristicInterface {

	private final Logger LOGGER;

	private final int agentID;

	private final DistributedHeuristicRequestProtocol requestProtocol;
	private DistributedHeuristicReplyProtocol replyProtocol=null;
	private final Communicator comm;
	
    private RelaxedPlan rp = new RelaxedPlan();
    RelaxedPlanRequest currentRequest;

    private LinkedList<LocalHeuristicRequest> localRequests = new LinkedList<LocalHeuristicRequest>();
    private TIntHashSet requestedActions = new TIntHashSet();

    private final int maxRecursionDepth;

    public LazilyDistributedSAFFRequestHeuristic(DIMAPWorldInterface world) {
    	this(world,  Integer.MAX_VALUE);
    }

	public LazilyDistributedSAFFRequestHeuristic(DIMAPWorldInterface world,int maxRecursionDepth) {
		super(world.getProblem(),new FFEvaluator(world.getProblem()),true);

		this.comm = world.getCommunicator();
		agentID = world.getAgentID();
		this.maxRecursionDepth = maxRecursionDepth > -1 ? maxRecursionDepth : Integer.MAX_VALUE;
		
		currentRequest = new RelaxedPlanRequest(agentID,problem,null);

		requestProtocol = new DistributedHeuristicRequestProtocol(
                world.getCommunicator(),
                world.getAgentName(),
                world.getEncoder()){

            @Override
            public void receiveHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re, String sender) {
                if(LOGGER.isDebugEnabled())LOGGER.debug(requestProtocol.getAddress() + " handle reply from " + sender + ": " + re);
                processReply(re,sender);
            }

        };

		LOGGER = Logger.getLogger(problem.agent + "." + LazilyDistributedSAFFRequestHeuristic.class);
	}
	
	@Override
	public DistributedHeuristicRequestProtocol getRequestProtocol(){
        return requestProtocol;
    }

	@Override
    public void setReplyProtocol(DistributedHeuristicReplyProtocol replyProtocol){
        this.replyProtocol = replyProtocol;
    }
    
    @Override
	public boolean isComputing() {
		// TODO Auto-generated method stub
		return false;
	}
    
    @Override
    public boolean hasWaitingLocalRequests(){
    	return localRequests.size() > 0;
    }
    
    
    public RelaxedPlan getRelaxedPlan(){
    	return currentRequest.getRP();
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
				LOGGER.error(comm.getAddress() + " starting heuristic from scratch, but still waiting for: "+currentRequest.waitingFor());
		}
		
		
		currentRequest = new RelaxedPlanRequest(agentID,problem,callback);
		requestedActions.clear();
		
//		Trace.it("	",comm.getAddress(),"already requested actions(clear):",currentRequest.hashCode(),requestedActions.size());
		
//		Trace.it(comm.getAddress(),".getHeuristic:",currentRequest.hashCode(),state.hashCode(),goal.hashCode());
		

		getRP(state,goal);
		
		currentRequest.waitForReply();
		
		
		
		Map<String,List<Integer>> requests = new HashMap<>();
		
		for(int i : rp.toArray()){
			Action a = problem.getAction(i);
			if(a.isProjection()){
				if(!requestedActions.contains(a.hashCode())){
					if(!requests.containsKey(a.getOwner()))requests.put(a.getOwner(), new LinkedList<Integer>());
					requests.get(a.getOwner()).add(a.hashCode());
				}
			}
		}
		
//		Trace.it(comm.getAddress(),".getHeuristic:",currentRequest.hashCode());
//		Trace.it(comm.getAddress(),".getHeuristic(state):",state);
//		for(int id : rp.toArray()){
//			Trace.it("	",comm.getAddress(),"rp:",id,problem.getAction(id).getLabel());
//		}
		
//		Trace.it("	",comm.getAddress(),"already requested actions:",currentRequest.hashCode(),requestedActions.size());
		
		
		for(String agent : requests.keySet()){
			sendRequest(agent,currentRequest.hashCode(), requests.get(agent),1);
		}
		
		currentRequest.receiveReply(rp, null);



	}

	public RelaxedPlan getRP(State state, SuperState goal) {

		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " get local RP: s:" + domain.humanize(state.getValues()) + ", goal: " + domain.humanize(goal.getValues()));

		rp = new RelaxedPlan();
		
		buildGoalPropositions(goal);
		setupExplorationQueue();
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


	protected void sendRequest(String agent, int reqHash, List<Integer> actionIDs, int recursionDepth) {
		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " send request: " + actionIDs);

		

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

		if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + " send request to "+agent+": " + reqHash +"("+ currentRequest.hashCode()+"), waiting for: " + currentRequest.waitingFor());

		DataAccumulator.getAccumulator().heuristicRequestMessages ++;
		DataAccumulator.getAccumulator().totalBytes += req.getBytes();

		if(replyProtocol != null && agent.equals(comm.getAddress())){
			replyProtocol.receiveHeuristicRequestMessage(req, comm.getAddress());
		}else{
			requestProtocol.sendHeuristicRequestMessage(req, agent);
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
    		LOGGER.warn(comm.getAddress() + " received reply to: " + re.getRequestHash() + ", but currently computing: " + currentRequest.hashCode());

    	}
		
//		Trace.it(comm.getAddress(),".processReply:",sender,re.getRequestHash(),re.heuristicValue);
		
		if(re.heuristicValue==LARGE_HEURISTIC){
			currentRequest.receiveReply(re.usedPublicActionIDs,null);
			return;
		}
		
		Map<String,List<Integer>> requests = new HashMap<>();
		
		for(int i : re.usedPublicActionIDs){
			Action a = problem.getAction(i);
			
			if(a==null)continue;	//unknown action
			
			if(a.isProjection()){
				if(!a.getOwner().equals(sender) && !requestedActions.contains(i)){
					//someone else's
					if(!requests.containsKey(a.getOwner()))requests.put(a.getOwner(), new LinkedList<Integer>());
					requests.get(a.getOwner()).add(a.hashCode());
				}
			}else{
				//my public action
				if(!currentRequest.getRP().contains(a.hashCode()) && !requestedActions.contains(i)){
					if(!requests.containsKey(a.getOwner()))requests.put(a.getOwner(), new LinkedList<Integer>());
					requests.get(a.getOwner()).add(a.hashCode());
				}
			}
		}
		
		for(String agent : requests.keySet()){
			sendRequest(agent,currentRequest.hashCode(), requests.get(agent),1);
		}
		
//		for(int id : re.usedPublicActionIDs){
//			Trace.it("	",comm.getAddress(),"rp:",id,problem.getAction(id)!=null?problem.getAction(id).getLabel():"UNKNOWN");
//		}

		//WARNING: we are redefining the message semantics here: usedPublicActionIDs is the whole relaxed plan computed by the other agent
		
		currentRequest.receiveReply(re.usedPublicActionIDs,null);

	}

	

	

	

}
