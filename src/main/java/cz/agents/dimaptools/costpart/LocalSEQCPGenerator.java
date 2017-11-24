package cz.agents.dimaptools.costpart;

import java.util.HashMap;
import java.util.HashSet;

import cz.agents.dimaptools.lp.LPSolution;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class LocalSEQCPGenerator implements CostPartitioningGeneratorInterface {
//	private LPSolution sol;
	HashSet<String> agents = new HashSet<>();
	HashMap<Integer,HashMap<String,Float>> actions = new HashMap<>();

	
	public void setLPSolution(LPSolution sol, String agent){
//		this.sol = sol;
		
		for(String action : sol.getAllVariables()){
			int hash = getHash(action);
			float val = sol.getVariableValue(action);
			
			if(!actions.containsKey(hash)){
				actions.put(hash,new HashMap<String,Float>());
			}
			if(!actions.get(hash).containsKey(agent)){
				actions.get(hash).put(agent, val);
			}
			
			actions.get(hash).put(agent, actions.get(hash).get(agent)+val);
		}
	}
	
	private int getHash(String var){
		String hashStr = var.split("_")[0];
		hashStr = hashStr.replace("a", "");
		hashStr = hashStr.replace("m", "-");
		return Integer.parseInt(hashStr);
	}
	
	
	
	/* (non-Javadoc)
	 * @see cz.agents.dimaptools.costpart.CostPartitioningGeneratorInterface#setProblem(java.lang.String, cz.agents.dimaptools.model.Problem)
	 */
	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
	}

	@Override
	public void updateCosts(Problem problem) {
		for(Action a : problem.getAllActions()){
			if(a.isPublic() || a.isProjection()){
				if(actions.containsKey(a.hashCode())){
//					float oldCost = a.getCost();
					a.setCost(getCost(a.getCost(),a.hashCode(),problem.agent,a.isProjection()));
//					LOGGER.info("UPDATE("+a.getLabel()+"): "+oldCost + "->"+a.getCost());
				}else{
//					System.out.println("KEEP("+a.getLabel()+"): "+a.getCost());
				}
			}
		}
	}
	
	private float getCost(float oldCost,int hash,String agent,boolean projection){
//		return oldCost/(float)actions.get(hash).keySet().size();
		
		if(!actions.get(hash).containsKey(agent)) return oldCost;
		
		float sum = 0;
		float max = 0;
		
		for(String ag : actions.get(hash).keySet()){
			sum += actions.get(hash).get(ag);
			max = Math.max(max, actions.get(hash).get(ag));
		}
		float agv = actions.get(hash).get(agent);
		
		if(sum==0){
			if(projection){
				return  oldCost / (float)(agents.size()-1);
			}else{
				return 0;
			}
		}
		
		return (oldCost * max * agv / sum)/max;
	}


	

}
