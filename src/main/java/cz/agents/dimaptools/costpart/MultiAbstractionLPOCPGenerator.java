package cz.agents.dimaptools.costpart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.abstractions.AbstractEdge;
import cz.agents.dimaptools.heuristic.abstractions.AbstractState;
import cz.agents.dimaptools.heuristic.abstractions.Abstraction;
import cz.agents.dimaptools.heuristic.abstractions.AtomicProjection;
import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class MultiAbstractionLPOCPGenerator implements CostPartitioningGeneratorInterface {
	
	private final LPSolver solver;
	private LPSolution sol;
	String LP = "";
	Set<String> agents = new HashSet<>();
	Map<String,Float> actions = new HashMap<>();
	String stateConstraints = "";
	String bounds = "";
	boolean free = false;
	int stateLimit = 100;
	
	private Map<String,Map<Integer,AtomicProjection>> projections = new HashMap<>();
	
	
	private final static Logger LOGGER = Logger.getLogger(MultiAbstractionLPOCPGenerator.class);
	
	

	public MultiAbstractionLPOCPGenerator(LPSolver solver, boolean free) {
		super();
		this.solver = solver;
		this.free = free;
	}

	@Override
	public void setProblem(String agent, Problem problem) {
		agents.add(agent);
		projections.put(agent, new HashMap<Integer,AtomicProjection>());

		
		for(int var=0; var < problem.getDomain().publicVarMax; ++var){
			LOGGER.info(agent + ": " + var + "=" + problem.getDomain().humanizeVar(var));
			LOGGER.info(agent + ": " + 9 + "=" + problem.getDomain().humanizeVal(9));
			LOGGER.info("init: " + problem.initState);
			LOGGER.info(problem.getDomain() +", PUBLIC:" +problem.getDomain().publicVarMax);
			AtomicProjection ap = new AtomicProjection(problem,var);
			if(LOGGER.isInfoEnabled())LOGGER.info(agent + "(public): " + ap);
			projections.get(agent).put(var, ap);
		}
		
		boolean onlyPublic = false;
		if(!onlyPublic){
			for(int var=problem.getDomain().agentVarMin; var < problem.getDomain().agentVarMax; ++var){
				AtomicProjection ap = new AtomicProjection(problem,var);
				if(LOGGER.isInfoEnabled())LOGGER.info(agent + ": " + ap);
				projections.get(agent).put(var, ap);
			}
		}
		
		for(int var : projections.get(agent).keySet()){
			
			Abstraction abs = projections.get(agent).get(var);
		
			if(abs.getInit() != null){ 
				stateConstraints +=  getStateString(abs.getInit(),var, agent,problem) + " = 0\n";
			}
			
			for(AbstractState s : abs.getAllStates()){
				if(free){
					bounds += getStateString(s,var,agent,problem) + " >= -10000\n";
				}else{
					bounds += getStateString(s,var,agent,problem) + " >= 0\n";
				}
				if(s.isGoalState()){
					stateConstraints +=  getHeuristicString(agent,var) + " - " + getStateString(s,var,agent,problem) + " <= 0\n";
				}
			}
			
			for(AbstractEdge e : abs.getAllEdges()){
				for(int l : e.getLabels()){
					LOGGER.info(agent + " action: " + l);
					String act =  getActionString(problem.getAction(l),var,agent);
					stateConstraints += getStateString(e.getTo(),var,agent,problem) + " - " + getStateString(e.getFrom(),var,agent,problem) + " - " + act + " <= 0\n";
				}
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
				
				float oldCost = a.getCost();
				float cpCost = 0;
				for(int var : projections.get(problem.agent).keySet()){
					cpCost += sol.getVariableValue(getActionString(a,var,problem.agent));
				}
				float newCost =  cpCost;
				a.setCost(newCost);
				LOGGER.info("UPDATE("+a.hashCode()+"): "+oldCost+" -> "+newCost + " ("+a+")");
//				LOGGER.info("UPDATE("+a.hashCode()+"): "+oldCost+" -> "+newCost + " ("+a+")" +", cpCost:"+cpCost+", altCost:"+altCost);
			}
		}
	}
	
	private String getActionString(Action a,int var, String agent){
		String l =  Integer.toString(a.hashCode());
		l="a" +l.replaceAll("-", "m");
		actions.put(l,a.getCost());
		return l+"_"+var+"_"+agent;

//		return l+"_"+agent;
	}
	
//	private String getActionString(Action a, String agent){
//		String l =  Integer.toString(a.hashCode());
//		l="a" +l.replaceAll("-", "m");
//		actions.put(l,a.getCost());
////		return l+"_"+var+"_"+agent;
//
//		return l+"_"+agent;
//	}
	
	private String getHeuristicString(String agentName,int var){
		return "h_"+var+"_"+agentName;
	}
	
	private String getStateString(AbstractState s,int var, String agent, Problem prob){
		String l;
		if(prob.getDomain().isPublicVar(var)){
			l =  s.hashCode()+"_"+var;
		}else{
			l =  s.hashCode()+"_"+var+"_"+agent;
		}
		

//		String l =  s.hashCode()+"_"+var;
		
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
			for(int var : projections.get(a).keySet()){
				if(plus) LP += " + ";
				LP += getHeuristicString(a,var);
				plus = true;
			}
		}
		
		//constraints
		LP +="\nSubject To\n";
		LP += stateConstraints;
		
		for(String act : actions.keySet()){
			plus = false;
			for(String ag : agents){
				for(int var : projections.get(ag).keySet()){
					if(plus) LP += " + ";
					plus = true;
					LP += act +"_"+var+ "_" + ag + " ";
				
				
					if(free){
						bounds += act +"_"+var + "_" + ag + " >= -10000\n";
					}else{
						bounds += act +"_"+var + "_" + ag + " >= 0\n";
					}
				
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
