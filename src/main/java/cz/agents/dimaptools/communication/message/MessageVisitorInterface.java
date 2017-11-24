package cz.agents.dimaptools.communication.message;


public interface MessageVisitorInterface {
	
	public void process(HeuristicReplyWithPublicActionsMessage msg, String sender);
	
	public void process(HeuristicRequestMessage msg, String sender);
		
	public void process(PlanningFinishedMessage msg, String sender);
	
	public void process(ReconstructPlanMessage msg, String sender);
	
	public void process(StateMessage msg, String sender);
	
	public void process(SharedProblemInfoMessage msg, String sender);

	public void process(ActionCostMessage msg, String sender);

	public void process(ReconstructRPMessage reconstructRPMessage, String sender);

}
