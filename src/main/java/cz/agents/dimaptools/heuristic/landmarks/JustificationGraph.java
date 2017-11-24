package cz.agents.dimaptools.heuristic.landmarks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.relaxed.Proposition;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.model.SuperState;

public class JustificationGraph {
	

	private final Logger LOGGER = Logger.getLogger(JustificationGraph.class);
	
	Map<Integer,Map<Integer,JGNode>> nodes = new HashMap<>();
	Set<JGEdge> edges = new HashSet<>();
	
	JGNode i = new JGNode("i");
	JGNode g = new JGNode("g");
	
	public JustificationGraph(ArrayList<UnaryOperator> operators, Map<Integer,Map<Integer,Proposition>> propositions,SuperState currentState,ArrayList<Proposition> goalPropositions){
		
		
		
		for(UnaryOperator op : operators){
			
//			LOGGER.info("add operator("+op.actionHash+"): " + op);
			
			if(op.effect == null) continue;
			
			JGNode pre;
			
			if(!op.precondition.isEmpty()){
				Proposition maxPre = null;
				for(Proposition p : op.precondition){
					if(maxPre == null){
						maxPre = p;
					}else{
						maxPre = p.cost > maxPre.cost ? p : maxPre;
					}
				}
				
				pre = getNode(maxPre);
			}else{
				pre = i;
			}
			
			JGNode eff = getNode(op.effect);
			
			addEdge(pre, eff, op);
		}
		
		for(int var: currentState.getSetVariableNames().toArray()){
			JGNode init = getNode(var, currentState.getValue(var));
			boolean add = true;
			for(JGEdge e : i.out){
				if(e.to.equals(init)){
					add = false;
					break;
				}
			}
			if(add){
				addEdge(i, init, new UnaryOperator(-1, -1, null, 0, false));
			}
		}
		
		for(Proposition p : goalPropositions){
			JGNode goal = getNode(p);
			boolean add = true;
			for(JGEdge e : goal.out){
				if(e.to.equals(g)){
					add = false;
					break;
				}
			}
			if(add){
				addEdge(goal, g, new UnaryOperator(-1, -1, null, 0, false));
			}
		}
		
		g.zeroReachableFromGoal = true;
	}
	

	private JGNode getNode(Proposition p) {
		return getNode(p.var, p.val);
	}
	
	
	private JGNode getNode(int var, int val) {
		JGNode n;
		if(nodes.containsKey(var)){
			if(nodes.get(var).containsKey(val)){
				return nodes.get(var).get(val);
			}else{
				n = new JGNode(var,val);
				nodes.get(var).put(val,n);
				return n;
			}
		}else{
			n = new JGNode(var,val);
			nodes.put(var,new HashMap<Integer,JGNode>());
			nodes.get(var).put(val,n);
			return n;
		}
		
	}
	
	private void addEdge(JGNode pre, JGNode eff, UnaryOperator op){
		boolean newEdge = true;
		for(JGEdge e : pre.out){
			if(e.to.equals(eff)){
				e.labels.add(op);
				if(op.baseCost == 0) e.zeroCost = true;
				newEdge = false;
				break;
			}
		}
		if(newEdge){
			JGEdge e = new JGEdge();
			e.from = pre;
			e.to = eff;
			e.labels.add(op);
			if(op.baseCost == 0) e.zeroCost = true;
			pre.out.add(e);
			eff.in.add(e);
			edges.add(e);
		}
	}
	
	public Set<UnaryOperator> computeCut(){
		Set<UnaryOperator> cut = new HashSet<UnaryOperator>();
		
		//search backward from goal and mark
		LinkedList<JGNode> queue = new LinkedList<>();
		Set<JGNode> closed = new HashSet<>();
		queue.add(g);
		
		while(!queue.isEmpty()){
			JGNode n = queue.poll();
			n.zeroReachableFromGoal = true;
			closed.add(n);
			
			for(JGEdge e : n.in){
				if(e.zeroCost && !closed.contains(e.from)){
					queue.add(e.from);
				}
			}
		}
		
		//search forward and find cut operators
		queue = new LinkedList<JGNode>();
		closed = new HashSet<>();
		queue.add(i);
		
		while(!queue.isEmpty()){
			JGNode n = queue.poll();
			closed.add(n);
			
			for(JGEdge e : n.out){
				if(e.to.zeroReachableFromGoal){
//					LOGGER.info(" ...cut edge: "+ e);
					cut.addAll(e.labels);
				}else{
					if(!closed.contains(e.to)){
						queue.add(e.to);
					}
				}
			}
		}
		
		return cut;
	}
	
	


	@Override
	public String toString() {
		String str = "JustificationGraph [i=" + i + ", g=" + g + "]:\n";
		for(JGEdge e : edges){
			str += e + "\n";
		}
		return str;
	}




	private class JGNode{
		String label;
		List<JGEdge> in = new LinkedList<>();
		List<JGEdge> out = new LinkedList<>();
		boolean zeroReachableFromGoal = false;
		
		public JGNode(int var, int val) {
			label=var+"-"+val;
		}
		
		public JGNode(String lbl) {
			label=lbl;
		}

		@Override
		public String toString() {
			return label+"("+zeroReachableFromGoal+")";
		}
		
		
		
	}
	
	private class JGEdge{
		JGNode from;
		JGNode to;
		boolean zeroCost = false;
		Set<UnaryOperator> labels = new HashSet<>();
		
		@Override
		public String toString() {
			return from+"->"+to+":"+zeroCost+","+labels;
		}
		
		
	}

}
