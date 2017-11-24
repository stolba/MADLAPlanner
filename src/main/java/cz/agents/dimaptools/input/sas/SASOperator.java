package cz.agents.dimaptools.input.sas;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Representation of a SAS+ operator/action as parsed from the SAS file
 * @author stolba
 *
 */
public class SASOperator {

	public final String name;
	public final String label;
	public final int cost;
	public final Map<String, String> pre;
	public final Map<String, String> eff;
	private Set<SASFact> facts;

	public boolean isPublic = false;


	public SASOperator(String name,String label, Map<String, String> pre,
			Map<String, String> eff, int cost) {
		super();
		this.name = name;
		this.label = label;
		this.pre = pre;
		this.eff = eff;
		this.cost = cost;
	}

	public Set<SASFact> getFacts(){
		if(facts == null){
			facts = new HashSet<SASFact>();

			for(Entry<String,String> e : pre.entrySet()){
				facts.add(new SASFact(e.getKey(), e.getValue()));
			}

			for(Entry<String,String> e : eff.entrySet()){
				facts.add(new SASFact(e.getKey(), e.getValue()));
			}
		}
		return facts;
	}

	public boolean containsFact(SASFact f){
		return facts.contains(f);
	}

	public boolean containsVar(String var){
		return pre.containsKey(var) || eff.containsKey(var);
	}

	public boolean containsVarVal(String var, String val){
		return (pre.containsKey(var) && pre.get(var).equals(val)) || (eff.containsKey(var) && eff.get(var).equals(val));
	}


	public boolean interactsWith(SASOperator op, String var){
		if(containsVar(var) && op.containsVar(var)){
			if(pre.containsKey(var) && op.pre.containsKey(var) && pre.get(var).equals(op.pre.get(var))){
				return true;
			}
			if(pre.containsKey(var) && op.eff.containsKey(var) && pre.get(var).equals(op.eff.get(var))){
				return true;
			}
			if(eff.containsKey(var) && op.pre.containsKey(var) && eff.get(var).equals(op.pre.get(var))){
				return true;
			}
			if(eff.containsKey(var) && op.eff.containsKey(var) && eff.get(var).equals(op.eff.get(var))){
				return true;
			}
		}
		return false;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SASOperator other = (SASOperator) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "\nSASOperator [label=" + label + ", pre=" + pre + ", eff=" + eff + "pub="+ isPublic+ "]";
	}




}
