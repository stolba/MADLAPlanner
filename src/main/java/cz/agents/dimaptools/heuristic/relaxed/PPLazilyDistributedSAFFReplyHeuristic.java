package cz.agents.dimaptools.heuristic.relaxed;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.heuristic.DistributedReplyHeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class PPLazilyDistributedSAFFReplyHeuristic extends RelaxationHeuristic implements DistributedReplyHeuristicInterface {

	private final Logger LOGGER;


	private DistributedHeuristicRequestProtocol requestProtocol = null;
	private final DistributedHeuristicReplyProtocol replyProtocol;
	private final Communicator comm;
	
    private RelaxedPlan rp = new RelaxedPlan();
    RelaxationHeuristicRequest currentRequest;
    
    private Map<String,RelaxedPlan> localRelaxedPlans = new HashMap<>();


	public PPLazilyDistributedSAFFReplyHeuristic(DIMAPWorldInterface world) {
		super(world.getProblem(),new FFEvaluator(world.getProblem()),true);

		this.comm = world.getCommunicator();
		
		replyProtocol = new DistributedHeuristicReplyProtocol(
                world.getCommunicator(),
                world.getAgentName(),
                world.getEncoder()){

            @Override
            public void receiveHeuristicRequestMessage(HeuristicRequestMessage req, String sender) {
                if(LOGGER.isDebugEnabled())LOGGER.debug(replyProtocol.getAddress() +  " handle request from " + sender + ": " + req.humanize(problem.getDomain()));

                processRequest(req,sender);
            }

        };

		LOGGER = Logger.getLogger(problem.agent + "." + PPLazilyDistributedSAFFReplyHeuristic.class);
	}
	
	public PPLazilyDistributedSAFFReplyHeuristic(DIMAPWorldInterface world,DistributedHeuristicRequestProtocol requestProtocol) {
        this(world);
        this.requestProtocol = requestProtocol;
    }

    public DistributedHeuristicReplyProtocol getReplyProtocol(){
        return replyProtocol;
    }

    public void setRequestProtocol(DistributedHeuristicRequestProtocol requestProtocol){
        this.requestProtocol = requestProtocol;
    }


    @Override
    public void getHeuristic(State state, HeuristicComputedCallback callback) {
        //XXX: exception?
        LOGGER.warn("This method should not be called on the Reply heuristic");
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




	

	public RelaxedPlan getRP(String sender, int hash) {

//		if(LOGGER.isDebugEnabled())LOGGER.debug(domain.agent + " get local RP: s:" + domain.humanize(state.getValues()) + ", goal: " + domain.humanize(goal.getValues()));
		
		//caching
		if(localRelaxedPlans.containsKey(sender)){
			if(localRelaxedPlans.get(sender).getHash() == hash){
				rp = localRelaxedPlans.get(sender);
			}else{
				rp = new RelaxedPlan(hash);
				localRelaxedPlans.put(sender,rp);
			}
		}else{
			rp = new RelaxedPlan(hash);
			localRelaxedPlans.put(sender,rp);
		}
		
		
		
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


	

	@Override
	public void processMessages() {
		if(!initialized){
			LOGGER.warn("NOT INITIALIZED YET!");
		}

	}

private void processRequest(HeuristicRequestMessage req, String sender) {
		
//		Trace.it(comm.getAddress(),".processRequest:",req.getRequestHash());
		
		
		
		State reqState = req.getState(domain);
		
		clearGoalPropositions();
		
		for(int a : req.getRequestedValues()){
			
			SuperState privatePre = new SuperState(problem.getAction(a).getPrecondition());
			for(int var : privatePre.getSetVariableNames().toArray()){
				if(domain.isPublicVar(var) && domain.isPublicVal(privatePre.getValue(var))){
					privatePre.forceSetValue(var, Domain.UNDEFINED);
				}
			}
//			System.out.println(privatePre.getNumberOfSetValues());
			
			//TODO: effects of the actions should be removed
			
			addGoalPropositions(privatePre);
		}
		
		
		setupExplorationQueue();
		setupExplorationQueueState(reqState);
		
		
		
		RelaxedPlan locrp = getRP(sender,req.getRequestHash());
		int localCost = 0;
		
		
		if(locrp == null){
			localCost = LARGE_HEURISTIC;
		}else{
			locrp.removeAll(req.getRequestedValues());
			RelaxedPlan pubrp = new RelaxedPlan();
			
			for(int a : locrp.toArray()){
				Action action = problem.getAction(a);
				if(action.isPublic()){
					pubrp.add(a);
				}else{
					localCost += 1; //action.getCost();
				}
			}
			
			locrp = pubrp;
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
		
		

		
		
//		Trace.it(comm.getAddress(),".processRequest(localCost):",req.getRequestHash(),localCost);
		
//		LOGGER.info(comm.getAddress() + " local cost: " + localCost);

		// send reply
		HeuristicReplyWithPublicActionsMessage re = new HeuristicReplyWithPublicActionsMessage(req.getRequestHash(), req.getState(domain).hashCode(), 0,localCost, locrp!=null?locrp.toArray():new int[0],req.getRecursionDepth());


		if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() +" send reply to "+sender+": " + req.getRequestHash());

		DataAccumulator.getAccumulator().heuristicReplyMessages ++;
		DataAccumulator.getAccumulator().totalBytes += re.getBytes();

		if(requestProtocol != null && sender.equals(comm.getAddress())){
			requestProtocol.receiveHeuristicReplyWithPublicActionsMessage(re, comm.getAddress());
		}else{
			replyProtocol.sendHeuristicReplyWithPublicActionsMessage(re, sender);
		}

	}

	

}
