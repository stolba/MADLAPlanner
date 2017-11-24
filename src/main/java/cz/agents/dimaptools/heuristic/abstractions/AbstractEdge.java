package cz.agents.dimaptools.heuristic.abstractions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AbstractEdge{
	private final int hash;
	private final Set<Integer> labels = new HashSet<>();
	private float weight;
	
	private AbstractState from;
	private AbstractState to;
	public AbstractEdge(int hash, float weight) {
		super();
		this.hash = hash;
		this.weight = weight;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public AbstractState getFrom() {
		return from;
	}
	public void setFrom(AbstractState from) {
		this.from = from;
	}
	public AbstractState getTo() {
		return to;
	}
	public void setTo(AbstractState to) {
		this.to = to;
	}
	
//	public void addLabel(String label){
//		labels.add(label);
//	}
	
	public void addLabel(int label){
		labels.add(label);
	}
	
	public void addLabels(Collection<Integer> newLabels){
		labels.addAll(newLabels);
	}
	
	public Set<Integer> getLabels(){
		return labels;
	}
	
	
	
	@Override
	public String toString() {
		return "AbstractEdge [hash=" + hash + ", from=" + from.hashCode() + ", to=" + to.hashCode() + ", w=" + weight + ", labels=" + labels
				+ "]";
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
		AbstractEdge other = (AbstractEdge) obj;
		if (hash != other.hash)
			return false;
		return true;
	}

}