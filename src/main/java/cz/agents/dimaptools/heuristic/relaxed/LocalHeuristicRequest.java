package cz.agents.dimaptools.heuristic.relaxed;

import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.search.SearchState;

public class LocalHeuristicRequest implements Comparable<LocalHeuristicRequest> {

	public final SearchState state;
	public final HeuristicComputedCallback callback;

	public LocalHeuristicRequest(State state, HeuristicComputedCallback callback) {
		super();
		this.state = (SearchState)state;	//TODO: no no no!
		this.callback = callback;
	}

	@Override
	public int compareTo(LocalHeuristicRequest o) {
		return state.compareTo(o.state);
	}



}
