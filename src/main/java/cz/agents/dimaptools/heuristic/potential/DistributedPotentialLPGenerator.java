package cz.agents.dimaptools.heuristic.potential;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.SuperState;

public class DistributedPotentialLPGenerator {
	
	String LP = "";
	final DIMAPWorldInterface w;
	private final LPSolver solver;
	private final String domain;
	private final String problem;
	private final String agent;
	
	private HashMap<String,Float> publicPots = new HashMap<>();
	
	private final static Logger LOGGER = Logger.getLogger(DistributedPotentialLPGenerator.class);
	

	public DistributedPotentialLPGenerator(DIMAPWorldInterface world,LPSolver solver,String domain, String problem, String agent) {
		
		w = world;
		
		this.solver = solver;
		this.domain = domain;
		this.problem = problem;
		this.agent = agent;
	}
	
	public String getName() {
		return agent;
	}
	
	public float generateAndSolvePotentialLP(PotentialHeuristic hpot){
		String LP = "";
		if(solver.isSolver() == LPSolver.Solver.LPSOLVE){
			LOGGER.error("LPSOLVE not implemented!");
		}
		if(solver.isSolver() == LPSolver.Solver.CPLEX){
			LP = generatePotentialHeuristicLPForCPLEX();
		}
		System.out.println(LP);
		LPSolution sol = solver.solveLP(LP);
		
		parsePotentialHeuristic(sol,hpot);
		
		return sol.getObjctiveValue();
	}
	
	public HashMap<String, Float> getPublicPots() {
		return publicPots;
	}

	public void setPublicPots(HashMap<String, Float> publicPots) {
		this.publicPots = publicPots;
	}

