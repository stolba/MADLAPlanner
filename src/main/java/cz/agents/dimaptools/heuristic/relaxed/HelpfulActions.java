package cz.agents.dimaptools.heuristic.relaxed;

import java.util.Arrays;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import gnu.trove.TIntHashSet;

public class HelpfulActions extends TIntHashSet {

	private static final long serialVersionUID = 2147896966914938009L;

	public HelpfulActions(){
		super();
	}

	public String humanize(Problem prob){
		String ret = "Helpful={";
		for(int ai : this.toArray()){
			Action a = prob.getAction(ai);
			ret += a == null ? "\n  UNKNOWN" : "\n  " + a.getLabel();
		}
		return ret + "\n}";
	}

	@Override
	public String toString() {
		return "HelpfulActions [" + Arrays.toString(toArray()) + "]";
	}




}
