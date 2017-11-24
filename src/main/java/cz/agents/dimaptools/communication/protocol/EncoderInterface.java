package cz.agents.dimaptools.communication.protocol;

import cz.agents.alite.communication.content.Content;
import cz.agents.dimaptools.communication.message.ActionCostMessage;
import cz.agents.dimaptools.communication.message.HeuristicReplyWithPublicActionsMessage;
import cz.agents.dimaptools.communication.message.HeuristicRequestMessage;
import cz.agents.dimaptools.communication.message.PlanningFinishedMessage;
import cz.agents.dimaptools.communication.message.ReconstructPlanMessage;
import cz.agents.dimaptools.communication.message.ReconstructRPMessage;
import cz.agents.dimaptools.communication.message.SharedProblemInfoMessage;
import cz.agents.dimaptools.communication.message.StateMessage;

public interface EncoderInterface {
	
	//search
	public Content encodeStateMessage(StateMessage msg);
	
	public Content encodeReconstructPlanMessage(ReconstructPlanMessage msg);
	
	public Content encodePlanningFinishedMessage(PlanningFinishedMessage msg);
	
	//heuristics
	public Content encodeHeuristicRequestMessage(HeuristicRequestMessage req);

	public Content encodeHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re);
	
	//other
	public Content encodeSharedProblemInfoMessage(SharedProblemInfoMessage msg);

	public Object decode(Content content);

	public Content encodeActionCostMessage(ActionCostMessage msg);

	public Content encodeReconstructRPMessage(ReconstructRPMessage msg);

	

}
