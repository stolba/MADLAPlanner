package cz.agents.dimaptools.lp;

import java.util.HashMap;
import java.util.Set;

public class LPSolution {
	
	private float objctiveValue;
	private String solutionStatus = "";
	
	private HashMap<String,Float> variableValues = new HashMap<>();

	public float getObjctiveValue() {
		return objctiveValue;
	}

	public void setObjectiveValue(float objctiveValue) {
		this.objctiveValue = objctiveValue;
	}
	
	public void setVariableValue(String var, float val){
		variableValues.put(var, val);
	}
	
	public boolean hasVariable(String var){
		return variableValues.containsKey(var);
	}

	public float getVariableValue(String var){
		return variableValues.get(var);
	}
	
	public Set<String> getAllVariables(){
		return variableValues.keySet();
	}

	public String getSolutionStatus() {
		return solutionStatus;
	}

	public void setSolutionStatus(String solutionStatus) {
		this.solutionStatus = solutionStatus;
	}

}
