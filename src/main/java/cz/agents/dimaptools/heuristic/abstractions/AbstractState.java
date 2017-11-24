package cz.agents.dimaptools.heuristic.abstractions;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AbstractState implements Comparable<AbstractState>{
	private final int hash;
	private String label;
	private boolean goalState = false;
	private boolean initState = false;
	
	private List<AbstractEdge> out = new LinkedList<>();
	private List<AbstractEdge> in = new LinkedList<>();
	
	private Map<Integer,AbstractEdge> toMap = new HashMap<>();
	private Map<Integer,AbstractEdge> fromMap = new HashMap<>();
	
	//search
	private float g = Float.POSITIVE_INFINITY;
	private AbstractEdge predecessor = null;
	
	public AbstractState(int hash, String label) {
		super();
		this.hash = hash;
		this.label = label;
	}
	
	public AbstractState(int hash) {
		super();
		this.hash = hash;
		this.label = "?";
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean addOutEdge(AbstractEdge e){
		if(!toMap.containsKey(e.getTo().hashCode())){
			e.getFrom().out.remove(e);
			out.add(e);
			e.setFrom(this);
			toMap.put(e.getTo().hash, e);
			return true;
		}else{
//			e.getFrom().out.remove(e);
			toMap.get(e.getTo().hashCode()).addLabels(e.getLabels());
			return false;
		}
		
	}
	
	public void addOutEdges(Collection<AbstractEdge> es){
		for(AbstractEdge e : es){
			addOutEdge(e);
		}
	}
	
	public boolean addInEdge(AbstractEdge e){
		if(!fromMap.containsKey(e.getFrom().hashCode())){
			e.getTo().in.remove(e);
			in.add(e);
			e.setTo(this);
			fromMap.put(e.getFrom().hash, e);
			return true;
		}else{
//			e.getTo().in.remove(e);
			fromMap.get(e.getFrom().hashCode()).addLabels(e.getLabels());
			return false;
		}
	}
	
	public void addInEdges(Collection<AbstractEdge> es){
		for(AbstractEdge e : es){
			addInEdge(e);
		}
	}
	
	public List<AbstractEdge> getOutEdges(){
		return out;
	}
	
	public List<AbstractEdge> getInEdges(){
		return in;
	}
	
	public boolean isGoalState() {
		return goalState;
	}

	public void setGoalState(boolean goalState) {
		this.goalState = goalState;
	}
	
	public boolean isInitState() {
		return initState;
	}

	public void setInitState(boolean initState) {
		this.initState = initState;
	}
	
	public void updateSearchStats(AbstractEdge e){
		if(e==null){
			g=0;
		}else{
			g = e.getFrom().getG() + e.getWeight();
			predecessor = e;
		}
	}
	
	public void resetSearchStats(){
		g = Float.POSITIVE_INFINITY;
		predecessor = null;
	}
	
	public float getG(){
		return g;
	}
	
	public AbstractEdge getPredecessorEdge(){
		return predecessor;
	}
	
	public AbstractState getPredecessorState(){
		if(predecessor==null)return null;
		return predecessor.getFrom();
	}
	
	

	@Override
	public String toString() {
		return "AbstractState [hash=" + hash + ", label=" + label
				+ ", goal=" + goalState + ", init=" + initState + ",in="+in.size() + ",out="+out.size() + ", g="+g + "]";
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
		AbstractState other = (AbstractState) obj;
		if (hash != other.hash)
			return false;
		return true;
	}

	@Override
	public int compareTo(AbstractState arg0) {
		if(arg0.getG() == g){
			return 0;
		}else{
			if(arg0.getG() < g){
				return -1;
			}else{
				return 1;
			}
		}
	}
	
	
	
}