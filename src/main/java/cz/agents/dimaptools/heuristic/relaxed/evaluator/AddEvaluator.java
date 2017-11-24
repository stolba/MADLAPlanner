package cz.agents.dimaptools.heuristic.relaxed.evaluator;

import java.util.List;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.HelpfulActions;
import cz.agents.dimaptools.heuristic.relaxed.Proposition;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

public class AddEvaluator implements EvaluatorInterface {

//	private final Logger LOGGER = Logger.getLogger(AddEvaluator.class);

    private final boolean provideHelpfulActions;
    private final Problem problem;
    private List<Proposition> goalPropositions;

    public AddEvaluator(Problem problem){
        this(problem,false);
    }

    public AddEvaluator(Problem problem, boolean provideHelpfulActions) {
        super();
        this.provideHelpfulActions = provideHelpfulActions;
        this.problem = problem;
    }

    @Override
    public void evaluateOperators(List<UnaryOperator> operators, int propositionCost) {
        for(UnaryOperator op : operators){
            op.cost += propositionCost;
        }

    }

    //use this to provide helpful actions:
    public void mark(Proposition goal, HelpfulActions ha){
        //TODO: why is sometimes goal market in the first run?
        //XXX: if this is really happening, it could be some issuse of reusing of the Propositions, which is scary
        if(!goal.marked){
            goal.marked = true;
            UnaryOperator op = goal.reachedBy;
            if(op != null){
                for(Proposition p : op.precondition){
                    mark(p,ha);
                }
//				LOGGER.info("COMPARE op.cost = " + op.cost + " op.base_cost.cost = " + op.base_cost + " goal.cost = " + goal.cost + " goal.distance = " + goal.distance);
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
        if(provideHelpfulActions){
            HelpfulActions ha = new HelpfulActions();
            for(Proposition g : goalPropositions){
                mark(g, ha);
            }

            return ha;
        }else{
            return null;
        }
    }



    @Override
    public int getTotalCost(List<Proposition> goalPropositions) {
        this.goalPropositions = goalPropositions;
        int total_cost = 0;
        for(Proposition p : goalPropositions){
            if(p.cost == -1){
//				LOGGER.info(problem.agent + " h_add: DEAD_END");
                return HeuristicInterface.LARGE_HEURISTIC;
            }
            total_cost += p.cost;
        }
        return total_cost;
    }

}
