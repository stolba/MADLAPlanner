package cz.agents.dimaptools.heuristic.potential;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.SuperState;

public class PotentialLPGenerator {
	
	String LP = "";
	final DIMAPWorldInterface w;
	private final LPSolver solver;
	private final String domain;
	private final String problem;
	private final String agent;
	
	private final static Logger LOGGER = Logger.getLogger(PotentialLPGenerator.class);
	

	public PotentialLPGenerator(DIMAPWorldInterface world,LPSolver solver,String domain, String problem, String agent) {
		
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
			LP = generatePotentialHeuristicLPForLPSolve();
		}
		if(solver.isSolver() == LPSolver.Solver.CPLEX){
			LP = generatePotentialHeuristicLPForCPLEX();
		}
//		System.out.println(LP);
		LPSolution sol = solver.solveLP(LP);
		
		parsePotentialHeuristic(sol,hpot);
		
		return sol.getObjctiveValue();
	}
	
	private void parsePotentialHeuristic(LPSolution sol, PotentialHeuristic hpot) {
		for(String potVar : sol.getAllVariables()){
			if(potVar.startsWith("P")){
				float pot = sol.getVariableValue(potVar);
				
				String V = potVar.substring(potVar.indexOf("V")+1, potVar.indexOf("v"));
				String v = potVar.substring(potVar.indexOf("v")+1, potVar.length());
				
				int var = Integer.parseInt(V);
				int val = Integer.parseInt(v);
				
//				System.out.println("var:"+V+", val:"+v+", pot:"+pot+", "+w.getDomain().humanizeVar(var)+":"+w.getDomain().humanizeVal(val)+", public:"+w.getDomain().isPublicVar(var)+"/"+w.getDomain().isPublicVal(val));
				
				hpot.setPotential(var, val, pot);
			}
		}
		
		hpot.setStatus(sol.getSolutionStatus());
	}

	

	public String generatePotentialHeuristicLPForLPSolve(){
				//header
				LP += "/*LP for potential heuristic, domain:"+domain+", problem:"+problem+", agent:"+agent+"*/\n\n";
				
				
				//objective function
				LP += "max: ";
				for(int var : w.getProblem().initState.getSetVariableNames().toArray()){
					int val = w.getProblem().initState.getValue(var);
					LP += "PV"+var+"v"+val+" + ";
					LOGGER.info("maximizing " + "PV"+var+"v"+val+" - " + w.getDomain().humanizeVar(var) + ":" + w.getDomain().humanizeVal(val) +", public:"+w.getDomain().isPublicVar(var)+"/"+w.getDomain().isPublicVal(val));
				}
				LP += " 0;\n\n";
				
				//constraints
				//maxpot constraints
				Set<String> maxpots = new HashSet<String>();
				for(int var : w.getProblem().initState.getSetVariableNames().toArray()){
					maxpots.add("PV"+var+"v"+w.getProblem().initState.getValue(var)+" <= MV"+var+";");
				}
				for(int var : w.getProblem().goalSuperState.getSetVariableNames().toArray()){
					maxpots.add("PV"+var+"v"+w.getProblem().goalSuperState.getValue(var)+" <= MV"+var+";");
				}
				for(Action a : w.getProblem().getAllActions()){
					for(int var : a.getPrecondition().getSetVariableNames().toArray()){
						maxpots.add("PV"+var+"v"+a.getPrecondition().getValue(var)+" <= MV"+var+";");
					}
					for(int var : a.getEffect().getSetVariableNames().toArray()){
						maxpots.add("PV"+var+"v"+a.getEffect().getValue(var)+" <= MV"+var+";");
					}
				}
				for(String m : maxpots){
					LP+=m+"\n";
				}
				
				//goal constraint
				for(int var = 0; var < w.getDomain().publicVarMax; var++){
					if(w.getProblem().goalSuperState.isSet(var)){
						LP += "PV"+var+"v"+w.getProblem().goalSuperState.getValue(var)+" + ";
					}else{
						LP += "MV"+var+" + ";
					}
				}
				for(int var = w.getDomain().agentVarMin; var < w.getDomain().agentVarMax; var++){
					if(w.getProblem().goalSuperState.isSet(var)){
						LP += "PV"+var+"v"+w.getProblem().goalSuperState.getValue(var)+" + ";
					}else{
						LP += "MV"+var+" + ";
					}
				}
				LP += " 0 <= 0;\n";
				
				//operator constraints
				for(Action a : w.getProblem().getAllActions()){
					SuperState pre = a.getPrecondition();
					SuperState eff = a.getEffect();
					
					//AHAA! this is not so simple - a projected action may have no effect, but in fact may be part of the solution
					//in projection, the facts may be produced out of nowhere! (unload)
					//and consuming a fact makes sense even without any effects (load)
					if(!eff.getSetVariableNames().isEmpty()){
					
						for(int var : eff.getSetVariableNames().toArray()){
							if(pre.isSet(var)){
								LP+="PV"+var+"v"+pre.getValue(var)+" - "+"PV"+var+"v"+eff.getValue(var)+" + ";
							}else{
								LP+="MV"+var+" - "+"PV"+var+"v"+eff.getValue(var)+" + ";
							}
						}
						
						LP += " 0 <= "+a.getCost()+";\n";
						
					}
				}
				
				return LP;
	
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
		
		//privat >= public constraint function
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
