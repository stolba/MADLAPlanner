package cz.agents.dimaptools.heuristic.potential;

import java.util.HashMap;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.model.State;

public class PotentialHeuristic implements HeuristicInterface {
	
	HashMap<Integer, HashMap<Integer,Float>> potentials;

	private String status;
	
	public PotentialHeuristic(){
		potentials = new HashMap<Integer, HashMap<Integer,Float>>();
	}

	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {
		float h = 0;
		for(int var : state.getSetVariableNames().toArray()){
			h += potentials.get(var).get(state.getValue(var));
		}
		callback.heuristicComputed(new HeuristicResult(Math.max(0,h)));
	}
	
	public float getPublicHeuristic(State state) {
		float h = 0;
		for(int var : state.getSetVariableNames().toArray()){
			int val = state.getValue(var);
			if(state.getDomain().inDomainVar(var) && state.getDomain().inDomainVal(val)){
				if(state.getDomain().isPublicVar(var) && state.getDomain().isPublicVal(val)){
					h += potentials.get(var).get(val);
				}
			}
		}
		return h;
	}
	
	public float getPrivateHeuristic(State state) {
		float h = 0;
		for(int var : state.getSetVariableNames().toArray()){
			int val = state.getValue(var);
			if(state.getDomain().inDomainVar(var) && state.getDomain().inDomainVal(val)){
				if(!state.getDomain().isPublicVar(var) || !state.getDomain().isPublicVal(val)){
					h += potentials.get(var).get(val);
				}
			}
		}
		return h;
	}

	@Override
	public void processMessages() {
		// TODO Auto-generated method stub

	}
	
	public void setPotential(int var, int val, float pot){
		if(!potentials.containsKey(var))potentials.put(var,new HashMap<Integer,Float>());
		potentials.get(var).put(val,pot);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String solutionStatus) {
		status = solutionStatus;
	}

}
