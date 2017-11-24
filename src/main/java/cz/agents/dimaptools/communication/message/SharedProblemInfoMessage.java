package cz.agents.dimaptools.communication.message;


public class SharedProblemInfoMessage  implements VisitableMessage{

	private final int couplingEstimate;

	public SharedProblemInfoMessage(int couplingEstimate) {
		super();
		this.couplingEstimate = couplingEstimate;
	}


	public int getCouplingEstimate() {
		return couplingEstimate;
	}


	@Override
	public String toString() {
		return "SharedProblemInfoMessage [couplingEstimate="+couplingEstimate+"]";
	}


	public int getBytes() {
		return 4;
	}
	
	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}

}
