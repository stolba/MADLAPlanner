package cz.agents.dimaptools.communication.message;

import java.util.Arrays;


public class HeuristicReplyWithPublicActionsMessage implements VisitableMessage {

	private final int requestHash;
	private final int stateHash;
	private final int goalHash;
	public final int heuristicValue;
	public final int[] usedPublicActionIDs;
	private final int recursionDepth;



	public HeuristicReplyWithPublicActionsMessage(int requestHash,int stateHash, int goalHash,int heuristicValue, int[] usedPublicActionIDs) {
		this.stateHash = stateHash;
		this.goalHash = goalHash;
		this.usedPublicActionIDs = usedPublicActionIDs;
		this.heuristicValue = heuristicValue;
		this.requestHash = requestHash;
		this.recursionDepth = -1;
	}

	public HeuristicReplyWithPublicActionsMessage(int requestHash,int stateHash, int goalHash,int heuristicValue, int[] usedPublicActionIDs,int recursionDepth) {
		this.stateHash = stateHash;
		this.goalHash = goalHash;
		this.usedPublicActionIDs = usedPublicActionIDs;
		this.heuristicValue = heuristicValue;
		this.requestHash = requestHash;
		this.recursionDepth = recursionDepth;
	}



	public int getStateHash() {
		return stateHash;
	}

	public int getGoalHash() {
		return goalHash;
	}

	public int getRequestHash() {
		return requestHash;
	}

	public int getRecursionDepth() {
		return recursionDepth;
	}



	@Override
	public String toString() {
		return "HeuristicReplyWithPublicActionsMessage [requestHash="
				+ requestHash + ", stateHash=" + stateHash + ", goalHash="
				+ goalHash + ", heuristicValue=" + heuristicValue
				+ ", usedPublicActionIDs="
				+ Arrays.toString(usedPublicActionIDs) + "]";
	}



	public int getBytes() {
		return 4+4+4*usedPublicActionIDs.length+(recursionDepth==-1?0:4);
	}

	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}










}
