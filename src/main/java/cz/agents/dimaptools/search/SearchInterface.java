package cz.agents.dimaptools.search;

import java.util.List;

import cz.agents.alite.configurator.ConfigurationInterface;

/**
 * State-space search interface
 * @author stolba
 *
 */
public interface SearchInterface {

	/**
	 * Do the search.
	 * @param config Algorithm-specific configuration, callback (can be asynchronous).
	 * @param planFoundCallback
	 */
	public void plan(ConfigurationInterface config, SearchCallback planFoundCallback);

	public interface SearchCallback{

		/**
		 * Once the whole plan is reconstructed this method is called by the agent reaching the initial state
		 * @param plan Complete plan with actions of all agents
		 */
		public void planFound(List<String> plan);
		
		/**
		 * Called each time the agent reconstructs part of the plan
		 * @param plan Part of the plan reconstructed by the agent - contains only the agent's actions
		 */
		public void partialPlanReconstructed(List<String> plan, String initiator, int solutionCost);

		public void planFoundByOther();

		public void planNotFound();

	}

}
