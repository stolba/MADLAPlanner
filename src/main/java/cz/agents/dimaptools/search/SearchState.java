package cz.agents.dimaptools.search;

import java.util.List;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.State;

public class SearchState extends State implements Comparable<SearchState> {

    private final static Logger LOGGER = Logger.getLogger(SearchState.class);

    private final float g;
    private float h = Float.MAX_VALUE;
    private boolean createdByPreferred = false;

    // TODO: remove, this is dangerous, as the agents among each other cannot share heuristics and simply parents of states/actions
    private final ParentAction parentAction;
    private final String parentActionOwner;
    private final boolean wasReachedByPublicAction;

    /**
     * Init
     * @param state
     */
	public SearchState(State state) {
		super(state);

		parentAction = null;
		parentActionOwner = null;
		wasReachedByPublicAction = false;

		// cost
		g = 0;
		// heuristic
		h = Integer.MAX_VALUE;
	}


    /**
     * Expanded by search action
     * @param state
     * @param parentAction
     */
    public SearchState(State state, SearchAction searchAction) {
        super(state);

        if(searchAction == null){
        	throw new IllegalArgumentException("Parent action cannot be null, use SearchState(State state) for initial state");
        }

        parentAction = new ParentAction(searchAction);
        parentActionOwner = null;
        wasReachedByPublicAction = searchAction.isPublic();

        // cost
        g = parentAction.parentState.g + searchAction.getPrice();

		//heuristic (lazy)
		h = parentAction.parentState.h;
    }


    /**
     * Received from other agent
     * @param state
     * @param parentAction
     */
    public SearchState(Domain domain, StateMessage msg, String sender) {
        super(domain,msg.getValues());

        parentAction = new ParentAction(sender);
        parentActionOwner = sender;
        wasReachedByPublicAction = false;
        createdByPreferred = msg.isPreferred();

        // cost
        this.g = msg.getG();

//		if (g > maxG) {
//			maxG = g;
//			LOGGER.info("Reached new maximal [" + parentAction.getOwner() + "] /g/: " + g + (createdByPreferred?" (P)":""));
//		}

		//heuristic (lazy)
		this.h = msg.getH();

//		if (h < minH) {
//			minH = h;
//			LOGGER.info("Reached new minimal ["
//					+ (parentAction != null ? parentAction.getOwner() : "null")
//					+ "] /h/: " + h);
//		}
    }


	public void setHeuristics(float heuristicValue) {
//		if(heuristicValue==1000000){
//			LOGGER.warn("Settig heuristic to 1000000!");
//		}
       h = heuristicValue;
    }

    public boolean wasReachedByPublicAction() {
        return wasReachedByPublicAction;
    }
    
    public boolean wasCreatedByPreferredAction(){
    	return createdByPreferred;
    }

    public boolean wasExpandedByMe(String myAgentName) {
        return parentAction != null && parentAction.label != null;
    }

    public SearchState transformBy(Action action) {
        SearchState result = new SearchState(this, new SearchAction(action, this));
        action.transform(result);
        return result;
    }
    
    public SearchState transformBy(Action action, boolean preferred) {
        SearchState result = new SearchState(this, new SearchAction(action, this));
        action.transform(result);
        result.createdByPreferred = preferred;
        return result;
    }

    

    public ParentAction getParentAction(){
        return parentAction;
    }

    public int getHeuristic(){
    	return (int)h;
    }
    
    public float getHeuristicF(){
    	return h;
    }

    @Override
    public int compareTo(SearchState o) {
    	if(g + h > o.g + o.h){
    		return 1;
    	}else if(g + h < o.g + o.h){
    		return -1;
    	}else{
    		return 0;
    	}
    }

    @Override
    public String toString() {
        return "SearchState [parentAction=" + (parentAction != null ? parentAction.label : "null" ) + ", g=" + g + ", h=" + h + "] -> " + super.toString() + "\n";
    }



    public int getBytes(){
    	return super.getBytes() + 4 + 4 + 4;
    }




	public int getG() {
		return (int)g;
	}
	
	public float getGF() {
		return g;
	}


	public String getParentActionOwner() {
		return parentActionOwner;
	}
	
	
	public ParentState reconstructPlan(List<String> plan) {
		return parentAction.reconstructPlan(plan);
	}

}
