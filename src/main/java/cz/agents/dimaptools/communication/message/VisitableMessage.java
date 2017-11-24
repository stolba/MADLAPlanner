package cz.agents.dimaptools.communication.message;

public interface VisitableMessage {
	
	public void visit(MessageVisitorInterface visitor, String sender);

}
