package cz.agents.dimaptools.search;

import java.util.List;

public class ParentAction {
	
	final ParentState parentState;
	final String label;
	final String owner;
	final int hash;
	
	
	

	public ParentAction(SearchAction action) {
		this.parentState = action.getParentState();
		this.label = action.getLabel();
		this.owner = action.getOwner();
		hash = action.hashCode();
	}
	
	
	public ParentAction(String owner) {
		super();
		this.parentState = null;
		this.label = null;
		hash = -1;
		this.owner = owner;
	}




	public ParentState reconstructPlan(List<String> plan) {
    	plan.add(0,"\n "+owner+" "+label+" "+hash);
    	return parentState.reconstructPlan(plan);
    }

}
