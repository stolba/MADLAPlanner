package cz.agents.dimaptools.heuristic.relaxed;

import java.util.Arrays;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import gnu.trove.TIntHashSet;

public class RelaxedPlan extends TIntHashSet {

	private static final long serialVersionUID = 23428024803968818L;

	public static final RelaxedPlan NOT_EXISTING_RELAXED_PLAN = new RelaxedPlan(false);

	private boolean exists = true;
	private int hash;

	public RelaxedPlan(){
		super();
	}
	
	public RelaxedPlan(int hash){
		super();
		this.hash = hash;
	}
	
	public int getHash(){
		return hash;
	}

	private RelaxedPlan(boolean doesNotExist){
		exists = false;
	}

	public void merge(RelaxedPlan otherRP){
		if(otherRP == null || !otherRP.exists){
			this.exists = false;
		}else{
			this.addAll(otherRP.toArray());
		}
	}

	public int getCost(){
		if(exists){
			return this.size();
		}else{
			return HeuristicInterface.LARGE_HEURISTIC;
		}
	}

	public String humanize(Problem prob){
		String ret = "RP=[";
		for(int ai : this.toArray()){
			Action a = prob.getAction(ai);
			ret += a == null ? "\n  UNKNOWN" : "\n  " + a.getLabel();
		}
		return ret + "\n]";
	}

	@Override
	public String toString() {
		return "RelaxedPlan [" + Arrays.toString(toArray()) + "]";
	}




}
