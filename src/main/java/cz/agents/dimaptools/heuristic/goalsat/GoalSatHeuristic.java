package cz.agents.dimaptools.heuristic.goalsat;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

/**
 * Simple heuristic, where h = number of unsatisfied goals
 * @author stolba
 *
 */
public class GoalSatHeuristic implements HeuristicInterface {

	private final SuperState goal;

	public GoalSatHeuristic(SuperState goal) {
		super();
		this.goal = goal;
	}

	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {
		int unsat = 0;
		int[] vals = goal.getValues();

		for(int var = 0; var < vals.length; ++var){
			if(state.isSet(var) && goal.isSet(var)){
				if(vals[var] != state.getValue(var)){
					++unsat;
				}
			}
		}
		callback.heuristicComputed(new HeuristicResult(unsat));
	}

	@Override
	public void processMessages() {

	}

}
