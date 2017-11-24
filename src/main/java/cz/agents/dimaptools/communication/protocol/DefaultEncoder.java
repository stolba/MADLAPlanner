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

public class DefaultEncoder implements EncoderInterface {

	@Override
	public Content encodeStateMessage(StateMessage msg) {
		return new Content(msg);
	}

	@Override
	public Content encodeReconstructPlanMessage(ReconstructPlanMessage msg) {
		return new Content(msg);
	}

	@Override
	public Content encodePlanningFinishedMessage(PlanningFinishedMessage msg) {
		return new Content(msg);
	}
	
	@Override
	public Content encodeHeuristicRequestMessage(HeuristicRequestMessage req) {
		return new Content(req);
	}

	@Override
	public Content encodeHeuristicReplyWithPublicActionsMessage(HeuristicReplyWithPublicActionsMessage re) {
		return new Content(re);
	}
	
	@Override
	public Content encodeSharedProblemInfoMessage(SharedProblemInfoMessage msg) {
		return new Content(msg);
	}
	
	@Override
	public Content encodeActionCostMessage(ActionCostMessage msg) {
		return new Content(msg);
	}
	
	@Override
	public Content encodeReconstructRPMessage(ReconstructRPMessage msg) {
		return new Content(msg);
	}
	
	

	@Override
	public Object decode(Content content) {
		return content.getData();
	}

	

	

	

	

}
