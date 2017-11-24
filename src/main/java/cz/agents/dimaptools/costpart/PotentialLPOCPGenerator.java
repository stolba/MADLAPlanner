package cz.agents.dimaptools.costpart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.SuperState;

public class PotentialLPOCPGenerator implements CostPartitioningGeneratorInterface {
	
	private final LPSolver solver;
	private LPSolution sol;
	String LP = "";
	Set<String> agents = new HashSet<>();
//	String objective = "";
	Set<String> objective = new HashSet<>();
	Set<String> emptyColumns = new HashSet<>();
	String goal = "";
	String actionConstraints = "";
	Set<String> maxpots = new HashSet<String>();
	Set<String> bounds = new HashSet<String>();
	Map<String,Float> actions = new HashMap<>();
	String lowerBound = " 0";
	
	
	private final static Logger LOGGER = Logger.getLogger(PotentialLPOCPGenerator.class);
	
	

	public PotentialLPOCPGenerator(LPSolver solver, boolean free) {
		super();
		this.solver = solver;
		
		if(free){
			lowerBound = " -10000";
		}
		
	}

	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
		
		//objective function
		for(int var : prob.getDomain().getVariableDomains().keySet()){
			float size = prob.getDomain().getVariableDomains().size();
			for(int val : prob.getDomain().getVariableDomains().get(var)){
				String str = getVariableString(var, val, agent);
				objective.add(Float.toString(1/size) + " " + str);	
				emptyColumns.add(str);
			}
		}
		
		//maxpot constraints and bounds
		
		for (int var : prob.initState.getSetVariableNames().toArray()) {
			int val = prob.initState.getValue(var);
			maxpots.add(getVariableString(var, val, agent) + " - "+getMaxpotString(var, agent) + " <= 0");
			bounds.add(getVariableString(var, val, agent)+" >= " + lowerBound);
			bounds.add(getMaxpotString(var, agent)+" >= " + lowerBound);
		}
		for (int var : prob.goalSuperState.getSetVariableNames().toArray()) {
			int val = prob.goalSuperState.getValue(var);
			maxpots.add(getVariableString(var, val, agent) + " - "+getMaxpotString(var, agent) + " <= 0");
			bounds.add(getVariableString(var, val, agent)+" >= " + lowerBound);
			bounds.add(getMaxpotString(var, agent)+" >= " + lowerBound);
		}
		for (Action a : prob.getAllActions()) {
			if(a.getEffect().getSetVariableNames().isEmpty())continue;
			for (int var : a.getPrecondition().getSetVariableNames().toArray()) {
				int val = a.getPrecondition().getValue(var);
				maxpots.add(getVariableString(var, val, agent) + " - "+getMaxpotString(var, agent)+ " <= 0");
				bounds.add(getVariableString(var, val, agent)+" >= " + lowerBound);
				bounds.add(getMaxpotString(var, agent)+" >= " + lowerBound);
			}
			for (int var : a.getEffect().getSetVariableNames().toArray()) {
				int val = a.getEffect().getValue(var);
				maxpots.add(getVariableString(var, val, agent) + " - "+getMaxpotString(var, agent)+ " <= 0");
				bounds.add(getVariableString(var, val, agent)+" >= " + lowerBound);
				bounds.add(getMaxpotString(var, agent)+" >= " + lowerBound);
			}
		}
		
		boolean plus;
		
		//goal constraint
		plus = false;
		for (int var = 0; var < prob.getDomain().publicVarMax; var++) {
			if (plus) {
				goal += " + ";
			} else {
				plus = true;
			}
			if (prob.goalSuperState.isSet(var)) {
				goal += getVariableString(var, prob.goalSuperState.getValue(var), agent);
			} else {
				goal += getMaxpotString(var, agent);
			}
		}
		for (int var = prob.getDomain().agentVarMin; var < prob.getDomain().agentVarMax; var++) {
			if (plus) {
				goal += " + ";
			} else {
				plus = true;
			}
			if (prob.goalSuperState.isSet(var)) {
				goal += getVariableString(var, prob.goalSuperState.getValue(var), agent);
			} else {
				goal += getMaxpotString(var, agent);
			}
		}
		goal += " <= 0\n";
		
		
		//operator constraints
		for (Action a : prob.getAllActions()) {
			SuperState pre = a.getPrecondition();
			SuperState eff = a.getEffect();

			if (!eff.getSetVariableNames().isEmpty()) {

				plus = false;
				for (int var : eff.getSetVariableNames().toArray()) {
					if (plus) {
						actionConstraints += " + ";
					} else {
						plus = true;
					}
					if (pre.isSet(var)) {
						actionConstraints += getVariableString(var, pre.getValue(var), agent) + " - " + getVariableString(var, eff.getValue(var), agent);
					} else {
						actionConstraints += getMaxpotString(var, agent) + " - " + getVariableString(var, eff.getValue(var), agent);
					}
				}

				actionConstraints += " - " + getActionString(a, agent) + " <= 0" + "\n";

			}else{
				actionConstraints += getActionString(a, agent) + " >= 0\n";
			}
		}
				
	}
	
	public void solveOCPLP(){

		LP = generateCostPartitioningLP();
//		LOGGER.info(LP);
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
	
	private String getVariableString(int var,int val, String agent){
		String str = "PV"+var+"v"+val+agent;
		emptyColumns.remove(str);
		return str;
	}
	
	private String getMaxpotString(int var, String agent){
		return "MV"+var+agent;
	}
	
	private String getActionString(Action a, String agent){
		String l =  Integer.toString(a.hashCode());
		l="a" +l.replaceAll("-", "m");
		actions.put(l,a.getCost());
		return l+"_"+agent;
	}
	
	
	
	
	

	
	
	public String generateCostPartitioningLP(){
		
		
		//header
		LP += "\\\\LP for potential-based cost-partitioning\n\n";
		LP += "\\\\EMPTY COLUMNS: " + emptyColumns + "\n\n";
				
		//objective function
		LP += "Maximize\n obj: ";
		boolean plus=false;
		for(String str : objective){
			if(plus){
				LP += " + ";
			}else{
				plus = true;
			}
			LP += str;
		}
		LP += "\n";
		
		
		//constraints
		LP +="Subject To\n";
		
		for(String m : maxpots){
			LP+=m+"\n";
		}
		
		LP += actionConstraints;
		
		LP += goal;
		
		plus = false;
		for(String act : actions.keySet()){
			plus = false;
			for(String ag : agents){
				if(plus) LP += " + ";
				plus = true;
				LP += act + "_" + ag + " ";
				
				bounds.add( act + "_" + ag + " >= " + lowerBound);
				
			}
			LP+=" <= " + actions.get(act) +"\n";
		}
		
		//empty columns
		for(String var : emptyColumns){
			LP+=var + " = 0\n";
		}
		
		//bounds
		LP +="Bounds\n";
		for(String b : bounds){
			LP+=b+"\n";
		}
				
		//end
		LP +="End";
		
		return LP;
	}
	
	
	
	

}
