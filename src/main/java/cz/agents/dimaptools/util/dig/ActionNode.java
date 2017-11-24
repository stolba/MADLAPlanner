package cz.agents.dimaptools.util.dig;

import java.util.HashSet;
import java.util.Set;

public class ActionNode {
	
	final String label;
	final String agent;
	
	Set<FactNode> pre = new HashSet<>();
	Set<FactNode> eff = new HashSet<>();
	
	int unsatPre = 0;
	int cost = 0;
	
	
	public ActionNode(String label, String agent) {
		super();
		this.label = label;
		this.agent = agent;
	}
	
	public String getNode(){
		return "  \"" + label + ":" + cost + "\" [label=\"" + label + ":" + cost + "\" shape=box];\n";
	}
	
	public String getEdges(){
		String s = "";
		
//		for(FactNode f : eff){
//			s += " \"" + agent + ":" + f.varval + "\" -> \"" + label + "\";\n"; 
//		}
		
		for(FactNode f : pre){
			s += " \"" + label + ":" + cost + "\" -> \"" + /*agent + ":" +*/ f.varval + ":" + f.cost + "\";\n"; 
		}
		
		return s;
	}
	
	public String toString(){
		return label+"["+agent+"]";
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
		ActionNode other = (ActionNode) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}
	
	
	
	

}
