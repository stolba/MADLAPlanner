package cz.agents.dimaptools.heuristic;

import cz.agents.dimaptools.communication.protocol.DistributedHeuristicReplyProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicRequestProtocol;

public interface DistributedReplyHeuristicInterface extends HeuristicInterface{

	public abstract DistributedHeuristicReplyProtocol getReplyProtocol();

	public abstract void setRequestProtocol(
			DistributedHeuristicRequestProtocol requestProtocol);

}