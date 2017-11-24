package cz.agents.dimaptools.heuristic.seq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.potential.PotentialHeuristic;
import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.SuperState;

public class DistributedSEQCostPartLPGenerator {
	
	String LP = "";
	String objective = "";
	Set<String> bounds = new HashSet<String>();
	private final LPSolver solver;
	private final String domain;
	private final String problem;
	private LPSolution sol;
	
	private HashMap<String,HashMap<Integer,HashMap<Integer,Constraint>>> constraints = new HashMap<>();
	
	private final static Logger LOGGER = Logger.getLogger(DistributedSEQCostPartLPGenerator.class);
	

	public DistributedSEQCostPartLPGenerator(LPSolver solver,String domain, String problem) {
		
		this.solver = solver;
		this.domain = domain;
		this.problem = problem;
	}
	
	public void generateCPLP(String agent, DIMAPWorldInterface world){
		if(solver.isSolver() == LPSolver.Solver.LPSOLVE){
			LOGGER.error("LPSOLVE not implemented!");
		}
		if(solver.isSolver() == LPSolver.Solver.CPLEX){
			prepareConstraints(agent,world);
		}
		
	}
	
	public float solveCPLP(){

		LP = generateCostPartitioningLP();
		System.out.println(LP);
		sol = solver.solveLP(LP);
		
		return sol.getObjctiveValue();
	}
	
	public LPSolution getSolution(){
		return sol;
	}
	
	
	private Constraint getConstraint(int var, int val, DIMAPWorldInterface w){
		if(!constraints.containsKey(w.getAgentName())){
			constraints.put(w.getAgentName(), new HashMap<Integer,HashMap<Integer,Constraint>>());
		}
		if(!constraints.get(w.getAgentName()).containsKey(var)){
			constraints.get(w.getAgentName()).put(var, new HashMap<Integer,Constraint>());
		}
		if(!constraints.get(w.getAgentName()).get(var).containsKey(val)){
			constraints.get(w.getAgentName()).get(var).put(val, new Constraint(var,val,w));
		}
		return constraints.get(w.getAgentName()).get(var).get(val);
	}
	
//	private String getActionString(Action a, String agent){
//		if(a.isProjection()){
//			String l = a.getSimpleLabel().replace(" ", "_");
//			l = l + "_" + agent;
//			return l;
//		}else{
//			return  a.getLabel().replace(" ", "_"); //"a" +a.hashCode();
//		}
//	}
	
	private String getActionString(Action a, String agent){
		String l =  a.hashCode()+"_"+agent;
		l="a" +l.replaceAll("-", "m");
		return l;
	}
	
	private void prepareConstraints(String agent, DIMAPWorldInterface w){
		for(Action a : w.getProblem().getAllActions()){
//			if(a.isPublic()){
				objective += " + "+ a.getCost() + getActionString(a,agent);
//			}
			bounds.add(getActionString(a,agent) + " >= 0");
			
			for(int var : a.getPrecondition().getSetVariableNames().toArray()){
				int val = a.getPrecondition().getValue(var);
				Constraint c = getConstraint(var,val,w);
				//Always Consume
				if(a.getEffect().isSet(var) && a.getEffect().getValue(var) != val){
					c.LBLHS.add(" - " + getActionString(a,agent));
					c.UBLHS.add(" - " + getActionString(a,agent));
				}
			}
			
			for(int var : a.getEffect().getSetVariableNames().toArray()){
				int val = a.getEffect().getValue(var);
				Constraint c = getConstraint(var,val,w);
				//Always Produce
				if(a.getPrecondition().isSet(var) && a.getPrecondition().getValue(var) != val){
					c.LBLHS.add(" + " + getActionString(a,agent));
					c.UBLHS.add(" + " + getActionString(a,agent));
				}
				//Sometimes Produce
				if(!a.getPrecondition().isSet(var)){
					c.LBLHS.add(" + " + getActionString(a,agent));
				}
				//Sometimes Consume
				if(!a.getPrecondition().isSet(var)){
					for(int val2 = 0; val2 < w.getDomain().publicValMax; ++val2){
						if(val != val2){
							Constraint c2 = getConstraint(var,val2,w);
							c2.UBLHS.add(" - " + getActionString(a,agent));
						}
					}
				}
			}
			
			
		}
	}
	
	
	public String generateCostPartitioningLP(){
		//header
		LP += "\\\\LP for distributed SEQ-based cost-partitioning, domain:"+domain+", problem:"+problem+"\n\n";
				
		//objective function
		LP += "Minimize\n obj: " + objective.substring(3);
		
		//constraints
		LP +="\nSubject To\n";
		for(String agent : constraints.keySet()){
			LP += "\\\\"+agent+"\n";
			for(int var : constraints.get(agent).keySet()){
				for(int val : constraints.get(agent).get(var).keySet()){
					Constraint c = constraints.get(agent).get(var).get(val);
					if(!c.LBLHS.isEmpty()){
						LP += c.getLBLHS() + " >= " + c.LBRHS + "\n";
					}
					if(!c.UBLHS.isEmpty()){
						LP += c.getUBLHS() + " <= " + c.UBRHS + "\n";
					}
				}
			}
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

	
	private class Constraint{
		int var;
		int val;
		HashSet<String> LBLHS = new HashSet<String>();
		HashSet<String> UBLHS = new HashSet<String>();
		String LBRHS = "0";
		String UBRHS = "0";
		
		public Constraint(int var, int val, DIMAPWorldInterface w) {
			this.var = var;
			this.val = val;
			
			if(w.getProblem().goalSuperState.isSet(var)){
				if(w.getProblem().goalSuperState.getValue(var) == val && w.getProblem().initState.getValue(var) != val){
					LBRHS = "1";
					UBRHS = "1";
				}else if(w.getProblem().goalSuperState.getValue(var) != val && w.getProblem().initState.getValue(var) == val){
					LBRHS = "-1";
					UBRHS = "-1";
				}
			}else{
				if(w.getProblem().initState.getValue(var) == val){
					LBRHS = "-1";
					UBRHS = "0";
				}else{
					LBRHS = "0";
					UBRHS = "1";
				}
			}
		}
		
		public String getLBLHS(){
			String strLBLHS = "";
			for(String a : LBLHS){
				strLBLHS += a;
			}
			return strLBLHS;//.substring(3);
		}
		
		public String getUBLHS(){
			String strUBLHS = "";
			for(String a : UBLHS){
				strUBLHS += a;
			}
			return strUBLHS;//.substring(3);
		}
		
	}
	
}