	private void parsePotentialHeuristic(LPSolution sol, PotentialHeuristic hpot) {
		for(String potVar : sol.getAllVariables()){
			if(potVar.startsWith("P")){
				float pot = sol.getVariableValue(potVar);
				
				String V = potVar.substring(potVar.indexOf("V")+1, potVar.indexOf("v"));
				String v = potVar.substring(potVar.indexOf("v")+1, potVar.length());
				
				int var = Integer.parseInt(V);
				int val = Integer.parseInt(v);
				
				System.out.println(potVar + ": var:"+V+", val:"+v+", pot:"+pot+", "+w.getDomain().humanizeVar(var)+":"+w.getDomain().humanizeVal(val)+", public:"+w.getDomain().isPublicVar(var)+"/"+w.getDomain().isPublicVal(val));
				
				hpot.setPotential(var, val, pot);
				
				if(w.getDomain().isPublicVar(var)&&w.getDomain().isPublicVal(val)){
					publicPots.put(potVar, pot);
				}
			}
			if(potVar.startsWith("M")){
				float pot = sol.getVariableValue(potVar);
				
				String V = potVar.substring(potVar.indexOf("V")+1, potVar.length());
				int var = Integer.parseInt(V);
				
				if(w.getDomain().isPublicVar(var)){
					publicPots.put(potVar, pot);
					System.out.println(potVar + ": var:"+V+", maxpot:"+pot+", "+w.getDomain().humanizeVar(var)+", public:"+w.getDomain().isPublicVar(var));
				}
			}
		}
		
		hpot.setStatus(sol.getSolutionStatus());
	}

	
	public String generatePotentialHeuristicLPForCPLEX(){
		boolean plus = false;
		
		//header
		LP += "\\LP for potential heuristic, domain:"+domain+", problem:"+problem+", agent:"+agent+"\n\n";
		
		
		//objective function
		LP += "Maximize\n obj: ";
		for(int var : w.getProblem().initState.getSetVariableNames().toArray()){
			int val = w.getProblem().initState.getValue(var);
			LP += "PV"+var+"v"+val+" + ";
			LOGGER.info("maximizing " + "PV"+var+"v"+val+" - " + w.getDomain().humanizeVar(var) + ":" + w.getDomain().humanizeVal(val) +", public:"+w.getDomain().isPublicVar(var)+"/"+w.getDomain().isPublicVal(val));
		}
		LP += " 0\n\n";
		
		//constraints
		LP +="Subject To\n";
		//maxpot constraints and bounds
		Set<String> maxpots = new HashSet<String>();
		Set<String> bounds = new HashSet<String>();
		for(int var : w.getProblem().initState.getSetVariableNames().toArray()){
			int val = w.getProblem().initState.getValue(var);
			maxpots.add("PV"+var+"v"+val+" - MV"+var+" <= 0");
//			if(w.getDomain().isPublicVar(var)&&w.getDomain().isPublicVal(val)){
				bounds.add("-inf <= PV"+var+"v"+val);
				bounds.add("-inf <= MV"+var);
//			}
		}
		for(int var : w.getProblem().goalSuperState.getSetVariableNames().toArray()){
			int val = w.getProblem().goalSuperState.getValue(var);
			maxpots.add("PV"+var+"v"+val+" - MV"+var+" <= 0");
//			if(w.getDomain().isPublicVar(var)&&w.getDomain().isPublicVal(val)){
				bounds.add("-inf <= PV"+var+"v"+val);
				bounds.add("-inf <= MV"+var);
//			}
		}
		for(Action a : w.getProblem().getAllActions()){
			for(int var : a.getPrecondition().getSetVariableNames().toArray()){
				int val = a.getPrecondition().getValue(var);
				maxpots.add("PV"+var+"v"+val+" - MV"+var+" <= 0");
//				if(w.getDomain().isPublicVar(var)&&w.getDomain().isPublicVal(val)){
					bounds.add("-inf <= PV"+var+"v"+val);
					bounds.add("-inf <= MV"+var);
//				}
			}
			for(int var : a.getEffect().getSetVariableNames().toArray()){
				int val = a.getEffect().getValue(var);
				maxpots.add("PV"+var+"v"+val+" - MV"+var+" <= 0");
//				if(w.getDomain().isPublicVar(var)&&w.getDomain().isPublicVal(val)){
					bounds.add("-inf <= PV"+var+"v"+val);
					bounds.add("-inf <= MV"+var);
//				}
			}
		}
		for(String m : maxpots){
			LP+=m+"\n";
		}
		
		//goal constraint
		plus = false;
		for(int var = 0; var < w.getDomain().publicVarMax; var++){
			if(plus){
				LP += " + ";
			}else{
				plus = true;
			}
			if(w.getProblem().goalSuperState.isSet(var)){
				LP += "PV"+var+"v"+w.getProblem().goalSuperState.getValue(var);
			}else{
				LP += "MV"+var;
			}
		}
		for(int var = w.getDomain().agentVarMin; var < w.getDomain().agentVarMax; var++){
			if(plus){
				LP += " + ";
			}else{
				plus = true;
			}
			if(w.getProblem().goalSuperState.isSet(var)){
				LP += "PV"+var+"v"+w.getProblem().goalSuperState.getValue(var);
			}else{
				LP += "MV"+var;
			}
		}
		LP += " <= 0\n";
		
		//operator constraints
		for(Action a : w.getProblem().getAllActions()){
			SuperState pre = a.getPrecondition();
			SuperState eff = a.getEffect();
			
			if(!eff.getSetVariableNames().isEmpty()){
			
				plus = false;
				for(int var : eff.getSetVariableNames().toArray()){
					if(plus){
						LP += " + ";
					}else{
						plus = true;
					}
					if(pre.isSet(var)){
						LP+="PV"+var+"v"+pre.getValue(var)+" - "+"PV"+var+"v"+eff.getValue(var);
					}else{
						LP+="MV"+var+" - "+"PV"+var+"v"+eff.getValue(var);
					}
				}
				
				LP += " <= "+a.getCost()+"\n";
				
			}
		}
		
//		//privat >= public constraint function
//		String pub = "";
//		String priv = "";
//				for(int var : w.getProblem().initState.getSetVariableNames().toArray()){
//					int val = w.getProblem().initState.getValue(var);
//					if(w.getDomain().isPublicVar(var) && w.getDomain().isPublicVal(val)){
//						if(pub.length()>0)pub += " + ";
//						pub += "PV"+var+"v"+val;
//					}else{
//						priv += " - PV"+var+"v"+val;
//					}
//				}
//				LP += pub + priv + " <= 0\n";
		
		//public pots constraints
		for(String potvar : publicPots.keySet()){
			LP += potvar + " = "+publicPots.get(potvar)+"\n";
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
