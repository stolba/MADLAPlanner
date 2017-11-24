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

public class SEQCostPartLPGenerator {
	
	String LP = "";
	String objective = "";
	Set<String> bounds = new HashSet<String>();
	final DIMAPWorldInterface w;
	private final LPSolver solver;
	private final String domain;
	private final String problem;
	private final String agent;
	
	private LPSolution sol = null;
	
	private HashMap<Integer,HashMap<Integer,Constraint>> constraints = new HashMap<>();
	
	private final static Logger LOGGER = Logger.getLogger(SEQCostPartLPGenerator.class);
	

	public SEQCostPartLPGenerator(DIMAPWorldInterface world,LPSolver solver,String domain, String problem, String agent) {
		
		w = world;
		
		this.solver = solver;
		this.domain = domain;
		this.problem = problem;
		this.agent = agent;
	}
	
	public float generateAndSolveCPLP(){
		String LP = "";
		if(solver.isSolver() == LPSolver.Solver.LPSOLVE){
			LOGGER.error("LPSOLVE not implemented!");
		}
		if(solver.isSolver() == LPSolver.Solver.CPLEX){
			prepareConstraints();
			LP = generateCostPartitioningLP();
		}
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(LP);
		}
		sol = solver.solveLP(LP);
		
		return sol.getObjctiveValue();
	}
	
	public String getName() {
		return agent;
	}
	
	public LPSolution getSolution(){
		return sol;
	}
	
	private Constraint getConstraint(int var, int val){
		if(!constraints.containsKey(var)){
			constraints.put(var, new HashMap<Integer,Constraint>());
		}
		if(!constraints.get(var).containsKey(val)){
			constraints.get(var).put(val, new Constraint(var,val));
		}
		return constraints.get(var).get(val);
	}
	
//	private String getActionString(Action a){
//		if(a.isProjection()){
//			String l = a.getSimpleLabel().replace(" ", "_");
//			l = l + "_" + agent;
//			return l;
//		}else{
//			return  a.getLabel().replace(" ", "_"); //"a" +a.hashCode();
//		}
//	}
	
	private String getActionString(Action a){
		String l =  "" + a.hashCode();
		l="a" +l.replaceAll("-", "m");
		return l;
	}
	
	private void prepareConstraints(){
		for(Action a : w.getProblem().getAllActions()){
//			LOGGER.info(a);
			objective += " + " + a.getCost() + getActionString(a);
			bounds.add(getActionString(a) + " >= 0");
			
			for(int var : a.getPrecondition().getSetVariableNames().toArray()){
				int val = a.getPrecondition().getValue(var);
				Constraint c = getConstraint(var,val);
				//Always Consume
				if(a.getEffect().isSet(var) && a.getEffect().getValue(var) != val){
//					LOGGER.info("always consume " + w.getDomain().humanizeVal(c.val));
					c.LBLHS.add(" - " + getActionString(a));
					c.UBLHS.add(" - " + getActionString(a));
				}
			}
			
			for(int var : a.getEffect().getSetVariableNames().toArray()){
				int val = a.getEffect().getValue(var);
				Constraint c = getConstraint(var,val);
				//Always Produce
				if(a.getPrecondition().isSet(var) && a.getPrecondition().getValue(var) != val){
//					LOGGER.info("always produce " + w.getDomain().humanizeVal(c.val));
					c.LBLHS.add(" + " + getActionString(a));
					c.UBLHS.add(" + " + getActionString(a));
				}
				//Sometimes Produce
				if(!a.getPrecondition().isSet(var)){
//					LOGGER.info("sometimes produce " + w.getDomain().humanizeVal(c.val));
					c.LBLHS.add(" + " + getActionString(a));
				}
				//Sometimes Consume
				if(!a.getPrecondition().isSet(var)){
					for(int val2 = 0; val2 < w.getDomain().publicValMax; ++val2){
						if(val != val2){
							Constraint c2 = getConstraint(var,val2);
//							LOGGER.info("sometimes consume " + w.getDomain().humanizeVal(c2.val));
							c2.UBLHS.add(" - " + getActionString(a));
						}
					}
				}
			}
			
			
		}
	}
	
	
	
	public String generateCostPartitioningLP(){
		//header
		LP += "\\\\LP for SEQ-based cost-partitioning, domain:"+domain+", problem:"+problem+", agent:"+agent+"\n\n";
				
		//objective function
		LP += "Minimize\n obj: " + objective.substring(3);
		
		//constraints
		LP +="\nSubject To\n";
		for(int var : constraints.keySet()){
			for(int val : constraints.get(var).keySet()){
				Constraint c = constraints.get(var).get(val);
				if(!c.LBLHS.isEmpty()){
					LP += c.getLBLHS() + " >= " + c.LBRHS +" \\\\"+w.getDomain().humanizeVal(c.val)+ "\n";
				}
				if(!c.UBLHS.isEmpty()){
					LP += c.getUBLHS() + " <= " + c.UBRHS +" \\\\"+w.getDomain().humanizeVal(c.val)+ "\n";
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
		
		public Constraint(int var, int val) {
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
