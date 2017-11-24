package cz.agents.dimaptools.heuristic.relaxed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class SubmissiveRelaxationHeuristic extends RelaxationHeuristic implements HeuristicInterface  {

	
	
	public SubmissiveRelaxationHeuristic(Problem problem,
			EvaluatorInterface evaluator, boolean sortedPublicOperatorsUpfront) {
		super(problem, evaluator, sortedPublicOperatorsUpfront);
	}
	
	public SubmissiveRelaxationHeuristic(Problem problem,
			EvaluatorInterface evaluator) {
		super(problem, evaluator);
	}





	private InterruptInterface interrupt = null;

	


	
	
	public void setInterrupt(InterruptInterface interrupt){
		this.interrupt = interrupt;
	}

	
	@Override
	public void relaxedExploration(){
//		LOGGER.info(domain.agent + debugPrint());
		
//		for(Proposition p : goalPropositions){
//			LOGGER.info(domain.agent + p.var+":"+problem.getDomain().humanizeVar(p.var) + ", " + p.val+":"+problem.getDomain().humanizeVal(p.val));
//			LOGGER.info(domain.agent + "... reached by " + (p.reachedBy==null?"null":problem.getAction(p.reachedBy.actionHash)));
//		}

		int unsolvedGoals = goalPropositions.size();
		while(!explorationQueue.isEmpty()){
			Proposition p = explorationQueue.poll();
//			LOGGER.info(domain.agent + " POLL " + p);

			if(p.cost < p.distance) continue;

			//if p is goal decrease the counter
			if(p.isGoal && --unsolvedGoals <= 0){
				//if counter is 0, check if all goals reached (some goal may have been counted more than once)
				boolean all_goals = true;
				for(Proposition g : goalPropositions){
					if(g.cost == -1){
						all_goals = false;
					}
				}
				if(all_goals)return;	//if all goals reached, finish
			}

			//increase cost of operator
			evaluator.evaluateOperators(p.preconditionOf, p.cost);

			//trigger operators
			for(UnaryOperator op : p.preconditionOf){
				--op.unsatisfied_preconditions;
				if(op.unsatisfied_preconditions == 0){
					enqueueIfNecessary(op.effect,op.cost,op);
				}
			}
			
			if(interrupt!= null){
				interrupt.interruptComputation();
			}
		}

//		LOGGER.warn(domain.agent + "exploration queue empty!");

	}

	
	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {
		
		buildGoalPropositions(problem.goalSuperState);
		setupExplorationQueue();
		setupExplorationQueueState(state);
		
		if(interrupt!= null){
			interrupt.interruptComputation();
		}
		
		relaxedExploration();

		callback.heuristicComputed(new HeuristicResult(evaluator.getTotalCost(goalPropositions),evaluator.getHelpfulActions(state)));
	}


	
	
	
	public interface InterruptInterface{
		
		public void interruptComputation();
		
	}

}
