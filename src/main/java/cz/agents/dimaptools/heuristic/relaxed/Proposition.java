package cz.agents.dimaptools.heuristic.relaxed;

import java.util.LinkedList;
import java.util.List;

public class Proposition implements Comparable<Proposition>{

	public final int id;

	public final int var;//debug
	public final int val;//debug

	public boolean isGoal;
	public int cost;
	public int distance;
	public boolean marked;
	public boolean markedPub;

	public List<UnaryOperator> preconditionOf = new LinkedList<UnaryOperator>();
	public UnaryOperator reachedBy;

	



	public Proposition(int id, int var, int val) {
		super();
		this.id = id;
		this.var = var;
		this.val = val;
	}



	@Override
	public int compareTo(Proposition o) {
		return distance - o.distance;
	}




	@Override
	public int hashCode() {
		return id;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Proposition other = (Proposition) obj;
		if (id != other.id)
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "Proposition [var=" + var + ", val=" + val + "]:"+cost+"/"+distance;
	}


}
