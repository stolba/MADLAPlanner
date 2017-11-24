package cz.agents.dimaptools.heuristic;

import cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;

public interface DistributedRequestHeuristicInterface extends HeuristicInterface{

	public abstract DistributedHeuristicRequestProtocol getRequestProtocol();

	public abstract void setReplyProtocol(
			DistributedHeuristicReplyProtocol replyProtocol);

	public abstract boolean isComputing();

	public abstract boolean hasWaitingLocalRequests();

}