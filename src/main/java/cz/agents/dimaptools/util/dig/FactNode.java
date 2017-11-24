package cz.agents.dimaptools.util.dig;

import java.util.HashSet;
import java.util.Set;

import cz.agents.dimaptools.model.Problem;

public class FactNode implements Comparable<FactNode>{
	
	final String varval;
	final Set<String> agents = new HashSet<>();
	final int var;
	final int val;
	
	boolean isPublic;
	
	Set<ActionNode> achievers = new HashSet<>();
	Set<ActionNode> preOf = new HashSet<>();
	ActionNode minAchiever = null;
	
	int cost = Integer.MAX_VALUE;

	public FactNode(int var, int val, Problem p) {
		super();
		this.var = var;
		this.val = val;
		
		agents.add(p.agent);
		varval = p.getDomain().humanizeVar(var) + "-" + p.getDomain().humanizeVal(val);
		isPublic = p.getDomain().isPublicVar(var) && p.getDomain().isPublicVal(val);
	}
	
//	public String getNodes(){
//		String s = "";
//		for(String agent : agents){
//			s += "  \"" + agent + ":" + varval + "\" [label=\"" + varval + "\"];\n";
//		}
//		return s;
//	}
	
	public String getNode(String agent){
		return "  \"" + /*agent + ":" +*/ varval + ":" + cost + "\" [label=\"" + varval + ":" + cost + "\"];\n";
		
	}
	
	public String getEdges(){
		String s = "";
		
		for(ActionNode a : achievers){
			s += " \"" + /*a.agent + ":" +*/ varval + ":" + cost + "\" -> \"" + a.label + ":" + a.cost + "\";\n"; 
		}
		
		return s;
	}
	
	public String getEdges(Set<ActionNode> actionNodes){
		String s = "";
		
		for(ActionNode a : achievers){
			if(actionNodes.contains(a)){
				s += " \"" + /*a.agent + ":" +*/ varval + ":" + cost + "\" -> \"" + a.label + ":" + a.cost + "\";\n";
			}
		}
		
		return s;
	}
	
	public String toString(){
		return varval;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + val;
		result = prime * result + var;
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
		FactNode other = (FactNode) obj;
		if (val != other.val)
			return false;
		if (var != other.var)
			return false;
		return true;
	}

	@Override
	public int compareTo(FactNode o) {
		return cost - o.cost;
	}
	
	

	
}
