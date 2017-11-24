package cz.agents.dimaptools.communication.message;

import cz.agents.dimaptools.heuristic.relaxed.RelaxedPlan;


public class ReconstructRPMessage implements VisitableMessage {

	private final int actionHash;
	public final RelaxedPlan rp;


	public ReconstructRPMessage(int actionHash, RelaxedPlan rp) {
		super();
		this.actionHash = actionHash;
		this.rp = rp;
	}
	
	public int getActionHash() {
		return actionHash;
	}

	public RelaxedPlan getRelaxedPlan() {
		return rp;
	}

	public int getBytes() {
		return 4+4*rp.size();
	}

	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}

	




	








}
