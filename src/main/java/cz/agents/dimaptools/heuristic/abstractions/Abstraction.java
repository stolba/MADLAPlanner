package cz.agents.dimaptools.heuristic.abstractions;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


public class Abstraction {
	
	private int maxStateID = 0;
	private int maxEdgeID = 0;

	private Map<Integer,AbstractState> states = new HashMap<>();
	private Map<Integer,AbstractEdge> edges = new HashMap<>();
	
	private AbstractState initState = null;
	private Set<AbstractState> goalStates = new HashSet<>();
	
	public void addState(AbstractState s){
		if(maxStateID<s.hashCode())maxStateID = s.hashCode()+1;
		
		states.put(s.hashCode(), s);
		if(s.isInitState()){
			if(initState != null)initState.setInitState(false);
			initState = s;
		}
		if(s.isGoalState()) goalStates.add(s);
	}
	
	public AbstractState getState(int hash){
		return states.get(hash);
	}
	
	public void removeState(int hash){
		AbstractState s = states.get(hash);
		states.remove(hash);
		if(s != null){
			for(AbstractEdge e : s.getInEdges()){
				edges.remove(e.hashCode());
			}
			for(AbstractEdge e : s.getOutEdges()){
				edges.remove(e.hashCode());
			}
		}
	}
	
	
	
	public AbstractState getInit(){
		return initState;
	}
	
	public Collection<AbstractState> getAllStates(){
		return states.values();
	}
	
	public Set<AbstractState> getGoalStates(){
		return goalStates;
	}
	
	public void addEdge(AbstractEdge e){
		boolean newEdge = false;
		if(e.getFrom()!=null){
			boolean added = e.getFrom().addOutEdge(e);
			if(added)newEdge=true;
		}
		if(e.getTo()!=null){
			boolean added = e.getTo().addInEdge(e);
			if(added)newEdge=true;
		}
		if(newEdge){
			edges.put(e.hashCode(), e);
		}
	}
	
	public AbstractEdge getEdge(int hash){
		return edges.get(hash);
	}
	
	public Collection<AbstractEdge> getAllEdges(){
		return edges.values();
	}
	
	public int newStateID(){
		return maxStateID++;
	}
	
	public int newEdgeID(){
		return maxEdgeID++;
	}
	
	public AbstractState shrink(Set<AbstractState> toShrink){
		if(toShrink.size()==1){
			return toShrink.iterator().next();
		}
		
//		String lbl = "[";
		AbstractState shrinkedState = new AbstractState(newStateID());
		
		boolean goal = false;
		boolean init = false;
//		boolean comma = false;
		for(AbstractState s : toShrink){
//			if(comma)lbl += ",";
//			comma=true;
//			lbl += s.getLabel();
			
			shrinkedState.addOutEdges(new HashSet<AbstractEdge>(s.getOutEdges()));
			shrinkedState.addInEdges(new HashSet<AbstractEdge>(s.getInEdges()));
			if(s.isGoalState())goal = true;
			if(s.isInitState())init = true;
			removeState(s.hashCode());
		}
		
		shrinkedState.setGoalState(goal);
		shrinkedState.setInitState(init);
//		shrinkedState.setLabel(lbl + "]");
		states.put(shrinkedState.hashCode(), shrinkedState);
		return shrinkedState;
	}
	
	public static Abstraction  merge(Abstraction abs1, Abstraction abs2){
		Abstraction abs3 = new Abstraction();
		
		Map<Integer,Set<AbstractState>> mergedFromAbs1 = new HashMap<>();
		Map<Integer,Set<AbstractState>> mergedFromAbs2 = new HashMap<>();
		
		for(AbstractState s1 : abs1.getAllStates()){
			for(AbstractState s2 : abs2.getAllStates()){
				AbstractState s = new AbstractState(abs3.newStateID());
				if(!mergedFromAbs1.containsKey(s1.hashCode())) mergedFromAbs1.put(s1.hashCode(), new LinkedHashSet<AbstractState>());
				mergedFromAbs1.get(s1.hashCode()).add(s);
				
				if(!mergedFromAbs2.containsKey(s2.hashCode())) mergedFromAbs2.put(s2.hashCode(), new LinkedHashSet<AbstractState>());
				mergedFromAbs2.get(s2.hashCode()).add(s);
				
//				s.setLabel("("+s1.getLabel()+"x"+s2.getLabel()+")");
				s.setInitState(s1.isInitState() && s2.isInitState());
				s.setGoalState(s1.isGoalState() && s2.isGoalState());
				
				abs3.addState(s);
			}
		}
		
		for(AbstractEdge e1 : abs1.getAllEdges()){
			for(AbstractEdge e2 : abs2.getAllEdges()){
				
				Set<Integer> newLabels = new HashSet<Integer>(e1.getLabels());
				newLabels.retainAll(e2.getLabels());
				if(newLabels.isEmpty())continue;
				
				
				Set<AbstractState> from = new HashSet<AbstractState>( mergedFromAbs1.get(e1.getFrom().hashCode()));
				from.retainAll(mergedFromAbs2.get(e2.getFrom().hashCode()));
				if(from.isEmpty())continue;
				
				Set<AbstractState> to = new HashSet<AbstractState>( mergedFromAbs1.get(e1.getTo().hashCode()));
				to.retainAll(mergedFromAbs2.get(e2.getTo().hashCode()));
				if(to.isEmpty())continue;
				
				for(AbstractState t : to){
					for(AbstractState f : from){
						AbstractEdge e = new AbstractEdge(abs3.newEdgeID(),Math.min(e1.getWeight(),e2.getWeight()));
						e.addLabels(newLabels);
						e.setFrom(f);
						e.setTo(t);
						abs3.addEdge(e);
					}
				}
				
				
			}
		}
		
		return abs3;
	}
	
	public void resetSearchStats(){
		for(AbstractState s : states.values()){
			s.resetSearchStats();
		}
	}
	
	@Override
	public String toString() {
		String st = "Abstraction:\n";
		if(initState != null){
			st += " States (init="+initState.hashCode()+"):\n";
		}else{
			st += " States (init="+initState+"!!!):\n";
		}
		for(AbstractState s : states.values()){
			st += "  " + s + "\n";
		}
		st += " Edges:\n";
		for(AbstractEdge e : edges.values()){
			st += "  " + e+ "\n";
		}
		return st;
	}
}
