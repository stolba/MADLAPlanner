package cz.agents.dimaptools.input.sas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.Problem;

/**
 * Representation of the FDR/SAS+ domain and problem as returned from the SASParser
 * @author stolba
 *
 */
public class SASDomain {

	private List<String> stateVariableNames;
    private final Map<String, List<String>> orderedStateVariableDomainMap;

    Map<String, String> init;
    Map<String, String> goal;

    Set<SASOperator> operators = new HashSet<SASOperator>();

    public SASDomain(){
    	orderedStateVariableDomainMap = new LinkedHashMap<String, List<String>>();
    }

    public void addVariable(String var){
    	orderedStateVariableDomainMap.put(var, new LinkedList<String>());
    }

    public void addValue(String var, String val){
    	orderedStateVariableDomainMap.get(var).add(val);
    }

    public String getVar(int var){
    	return stateVariableNames.get(var);
    }

    public String getVal(int var, int val){
    	return orderedStateVariableDomainMap.get(stateVariableNames.get(var)).get(val);
    }

    public String getVal(String var, int val){
    	return orderedStateVariableDomainMap.get(var).get(val);
    }
    
    public List<String> getDomain(String var){
    	return orderedStateVariableDomainMap.get(var);
    }

    public Set<String> getVariables(){
    	return orderedStateVariableDomainMap.keySet();
    }

    public void finishVariables(){
    	stateVariableNames = new LinkedList<String>(orderedStateVariableDomainMap.keySet());
    }

    public void setInit(Map<String, String> map){
    	init = map;
    }

    public void setGoal(Map<String, String> map){
    	goal = map;
    }

	public void addOperator(String name,String label, Map<String, String> pre,Map<String, String> eff, int cost) {
		operators.add(new SASOperator(name,label,pre,eff,cost));
	}

	public int operators() {
		return operators.size();
	}
	
	public SASDomain(Domain dom, Problem prob, String agent, Set<Action> actions, int costMultiply){
		this();
		
		stateVariableNames = new LinkedList<String>();
		init = new HashMap<String, String>();
		goal = new HashMap<String, String>();
		
		//variable and values
		for(int var : dom.getVariableDomains().keySet()){
			String varS = dom.humanizeVar(var);
			stateVariableNames.add(varS);
			orderedStateVariableDomainMap.put(varS, new LinkedList<String>());
			
			for(int val : dom.getVariableDomains().get(var)){
				
				String valS = dom.humanizeVal(val);
				orderedStateVariableDomainMap.get(varS).add(valS);
				
			}
		}
		
		//init
		for(int var : prob.initState.getSetVariableNames().toArray()){
			int val = prob.initState.getValue(var);
			
			init.put(dom.humanizeVar(var), dom.humanizeVal(val));
		}
		
		//goal
		for(int var : prob.goalSuperState.getSetVariableNames().toArray()){
			int val = prob.goalSuperState.getValue(var);
			
			goal.put(dom.humanizeVar(var), dom.humanizeVal(val));
		}
		
		//actions
		for(Action a : actions){
			Map<String,String> pre = new HashMap<String, String>();
			Map<String,String> eff = new HashMap<String, String>();
			
			for(int var : a.getPrecondition().getSetVariableNames().toArray()){
				int val = a.getPrecondition().getValue(var);
				
				pre.put(dom.humanizeVar(var), dom.humanizeVal(val));
			}
			
			for(int var : a.getEffect().getSetVariableNames().toArray()){
				int val = a.getEffect().getValue(var);
				
				eff.put(dom.humanizeVar(var), dom.humanizeVal(val));
			}
			
			SASOperator op = new SASOperator(a.getSimpleLabel(), a.getLabel(), pre, eff, (int)(a.getCost()*costMultiply));
			operators.add(op);
		}
	}

}
