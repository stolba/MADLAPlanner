package cz.agents.dimaptools.communication.message;

import java.util.Arrays;

import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.State;
import cz.agents.dimaptools.model.SuperState;

public class HeuristicRequestMessage  implements VisitableMessage{

	private final int hash;
	private final int[] stateValues;
	private final int[] requestedValues;
	private final int recursionDepth;

	public HeuristicRequestMessage(int goalHash, int[] stateValues,int[] requestedValues, int recursionDepth) {
		super();
		this.hash = goalHash;
		this.stateValues = stateValues;
		this.requestedValues = requestedValues;
		this.recursionDepth = recursionDepth;
	}

	public HeuristicRequestMessage(int goalHash, int[] stateValues,int[] requestedValues) {
		super();
		this.hash = goalHash;
		this.stateValues = stateValues;
		this.requestedValues = requestedValues;
		this.recursionDepth = -1;
	}



	public int getRequestHash() {
		return hash;
	}



	public State getState(Domain dom){
		return new State(dom,stateValues);
	}

	public SuperState getRequestedGoal(Domain dom){
		return new SuperState(dom,requestedValues);
	}

	public int[] getRequestedValues(){
		return requestedValues;
	}
	
	public int[] getStateValues(){
		return stateValues;
	}

	public int getRecursionDepth() {
		return recursionDepth;
	}


	@Override
	public String toString() {
		return "HeuristicRequestMessage [\nstate="
				+ Arrays.hashCode(stateValues) + ", \nrequestedGoal=" + Arrays.toString(requestedValues) + "("+Arrays.hashCode(requestedValues)+")]";
	}

	public String humanize(Domain dom){
		return "HeuristicRequestMessage [\nstate="
				+ Arrays.hashCode(stateValues) + ", \nrequestedGoal=" + dom.humanize(requestedValues) + "("+Arrays.hashCode(requestedValues)+")]";
	}



	public int getBytes() {
		return 4+4*stateValues.length+4*requestedValues.length+(recursionDepth==-1?0:4);
	}
	
	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}

}
