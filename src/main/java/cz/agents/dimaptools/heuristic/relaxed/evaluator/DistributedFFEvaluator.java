package cz.agents.dimaptools.heuristic.relaxed.evaluator;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.CommunicationPerformer;
import cz.agents.alite.communication.channel.CommunicationChannelBroadcast;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.ActionCostMessage;
import cz.agents.dimaptools.communication.message.ReconstructRPMessage;
import cz.agents.dimaptools.communication.protocol.DistributedRPExtractionProtocol;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.HelpfulActions;
import cz.agents.dimaptools.heuristic.relaxed.Proposition;
import cz.agents.dimaptools.heuristic.relaxed.RelaxedPlan;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

public class DistributedFFEvaluator implements EvaluatorInterface {

	private final Logger LOGGER = Logger.getLogger(DistributedFFEvaluator.class);
	
	private final CommunicationPerformer performer;
	
	private final String agent;
	private final int id;
	private final Problem problem;
	private RelaxedPlan finalRP = null;
	private HelpfulActions ha = new HelpfulActions();
	
	List<UnaryOperator> operators = new LinkedList<UnaryOperator>();
	DistributedRPExtractionProtocol protocol;
	
	private boolean finished = false;
	private boolean computing = false;
	
	int depth =0;
	

	public DistributedFFEvaluator(DIMAPWorldInterface world) {
		super();
		this.problem = world.getProblem();
		performer = world.getCommPerformer();
		agent = world.getAgentName();
		id = world.getAgentID();
		
		protocol = new DistributedRPExtractionProtocol(world.getCommunicator(),world.getAgentName(),world.getEncoder()) {
			
			

			@Override
			public void process(ReconstructRPMessage msg,String sender) {
				
				if(finalRP != null)return;
				
				if(msg.getActionHash() == -1){
					finalRP = msg.getRelaxedPlan();
					finished = true;
//					LOGGER.info(agent + " received FINISH");
					
					return;
				}
				
				RelaxedPlan rp = msg.getRelaxedPlan();
				
				int msgs = 0;
				
				for(UnaryOperator op : operators){
					if(op.actionHash == msg.getActionHash()){
						
//						LOGGER.info(agent + " received " + problem.getAction(op.actionHash));
						
						for(Proposition p : op.precondition){
							msgs += mark(p,rp);
						}
						rp.add(op.actionHash);
					}
				}
				
				if(msgs==0){
					finish(rp);
				}
				
			}
		};
	}
	
	public void setAvailableOperators(List<UnaryOperator> operators){
		this.operators = operators;
	}

	@Override
	public void evaluateOperators(List<UnaryOperator> operators, int proposition_cost) {
		for(UnaryOperator op : operators){
			op.cost = Math.max(proposition_cost,op.cost);
		}
	}
	
	public int mark(Proposition goal, RelaxedPlan rp){
		int sentMsgs = 0;
		
		if(!goal.marked){
			goal.marked = true;
			
//			LOGGER.info(agent + " mark " + problem.getDomain().humanizeVar(goal.var) + "-" + problem.getDomain().humanizeVal(goal.val));
			
			UnaryOperator op = goal.reachedBy;
			if(op != null){
				if(problem.getAction(op.actionHash).isProjection()){
					rp.add(op.actionHash);
					protocol.sendRPMessage(new ReconstructRPMessage(op.actionHash, rp),problem.getAction(op.actionHash).getOwner());
					++sentMsgs;
				}else{
					for(Proposition p : op.precondition){
						sentMsgs += mark(p,rp);
					}
					rp.add(op.actionHash);
					
					if(op.cost==op.baseCost && !problem.getAction(op.actionHash).isProjection()){
						// We have no 0-cost operators and axioms to worry
	                    // about, so it implies applicability.
						ha.add(op.actionHash);
					}
				}
			}
		}
		
		return sentMsgs;
	}
	
	private void finish(RelaxedPlan rp){
		//finish
		finished = true;
		computing = false;
		finalRP = rp;
		
//		LOGGER.info(agent + " send FINISH");
		protocol.sendRPMessage(new ReconstructRPMessage(-1, rp),CommunicationChannelBroadcast.BROADCAST_ADDRESS);
	}
	
	@Override
	public HelpfulActions getHelpfulActions(State state){
		return ha;
	}


	@Override
	public int getTotalCost(List<Proposition> goalPropositions) {
		
		
		
		for(Proposition p : goalPropositions){
			if(p.cost == -1){
//				LOGGER.info(problem.agent + " h_FF: DEAD_END");
				return HeuristicInterface.LARGE_HEURISTIC;
			}
		}
		
//		if(!finished){
//			finalRP = null;
//		}
		
//		LOGGER.info(agent + " getTotalCost("+computing+")");
		
		if(finalRP!=null){
//			LOGGER.info(agent + " done");
			
			return finalRP.getCost();
		}
		
		//HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!
		//otherwise we get a stack-overflow
		++depth;
		if(depth > 10){
			return HeuristicInterface.LARGE_HEURISTIC;
		}
		//HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!HACK!
		
		if(id==0){
			if(!computing){
			
				finished = false;
		
				RelaxedPlan rp = new RelaxedPlan();
				ha = new HelpfulActions();
				
				int msgs = 0;
				
				for(Proposition p : goalPropositions){
					msgs += mark(p,rp);
				}
				
				computing = true;
				
				if(msgs==0){
					finish(rp);
				}
				
			}
		}else{
			finished = false;
			computing = true;
			
		}
		
		//this is actually a problem, there should be callback instead and this should be outside
		while(computing && !finished){
			performer.performReceiveNonblock();	
		}
		
		
		
		
		
//		LOGGER.info(agent + " done");
		
		return finalRP.getCost();

	}
	
	public RelaxedPlan getRelaxedPlan(){
		return finalRP;
	}

}
