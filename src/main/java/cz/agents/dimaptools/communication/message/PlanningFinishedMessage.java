package cz.agents.dimaptools.communication.message;

public class PlanningFinishedMessage  implements VisitableMessage{
	
	
	
	@Override
	public String toString() {
		return "PlanningFinishedMessage";
	}

	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}

}
