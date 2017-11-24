package cz.agents.dimaptools.search;

import java.util.List;

public class ParentState {
	
	public final float g;
	public final float h;
	public final ParentAction parentAction;
	public final int hash;

	public ParentState(ParentAction parentAction, float g, float h,int hash) {
		this.parentAction = parentAction;
		this.h = h;
		this.g = g;
		this.hash = hash;
	}

	public ParentState reconstructPlan(List<String> plan) {
		if (parentAction == null || parentAction.parentState == null) {
            return this;
        } else {
            return parentAction.reconstructPlan(plan);
        }
	}

	public String getParentActionOwner() {
		if(parentAction == null){
			return null;
		}else{
			return parentAction.owner;
		}
	}
	
	public int hashCode(){
		return hash;
	}

}
