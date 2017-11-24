package cz.agents.dimaptools.heuristic;

import cz.agents.dimaptools.model.State;

/**
 * Generic interface of a (distributed) state-space heuristic
 * @author stolba
 *
 */
public interface HeuristicInterface {

	/**
	 * Unknown/large heuristic = infinity
	 */
	public static final int LARGE_HEURISTIC = 1000000;

	/**
	 * Get the heuristic estimate for given state. The estimate is returned via callback (may be asynchronous)
	 * @param state
	 * @param callback
	 */
	public void getHeuristic(State state, HeuristicComputedCallback callback);

	/**
	 * Process communication
	 */
	public void processMessages();


	public interface HeuristicComputedCallback{

		public void heuristicComputed(HeuristicResult result);

	}

}
