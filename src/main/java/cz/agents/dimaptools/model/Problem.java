package cz.agents.dimaptools.model;

import gnu.trove.TIntObjectHashMap;

import java.util.HashSet;
import java.util.Set;


public class Problem {

	public final String agent;
	public final State initState;
    public final SuperState goalSuperState;
    private final Set<Action> myActions;
    private final Set<Action> allActions;
    private final Set<Action> projectedActions;
    private final Set<Action> publicActions;

    private final TIntObjectHashMap allActionMap = new TIntObjectHashMap();



	public Problem(String agent, State initState, SuperState goalSuperState,Set<Action> myActions, Set<Action> allActions, Set<Action> publicActions) {
		super();
		this.agent = agent;
		this.initState = initState;
		this.goalSuperState = goalSuperState;
		this.myActions = myActions;
		this.allActions = allActions;
		this.publicActions = publicActions;
		
		projectedActions = new HashSet<Action>(allActions);
		projectedActions.removeAll(myActions);

		for(Action a : allActions){
			allActionMap.put(a.hashCode(), a);
		}
	}


	/**
	 * Get only actions owned by the agent.
	 * @return
	 */
	public Set<Action> getMyActions() {
		return myActions;
	}
	
	public Set<Action> getMyPrivateActions() {
		Set<Action> privateA = new HashSet<Action>(myActions);
		privateA.removeAll(publicActions);
		return privateA;
	}
	
	public Set<Action> getMyPublicActions() {
		Set<Action> publicA = new HashSet<Action>(myActions);
		publicA.retainAll(publicActions);
		return publicA;
	}


	/**
	 * Get actions owned by the agent and projections of public actions owned by other agents
	 * @return
	 */
	public Set<Action> getAllActions() {
		return allActions;
	}
	
	/**
	 * Get projections of all public actions owned by other agents.
	 * @return
	 */
	public Set<Action> getProjectedActions() {
		return projectedActions;
	}
	
	/**
	 * Get all pure projections of public actions owned by other agents.
	 * @return
	 */
	public Set<Action> getPureProjectedActions() {
		Set<Action> ppa = new HashSet<Action>();
		for(Action a : projectedActions){
			if(a.isPure())ppa.add(a);
		}
		return ppa;
	}

	/**
	 * Get all public actions and projections
	 * @return
	 */
	public Set<Action> getPublicActions() {
		return publicActions;
	}


	public Action getAction(int hash){
		return (Action)allActionMap.get(hash);
	}


	public Domain getDomain(){
		return initState.getDomain();
	}

}
