package cz.agents.dimaptools.util.dig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class DetailedInteractionGraph {
	
	private final static Logger LOGGER = Logger.getLogger(DetailedInteractionGraph.class);
	
	private final Map<String,Problem> problems = new HashMap<>();
	
	
//	private Map<String,Node> nodes = new HashMap<>();
//	private List<Edge> edges = new LinkedList<>();
	
//	private Node init = new Node("init");
//	private Node goal = new Node("goal");
	
	private Map<String,FactNode> facts = new HashMap<>();
	private Map<String,ActionNode> actions = new HashMap<>();
	
	ActionNode init = new ActionNode("init","init");
	ActionNode goal = new ActionNode("goal","goal");
	
	Set<ActionNode> achievers = null;
	float agentMaxCost = 0;
	float agentAddCost = 0;
	float agentSimpleCost = 0;
	
	private FactNode getFactNode(int var, int val, Problem problem){
		FactNode f = new FactNode(var,val,problem);
		if(facts.containsKey(f.varval)){
			f = facts.get(f.varval);
			f.agents.add(problem.agent);
		}else{
			f.agents.add(problem.agent);
			facts.put(f.varval, f);
		}
		return f;
	}
	
	private ActionNode getActionNode(Action a, Problem p){
		
		String owner = a.getPrecondition().getNumberOfSetValues() == 0 ? "init" : a.getOwner();
		
		ActionNode an = new ActionNode(a.getLabel(),owner);
		if(actions.containsKey(an.label)){
			an = actions.get(an.label);
		}else{
			for(int var : a.getEffect().getSetVariableNames().toArray()){
				an.eff.add(getFactNode(var,a.getEffect().getValue(var),p));
			}
			
			for(int var : a.getPrecondition().getSetVariableNames().toArray()){
				FactNode f = getFactNode(var,a.getPrecondition().getValue(var),p);
				an.pre.add(f);
				an.unsatPre ++;
				f.preOf.add(an);
			}
			
			actions.put(an.label, an);
		}
		
		return an;
	}
	
	
	public void addAgent(String agent, Problem problem){
		problems.put(agent, problem);
		
//		for(int var : problem.initState.getSetVariableNames().toArray()){
//			FactNode f = getFactNode(var,problem.initState.getValue(var),problem);
//			init.eff.add(f);
//			f.achievers.add(init);
//		}
		
		for(int var : problem.goalSuperState.getSetVariableNames().toArray()){
			FactNode f = getFactNode(var,problem.goalSuperState.getValue(var),problem);
			if(!goal.pre.contains(f)){
				goal.pre.add(f);
				goal.unsatPre ++;
				f.preOf.add(goal);
			}
		}
		
	}
	
	public void buildGraph(){
		LinkedList<FactNode> open = new LinkedList<>();
		Set<FactNode> closed = new HashSet<>();
		
		open.addAll(goal.pre);
//		open.add(goal.pre.iterator().next());
		
		while(!open.isEmpty()){
			
			FactNode f = open.pollFirst();
			
			if(!closed.contains(f)){
				
				closed.add(f);
			
				Set<Action> potentialActions = new HashSet<>();
				
				if(f.isPublic){
					for(String agent : problems.keySet()){
						potentialActions.addAll(problems.get(agent).getMyActions());
					}
				}else{
					for(String agent : f.agents){
						potentialActions.addAll(problems.get(agent).getMyActions());
					}
				}
				
				for(Action a : potentialActions){
					if(a.getEffect().isSet(f.var) && a.getEffect().getValue(f.var)==f.val){
						ActionNode an = getActionNode(a, problems.get(a.getOwner()));
						f.achievers.add(an);
						open.addAll(an.pre);
					}
					if(a.getEffect().isSet(f.var) && a.getEffect().getValue(f.var)!=f.val){
						//TODO: add negative edge?
					}
				}
				
				for(String agent : f.agents){
					if(problems.get(agent).initState.getValue(f.var)==f.val){
						init.eff.add(f);
						f.achievers.add(init);
					}
				}
			}
		}
	}
	
	
	public int getCost(){
		PriorityQueue<FactNode> q = new PriorityQueue<>();
		Set<FactNode> closed = new HashSet<FactNode>();
		
//		LinkedList<FactNode> q = new LinkedList<>();
		
		for(FactNode f : facts.values()){
			if(init.eff.contains(f)){
				f.cost = 0;
				q.add(f);
			}else{
				f.cost = Integer.MAX_VALUE;
			}
		}
		
		for(ActionNode a : actions.values()){
			if(a.unsatPre == 0){
				for(FactNode fn : a.eff){
					fn.cost = Math.min(fn.cost, a.cost);
					fn.minAchiever = a;
					q.add(fn);
				}
			}
		}
		
		while(!q.isEmpty() && goal.unsatPre!=0){
			FactNode f = q.poll();
			
			if(!closed.contains(f)){
				
				closed.add(f);
			
				for(ActionNode a : f.preOf){ 
					a.unsatPre -= 1; 
					a.cost = Math.max(a.cost, f.cost +1);	//TODO: the cost needs to be sat according to agent interactions, max or add?
					if(a.unsatPre == 0){
						for(FactNode fn : a.eff){
							if(fn.cost > a.cost){ //min
								fn.cost = a.cost;
								if(fn.varval.equals("var4-at(package2, pgh-po)")){
									LOGGER.info("setting " + fn.varval + " to " + a.cost);
								}
								fn.minAchiever = a;
							}
							q.add(fn);
						}
					}
				}
			}
		}
		
		return goal.unsatPre == 0 ? goal.cost : -1;
	}
	
	public Set<ActionNode> getAchievers(){
		if(achievers!=null)return achievers;
		
		achievers = new HashSet<>();
		
		for(FactNode g : goal.pre){
		
			LinkedList<FactNode> open = new LinkedList<>();
			Set<ActionNode> gAchievers = new HashSet<>();
			
			open.add(g);
			
			while(!open.isEmpty()){
				FactNode f = open.pollFirst();
				
				if(f.minAchiever != null && !gAchievers.contains(f.minAchiever)){
					gAchievers.add(f.minAchiever);
					open.addAll(f.minAchiever.pre);
				}
			}
			
			agentSimpleCost = Math.max(agentSimpleCost, getAgentSimpleValue(gAchievers));
			agentMaxCost = Math.max(agentMaxCost, getAgentValue(gAchievers));
			agentAddCost = agentAddCost + getAgentValue(gAchievers);
			
			achievers.addAll(gAchievers);
		}
		
		return achievers;
	}
	
	private float getAgentValue(Set<ActionNode> actionNodes){
		Set<String> agents = new HashSet<>();
		
		for(ActionNode a : actionNodes){
			agents.add(a.agent);
		}
		
		agents.remove(init.agent);
		agents.remove(goal.agent);
		
		return 1f - 1f/(float)agents.size();
	}
	
	private float getAgentSimpleValue(Set<ActionNode> actionNodes){
		Set<String> agents = new HashSet<>();
		
		for(ActionNode a : actionNodes){
			agents.add(a.agent);
		}
		
		agents.remove(init.agent);
		agents.remove(goal.agent);
		
		return (float)agents.size();
	}
	
	public float getAgentMaxValue(){
		if(achievers!=null)getAchievers();
		return agentMaxCost;
	}
	
	public float getAgentAddValue(){
		if(achievers!=null)getAchievers();
		return agentAddCost;
	}
	
	public float getAgentAddPerGoalValue(){
		if(achievers!=null)getAchievers();
		return agentAddCost/goal.pre.size();
	}
	
	public float getAgentSimpleValue(){
		if(achievers!=null)getAchievers();
		return agentSimpleCost/problems.size();
	}
	
	public String printActionSubset(Set<ActionNode> actionNodes){
		Set<FactNode> factNodes = new HashSet<>();
		Set<String> agents = new HashSet<>();
		
		actionNodes.add(init);
		actionNodes.add(goal);
		
		String graph = "digraph DIG {\n ratio=1;\n\n";
		
		for(ActionNode a : actionNodes){
			graph += a.getEdges();
			factNodes.addAll(a.pre);
			agents.add(a.agent);
		}
		
		for(FactNode fn : factNodes){
			graph += fn.getEdges(actionNodes);
		}
		
		for(String agent : agents){
			graph += "\n subgraph \"cluster" + agent + "\" {\n   label=\""+agent+"\"\n";
			
			for(ActionNode an : actionNodes){
				if(an.agent.equals(agent)){
					graph += an.getNode();
				}
			}
			
			for(FactNode fn : factNodes){
				if(!fn.isPublic && fn.agents.contains(agent)){
					graph += fn.getNode(agent);
				}
			}
			
			graph +=" }\n"; 
		}
		
		
		graph += "}\n";
		
		return graph;	
		
	}
	
	
	
	
	
	
	

	
	public int numberOfAgents(){
		return problems.keySet().size();
	}
	
	public String toString(){
		
		actions.put("init", init);
		actions.put("goal", goal);
		
		Set<String> agents = new HashSet<>();
		agents.addAll(problems.keySet());
		agents.add("init");
		agents.add("goal");
		
		String graph = "digraph DIG {\n ratio=1;\n\n";
		
		for(ActionNode an : actions.values()){
			graph += an.getEdges();
		}
		
		for(FactNode fn : facts.values()){
			graph += fn.getEdges();
		}
		
		for(String agent : agents){
			graph += "\n subgraph \"cluster" + agent + "\" {\n   label=\""+agent+"\"\n";
			
			for(ActionNode an : actions.values()){
				if(an.agent.equals(agent)){
					graph += an.getNode();
				}
			}
			
			for(FactNode fn : facts.values()){
				if(!fn.isPublic && fn.agents.contains(agent)){
					graph += fn.getNode(agent);
				}
			}
			
			graph +=" }\n"; 
		}
		
		
		graph += "}\n";
		
		return graph;
	}

}
