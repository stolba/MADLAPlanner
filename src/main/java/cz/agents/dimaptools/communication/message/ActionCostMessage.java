package cz.agents.dimaptools.communication.message;

import java.util.Arrays;


public class ActionCostMessage implements VisitableMessage {

	private final int stateHash;
	private final int actionHash;
	public final int heuristicValue;


	



	public ActionCostMessage(int stateHash, int actionHash, int heuristicValue) {
		super();
		this.stateHash = stateHash;
		this.actionHash = actionHash;
		this.heuristicValue = heuristicValue;
	}
	
	

	public int getStateHash() {
		return stateHash;
	}



	public int getActionHash() {
		return actionHash;
	}



	public int getHeuristicValue() {
		return heuristicValue;
	}



	public int getBytes() {
		return 4+4+4;
	}

	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}



	@Override
	public String toString() {
		return "ActionCostMessage [stateHash=" + stateHash + ", actionHash="
				+ actionHash + ", heuristicValue=" + heuristicValue + "]";
	}

	








}
