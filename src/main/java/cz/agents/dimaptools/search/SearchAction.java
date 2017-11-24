package cz.agents.dimaptools.search;

import cz.agents.dimaptools.model.Action;

public class SearchAction extends Action {

    public final ParentState parentState;

    public SearchAction(Action action, SearchState parentState) {
        super(action);

        this.parentState = new ParentState(parentState.getParentAction(),parentState.getGF(),parentState.getHeuristicF(),parentState.hashCode());
    }

   

    public float getPrice() {
        return super.getCost();
    }



	public ParentState getParentState() {
		return parentState;
	}

    

}
