package cz.agents.dimaptools.heuristic.relaxed;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.ActionCostMessage;
import cz.agents.dimaptools.communication.protocol.SynchronizedDistributedHeuristicProtocol;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.DistributedFFEvaluator;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.State;

public class SynchronizedDistributedRelaxationHeuristic extends RelaxationHeuristic{
	
	private static final Logger LOGGER = Logger.getLogger(SynchronizedDistributedRelaxationHeuristic.class);

	private final SynchronizedDistributedHeuristicProtocol protocol;
	private HeuristicComputedCallback currentCallback = null;
	
	private final String agent;
	
	public SynchronizedDistributedRelaxationHeuristic(DIMAPWorldInterface world, EvaluatorInterface evaluator) {
		super(world.getProblem(), evaluator);
		
		agent = world.getAgentName();
		
		if(evaluator instanceof DistributedFFEvaluator){
			((DistributedFFEvaluator) evaluator).setAvailableOperators(operators);
		}

		protocol = new SynchronizedDistributedHeuristicProtocol(world.getCommunicator(), world.getAgentName(), world.getEncoder()) {
			
			@Override
			public void process(ActionCostMessage msg, String sender) {
				
//				LOGGER.info(agent + " received: " + msg);
				
				for(UnaryOperator op : operators){
					if(op.actionHash == msg.getActionHash()){
						
						Proposition p = op.effect;
						
						if(p.cost == -1 || p.cost > msg.getHeuristicValue()){
							p.cost = msg.getHeuristicValue();
							p.distance = msg.getHeuristicValue();
							p.reachedBy = op;
							explorationQueue.add(p);
						}
					}
				}
				
				finishExplorationQueue();
				
			}
		};
	}
	
	
	@Override
	public void enqueueIfNecessary(Proposition p, int cost, UnaryOperator op){
		if(op!=null){
			if(problem.getAction(op.actionHash).isProjection()){
				return; //ignore projections
			}else if(problem.getAction(op.actionHash).isPublic()){
				sendAction(problem.getAction(op.actionHash),cost);
			}
		}
		
		if(p.cost == -1 || p.cost > cost){
			p.cost = cost;
			p.distance = cost;
			p.reachedBy = op;
			explorationQueue.add(p);
		}
	}


	private void sendAction(Action action, int cost) {
		ActionCostMessage msg = new ActionCostMessage(currentState.hashCode(),action.hashCode(),cost);
//		LOGGER.info(agent + " send: " + msg);
		protocol.sendActionCostMessage(msg);
	}


	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {
		
		buildGoalPropositions(problem.goalSuperState);
		setupExplorationQueue();
		setupExplorationQueueState(state);
		relaxedExploration();
		
		int cost = evaluator.getTotalCost(goalPropositions); 
		
		if(cost < HeuristicInterface.LARGE_HEURISTIC){
			callback.heuristicComputed(new HeuristicResult(cost,evaluator.getHelpfulActions(state)));
		}else{
			currentCallback = callback;
		}
		
		

		
	}
	
	public void finishExplorationQueue(){
        /*
         * Check this! We need to add propositions which were skipped, but also update costs
         * of propositions which were added, but can now be achieved another way
         */

//        int unsolvedGoals = 0;
//        for(Proposition g : goalPropositions){
//            if(g.cost == -1)++unsolvedGoals;
//        }

        //continue with the exploration
        while(!explorationQueue.isEmpty()){
            Proposition p = explorationQueue.poll();

            if(p.cost < p.distance) continue;

//            if(p.isGoal && --unsolvedGoals <= 0){ //cheaper but incomplete test
//                boolean all_goals = true;
//                for(Proposition g : goalPropositions){ //complete test
//                    if(g.cost == -1){
//                        all_goals = false;
//                        ++unsolvedGoals;
//                    }
//                }
//                if(all_goals)break;
//            }

            //compute cost
            evaluator.evaluateOperators(p.preconditionOf, p.cost);

            //trigger operators
            for(UnaryOperator op : p.preconditionOf){
                --op.unsatisfied_preconditions;
                if(op.unsatisfied_preconditions <= 0){
                    enqueueIfNecessary(op.effect,op.cost,op);
                }
            }


        }

//		LOGGER.info(domain.agent + "("+id+")" + "finish eq done, pending requests:"+requests.size());
        int totalCost = evaluator.getTotalCost(goalPropositions);

        if(totalCost < HeuristicInterface.LARGE_HEURISTIC){
        	HelpfulActions ha = evaluator.getHelpfulActions((State)currentState);
        	if(currentCallback!=null){
        		currentCallback.heuristicComputed(new HeuristicResult(totalCost,ha));
        	}
        	currentCallback = null;
        }
    }

}


