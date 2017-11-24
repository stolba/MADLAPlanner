package cz.agents.dimaptools.heuristic.relaxed.evaluator;

import java.util.List;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.HelpfulActions;
import cz.agents.dimaptools.heuristic.relaxed.Proposition;
import cz.agents.dimaptools.heuristic.relaxed.RelaxedPlan;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

public class FFEvaluator implements EvaluatorInterface {

	private final Logger LOGGER = Logger.getLogger(FFEvaluator.class);
	
	private final Problem problem;
	private RelaxedPlan rp;
	private HelpfulActions ha = new HelpfulActions();
	

	public FFEvaluator(Problem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public void evaluateOperators(List<UnaryOperator> operators, int proposition_cost) {
		for(UnaryOperator op : operators){
			op.cost = Math.max(proposition_cost,op.cost);
		}
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
				
				if(op.cost==op.baseCost && !problem.getAction(op.actionHash).isProjection()){
					// We have no 0-cost operators and axioms to worry
                    // about, so it implies applicability.
					ha.add(op.actionHash);
				}
			}
		}
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

		rp = new RelaxedPlan();
		ha = new HelpfulActions();
		
		for(Proposition p : goalPropositions){
			mark(p);
		}
		
//		System.out.println(problem.agent + "(FFeval): " + rp.humanize(problem) + "("+rp.getCost()+")");
		
		
		
		return rp.getCost();

	}
	
	public RelaxedPlan getRelaxedPlan(){
		return rp;
	}

}
