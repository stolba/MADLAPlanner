package cz.agents.dimaptools.heuristic.relaxed.evaluator;

import java.util.List;

import cz.agents.dimaptools.heuristic.relaxed.HelpfulActions;
import cz.agents.dimaptools.heuristic.relaxed.Proposition;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.model.State;

public interface EvaluatorInterface {

    public void evaluateOperators(List<UnaryOperator> operators, int propositionCost);

    public int getTotalCost(List<Proposition> goalPropositions);

    public HelpfulActions getHelpfulActions(State state);

}
