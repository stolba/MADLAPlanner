package cz.agents.dimaptools.costpart;

import java.util.HashMap;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;


public class CPTest {
	
	private final static Logger LOGGER = Logger.getLogger(CPTest.class);
	
	private HashMap<Integer,Float> originalCosts = new HashMap<>();
	private HashMap<Integer,Float> costs = new HashMap<>();
	
	public void addOriginalProblem(Problem prob){
		for(Action a : prob.getAllActions()){
//			if(a.isPublic()){
				originalCosts.put(a.hashCode(),a.getCost());
//			}
		}
	}
	
	public void addCPProblem(Problem prob){
		for(Action a : prob.getAllActions()){
//			if(a.isPublic()){
				if(costs.containsKey(a.hashCode())){
					costs.put(a.hashCode(),costs.get(a.hashCode())+a.getCost());
				}else{
					costs.put(a.hashCode(),a.getCost());
				}
//			}
		}
	}
	
	public boolean testCP(){
		boolean cp = true;
		
		for(Integer a : costs.keySet()){
			float cpcost = costs.get(a);
			if(originalCosts.containsKey(a)){
				float origcost = originalCosts.get(a);
				if(cpcost!=origcost){
					LOGGER.warn(a + ": original:"+origcost+", cpcost:"+cpcost);
					if(cpcost > origcost){
						cp = false;
					}
				}
			}else{
				LOGGER.warn(a + " missing in the original problems!");
			}
		}
		
		return cp;
	}

}
