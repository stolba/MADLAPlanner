package cz.agents.dimaptools.costpart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.abstractions.AbstractEdge;
import cz.agents.dimaptools.heuristic.abstractions.AbstractState;
import cz.agents.dimaptools.heuristic.abstractions.MergeAndShrink;
import cz.agents.dimaptools.heuristic.abstractions.MergeAndShrink.ShrinkStrategy;
import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.search.SearchState;

public class AbstractionLPOCPGenerator implements CostPartitioningGeneratorInterface {
	
	private final LPSolver solver;
	private LPSolution sol;
	String LP = "";
	Set<String> agents = new HashSet<>();
	Map<String,Float> actions = new HashMap<>();
	String stateConstraints = "";
	String bounds = "";
	boolean free = false;
	int stateLimit = 100;
	
	
	private final static Logger LOGGER = Logger.getLogger(AbstractionLPOCPGenerator.class);
	
	

	public AbstractionLPOCPGenerator(LPSolver solver, int stateLimit, boolean free) {
		super();
		this.solver = solver;
		this.free = free;
	}

	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
		
		MergeAndShrink mas = new MergeAndShrink(prob,stateLimit);
		mas.mergeAndShrink(ShrinkStrategy.EQUAL_G);
		
		/*
		 * In an ideal world, this (mas.getAbstraction().getInit() == null) should not happen, but alas, 
		 * we live in a world which is far from ideal and as 
		 * we might have a public variable with private values such 
		 * evil things happen. There is probably no correct way to handle it.
		 */
		if(mas.getAbstraction().getInit() != null){ 
			stateConstraints +=  getStateString(mas.getAbstraction().getInit(), agent) + " = 0\n";
		}
		
		for(AbstractState s : mas.getAbstraction().getAllStates()){
			if(free){
				bounds += getStateString(s,agent) + " >= -10000\n";
			}else{
				bounds += getStateString(s,agent) + " >= 0\n";
			}
			if(s.isGoalState()){
				stateConstraints +=  getHeuristicString(agent) + " - " + getStateString(s,agent) + " <= 0\n";
			}
		}
		
		for(AbstractEdge e : mas.getAbstraction().getAllEdges()){
			for(int l : e.getLabels()){
				String act =  getActionString(prob.getAction(l),agent);
				stateConstraints += getStateString(e.getTo(),agent) + " - " + getStateString(e.getFrom(),agent) + " - " + act + " <= 0\n";
			}
		}
				
		
	}
	
	public void solveOCPLP(){

		LP = generateCostPartitioningLP();
		LOGGER.info(LP);
		sol = solver.solveLP(LP);
		
	}
	
	public LPSolution getSolution(){
		return sol;
	}

	@Override
	public void updateCosts(Problem problem) {
		for(Action a : problem.getAllActions()){
			if(a.isPublic()){
				
				float sumCost = 0;
				for(String ag : agents){
					sumCost += sol.getVariableValue(getActionString(a,ag));
				}
				
				float altCost = 0; //oldCost/agents.size()
//				float altCost = a.getCost()/agents.size();
				if(a.isProjection()){
					altCost = a.getCost()/((float)agents.size()-1f);
				}else{
					altCost = 0;
				}
				
				float oldCost = a.getCost();
				float newCost = sumCost==0 ? altCost : sol.getVariableValue(getActionString(a,problem.agent));
				a.setCost(newCost);
				LOGGER.info("UPDATE("+a.hashCode()+"): "+oldCost+" -> "+newCost + " ("+a+")");
			}
		}
	}
	
	private String getActionString(Action a, String agent){
		String l =  Integer.toString(a.hashCode());
		l="a" +l.replaceAll("-", "m");
		actions.put(l,a.getCost());
		return l+"_"+agent;
	}
	
	private String getHeuristicString(String agentName){
		return "h_"+agentName;
	}
	
	private String getStateString(AbstractState s, String agent){
		String l =  s.hashCode()+"_"+agent;
		l="s" +l.replaceAll("-", "m");
		return l;
	}
	

	
	
	public String generateCostPartitioningLP(){
		//header
		LP += "\\\\LP for abstraction-based cost-partitioning\n\n";
				
		//objective function
		LP += "Maximize\n obj: ";
		
		boolean plus = false;
		for(String a : agents){
			if(plus) LP += " + ";
			LP += getHeuristicString(a);
			plus = true;
		}
		
		//constraints
		LP +="\nSubject To\n";
		LP += stateConstraints;
		
		for(String act : actions.keySet()){
			plus = false;
			for(String ag : agents){
				if(plus) LP += " + ";
				plus = true;
				LP += act + "_" + ag + " ";
				
				if(free){
					bounds += act + "_" + ag + " >= -10000\n";
				}else{
					bounds += act + "_" + ag + " >= 0\n";
				}
			}
			LP+=" <= " + actions.get(act) +"\n";
		}
		
		//bounds
		LP +="Bounds\n";
		LP+= bounds;
		
				
		//end
		LP +="End";
		
		return LP;
	}

}
