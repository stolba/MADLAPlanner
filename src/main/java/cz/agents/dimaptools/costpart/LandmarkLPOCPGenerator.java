package cz.agents.dimaptools.costpart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.landmarks.LMCutHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class LandmarkLPOCPGenerator implements CostPartitioningGeneratorInterface {
	
	private final LPSolver solver;
	private LPSolution sol;
	String LP = "";
	Set<String> agents = new HashSet<>();
	Set<String> landmarks = new HashSet<>();
	Map<String,Integer> actions = new HashMap<>();
	Map<String,Set<String>> actionInLandmarks = new HashMap<>();
	String stateConstraints = "";
	String bounds = "";
	boolean free = false;
	int lms = 0;
	
	
	private final static Logger LOGGER = Logger.getLogger(LandmarkLPOCPGenerator.class);
	
	

	public LandmarkLPOCPGenerator(LPSolver solver, boolean free) {
		super();
		this.solver = solver;
		this.free = free;
	}

	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
		
		LMCutHeuristic lmcut = new LMCutHeuristic(prob);
		lmcut.getHeuristic(prob.initState, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				// TODO Auto-generated method stub
				
			}
		});
		List<Set<UnaryOperator>> landmarks = lmcut.getLandmarks();
		
		for(Set<UnaryOperator> lm : landmarks){
			String lmString = getLandmarkString();
			for(UnaryOperator op : lm){
				String act = getActionString(op, agent);
				if(!actionInLandmarks.containsKey(act)){
					actionInLandmarks.put(act, new HashSet<String>());
				}
				actionInLandmarks.get(act).add(lmString);
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
				
				LOGGER.info("...updating("+a.hashCode()+"): "+getActionString(a,problem.agent));
				
				float oldCost = a.getCost();
				
				float newCost;
				
				if(sol.hasVariable(getActionString(a,problem.agent))){
					newCost = sol.getVariableValue(getActionString(a,problem.agent));
				}else{
					newCost = oldCost / agents.size();
				}
				
				a.setCost(newCost);
				LOGGER.info("UPDATE("+a.hashCode()+"): "+oldCost+" -> "+newCost + " ("+a+")");
			}
		}
	}
	
	private String getActionString(UnaryOperator op, String agent){
		String l =  Integer.toString(op.actionHash);
		l="a" +l.replaceAll("-", "m");
		actions.put(l,op.baseCost);
		return l+"_"+agent;
	}
	
	private String getActionString(Action a, String agent){
		String l =  Integer.toString(a.hashCode());
		l="a" +l.replaceAll("-", "m");
		return l+"_"+agent;
	}
	
	private String getLandmarkString(){
		++lms;
		String lmStr = "L"+lms;
		landmarks.add(lmStr);
		return lmStr;
	}
	
	

	
	
	public String generateCostPartitioningLP(){
		//header
		LP += "\\\\LP for landmark-based cost-partitioning\n\n";
				
		//objective function
		LP += "Maximize\n obj: ";
		
		boolean plus = false;
		for(String lm : landmarks){
			if(plus) LP += " + ";
			LP += lm;
			plus = true;
		}
		
		//constraints
		LP +="\nSubject To\n";
		
		for(String act : actionInLandmarks.keySet()){
			plus = false;
			for(String lm : actionInLandmarks.get(act)){
				if(plus) LP += " + ";
				plus = true;
				LP +=  lm + " ";
				
				if(free){
					bounds += lm + " >= -10000\n";
				}else{
					bounds += lm + " >= 0\n";
				}
			}
			LP+=" - " + act +" <= 0\n";
		}
		
		
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
