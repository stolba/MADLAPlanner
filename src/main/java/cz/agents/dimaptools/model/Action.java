package cz.agents.dimaptools.model;



public class Action implements Comparable<Action> {

	private final String name;
    private final String label;
    private final String agentOwner;	//owner

    private final SuperState precondition;
    private final SuperState effect;

    private final boolean isPublic;
    private final boolean isProjection;
    private final boolean isPure;

    private final int hash;
    
    private float cost = 1;

    public Action(String name, String label, String agentName, SuperState precondition, SuperState effect, boolean isPublic) {
    	this(name, label, agentName, precondition, effect, isPublic, false);
    }
    
    public Action(String name, String label, String agentName, SuperState precondition, SuperState effect, boolean isPublic, boolean isProjection) {
    	this(name, label, agentName, precondition, effect, isPublic,isProjection, false);
    }

    public Action(String name, String label, String agentName, SuperState precondition, SuperState effect, boolean isPublic, boolean isProjection, boolean isPure) {
    	this.name = name;
    	this.label = label;
        this.agentOwner = agentName;
        this.precondition = precondition;
        this.effect = effect;
        this.isPublic = isPublic;
        this.isProjection = isProjection;
        this.isPure = isPure;

        String agentAndLabel = agentName + ": " + label;
		hash = agentAndLabel.hashCode();
    }

    public Action(Action action) {
        this(action.name,action.label, action.agentOwner, action.precondition, action.effect, action.isPublic);
        this.setCost(action.cost);
    }

    public boolean isApplicableIn(SuperState state) {
        return state.unifiesWith(precondition);
    }
    
    public boolean isPubliclyApplicableIn(SuperState state) {
        return state.unifiesPubliclyWith(precondition);
    }

    /**
     * Applies effects of the action on a given state
     * @param state
     */
    public void transform(SuperState state) {
        state.setAllValuesBy(effect);
    }

    public String getLabel() {
        return isProjection ? "*"+label+"("+agentOwner+")" : label;//[" + isPure + "]
    }
    
    public String getSimpleLabel() {
        return label;//[" + isPure + "]
    }

    /**
     * ID of agent owning the action
     * @return
     */
    public String getOwner() {
        return agentOwner;
    }

    public boolean isPublic() {
        return isPublic;
    }

    /**
     * TRUE if action is a projection of action owned by another agent
     * @return
     */
    public boolean isProjection() {
        return isProjection;
    }
    
    /**
     * TRUE if action is a projection, but the original action had no private preconditions
     * @return
     */
    public boolean isPure() {
        return isProjection && isPure;
    }

    public SuperState getEffect(){
        return effect;
    }

    public SuperState getPrecondition(){
        return precondition;
    }

    @Override
    public String toString() {
        return "Action [label=" + getLabel()
        		+ ", cost=" + cost
        		+ ", precondition=" + precondition
        		+ ", effect=" + effect
        		+ ", isPublic=" + isPublic
        		+", owner=" + agentOwner
        		+ "] - "+this.hashCode()+"\n";
    }
    
    public String printToPlan() {
    	String params = label.replace(name+" ", " ");
    	//params = params.replace("-", " ");
		return "("+name+params+")";
	}

    @Override
	public int compareTo(Action o) {
		return label.compareTo(o.label);
	}


    @Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Action other = (Action) obj;
		if (agentOwner == null) {
			if (other.agentOwner != null)
				return false;
		} else if (!agentOwner.equals(other.agentOwner))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}



	public int getBytes() {
		return 2 + 4 + 4 + precondition.getBytes() + effect.getBytes();
	}

	public float getCost() {
		return cost;
	}
	
	public void setCost(float newCost){
		cost = newCost;
	}

	





}
