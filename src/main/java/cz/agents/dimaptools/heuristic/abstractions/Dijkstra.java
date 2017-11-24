package cz.agents.dimaptools.heuristic.abstractions;

import java.util.LinkedList;
import java.util.PriorityQueue;

public class Dijkstra {
	
	private LinkedList<AbstractEdge> path = new LinkedList<>();

	public float search(AbstractState init){
		PriorityQueue<AbstractState> open = new PriorityQueue<>();
		init.updateSearchStats(null);
		open.add(init);
		
		while(!open.isEmpty()){
			AbstractState s = open.poll();
			
			if(s.isGoalState()){
				float g = s.getG();
				while(!s.equals(init)){
					path.addFirst(s.getPredecessorEdge());
					s = s.getPredecessorState();
				}
				return g;
			}
			
			for(AbstractEdge e : s.getOutEdges()){
				float g = s.getG() + e.getWeight();
				AbstractState t = e.getTo();
				
				if(g < t.getG()){
					if(open.contains(t))open.remove(t);
					t.updateSearchStats(e);
					open.add(t);
				}
			}
		}
		
		return Float.POSITIVE_INFINITY;
	}

	public LinkedList<AbstractEdge> getShortestPath() {
		return path;
	}

}
