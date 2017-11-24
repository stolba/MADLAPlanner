package cz.agents.dimaptools.costpart;

import java.util.HashMap;

import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class SEQCostPartitioningGenerator implements LPBasedCPInterface {
//	private LPSolution sol;
	private HashMap<String,Problem> problems = new HashMap<>();
	private HashMap<Integer,CPAction> actions = new HashMap<>();
	
	private boolean compensate = false;
	
	
	
	
	public SEQCostPartitioningGenerator(boolean compensate) {
		super();
		this.compensate = compensate;
	}

	/* (non-Javadoc)
	 * @see cz.agents.dimaptools.costpart.LPBasedInterface#setLPSolution(cz.agents.dimaptools.lp.LPSolution)
	 */
	@Override
	public void setLPSolution(LPSolution sol){
//		this.sol = sol;
		
		for(String action : sol.getAllVariables()){
			int hash = getHash(action);
			String agent = getAgent(action);
			
			if(problems.get(agent).getAction(hash).isPublic()){
				if(!actions.containsKey(hash)){
					actions.put(hash, new CPAction(action,sol.getVariableValue(action),hash,agent));
				}else{
					actions.get(hash).update(agent, sol.getVariableValue(action));
				}
			}
		}
	}
	
	private int getHash(String var){
		String hashStr = var.split("_")[0];
		hashStr = hashStr.replace("a", "");
		hashStr = hashStr.replace("m", "-");
		return Integer.parseInt(hashStr);
	}
	
	private String getAgent(String var){
		String ag = var.split("_")[1];
		return ag.replace("m", "-");
	}
	
	/* (non-Javadoc)
	 * @see cz.agents.dimaptools.costpart.CostPartitioningGeneratorInterface#setProblem(java.lang.String, cz.agents.dimaptools.model.Problem)
	 */
	@Override
	public void setProblem(String agent,Problem prob){
		problems.put(agent, prob);
	}
	
	/* (non-Javadoc)
	 * @see cz.agents.dimaptools.costpart.CostPartitioningGeneratorInterface#updateCosts(cz.agents.dimaptools.model.Problem)
	 */
	@Override
	public void updateCosts(Problem problem) {
		for(CPAction cpa : actions.values()){
			Action a = problem.getAction(cpa.hash);
			if(a.isPublic()){
				a.setCost(cpa.calculateCost(problem.agent, a.getCost(),a.isProjection()));
			}
		}
	}
	
	
	private class CPAction{
//		final String LPVar;
		final String label;
		final int hash;
		HashMap<String,Float> cp = new HashMap<>();
		
		private float sumCost = 0;
		private float cpCost = 0;
		
		public CPAction(String var, float val, int hash, String agent) {
//			LPVar = var;
			this.hash = hash;
			
			label = problems.get(agent).getAction(hash).getSimpleLabel();
			
			cp.put(agent, val);
			sumCost += val;
			
			System.out.println(agent + ": " + label + " = " +val);
		}
		
		public void update(String agent, float val){
			cp.put(agent, val);
			sumCost += val;
			
			System.out.println(agent + ": " + label + " = " +val);
		}
		
		public float calculateCost(String agent, float cost, boolean projection) {
			float newCost;
			
			if(sumCost==0){
//				
				
				if(compensate){
					if(projection){
						newCost = cost / (float)(cp.keySet().size()-1);
					}else{
						newCost = 0;
					}
				}else{
					newCost = 1f/(float)cp.keySet().size();
				}
			}else{
				newCost = cost * cp.get(agent)/sumCost; 
			}
			
			cpCost+=newCost;
			if(cpCost > cost){
				System.out.println("WARNING:"+label+" OVERCOST " + cpCost);
			}
			
//			System.out.println(agent+":UPDATE("+label+") "+cost + "->"+cost+"*" + cp.get(agent)+"/"+sumCost+"="+newCost);
			return newCost;
		}
		
		
		
		
		
	}


	

}
