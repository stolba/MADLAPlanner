package cz.agents.dimaptools.costpart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.search.SearchState;

public class PublicLPOCPGenerator implements CostPartitioningGeneratorInterface {
	
	private final LPSolver solver;
	private LPSolution sol;
	String LP = "";
	Set<String> agents = new HashSet<>();
	Map<String,Float> actions = new HashMap<>();
	String stateConstraints = "";
	String bounds = "";
	boolean free = false;
	
	
	private final static Logger LOGGER = Logger.getLogger(PublicLPOCPGenerator.class);
	
	

	public PublicLPOCPGenerator(LPSolver solver, boolean free) {
		super();
		this.solver = solver;
		this.free = free;
	}

	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
		Set<SearchState> closed = new HashSet<>();
		LinkedList<SearchState> open = new LinkedList<>(); //breadth first search
		open.add(new SearchState(prob.initState));
		stateConstraints +=  getStateString(prob.initState, agent) + " = 0\n";
				
		while(!open.isEmpty()){
			SearchState s = open.pollFirst();
			if(closed.contains(s))continue;
			closed.add(s);
			String s1 = getStateString(s, agent);
			
			if(free){
				bounds += s1 + " >= -10000\n";
			}else{
				bounds += s1 + " >= 0\n";
			}
			
			for(Action a : prob.getPublicActions()){
				if(a.isPubliclyApplicableIn(s)){
					String act = getActionString(a, agent);
					
					
					SearchState newState = s.transformBy(a);
					open.addLast(newState);
					
					String s2 = getStateString(newState, agent);
					
					stateConstraints += s2 + " - " + s1 + " - " + act + " <= 0\n";
					
				}
			}
			if(s.unifiesWith(prob.goalSuperState)){
				stateConstraints +=  getHeuristicString(agent) + " - " + s1 + " <= 0\n";
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
	
	private String getStateString(State s, String agent){
		String l =  s.hashCode()+"_"+agent;
		l="s" +l.replaceAll("-", "m");
		return l;
	}
	

	
	
	public String generateCostPartitioningLP(){
		//header
		LP += "\\\\LP for public abstraction-based cost-partitioning\n\n";
				
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
