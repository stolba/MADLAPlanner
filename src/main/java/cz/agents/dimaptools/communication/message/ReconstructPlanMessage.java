package cz.agents.dimaptools.communication.message;

import java.util.List;

/**
 * Message used to reconstruct the plan when it is found
 * @author stolba
 *
 */
public class ReconstructPlanMessage  implements VisitableMessage{

	private final List<String> plan;
	private final int lastStateHash;
	private final String initiatorID;
	private final int solutionCost;


	public ReconstructPlanMessage(List<String> plan, int hashCode, String initiatorID, int solCost) {
		this.plan = plan;
		this.lastStateHash = hashCode;
		this.initiatorID = initiatorID;
		this.solutionCost = solCost;
	}


	/**
	 * Current part of the plan
	 * @return
	 */
	public List<String> getPlan() {
		return plan;
	}


	/**
	 * Hash code of the currently last (backwards) state in the plan
	 * @return
	 */
	public int getLastStateHash() {
		return lastStateHash;
	}
	

	public String getInitiatorID() {
		return initiatorID;
	}


	public int getSolutionCost() {
		return solutionCost;
	}


	@Override
	public String toString() {
		return "ReconstructPlanMessage [plan=" + plan + ", lastStateHash="
				+ lastStateHash + ", initiator="
						+ initiatorID + ", solutionCost="
								+ solutionCost + "]";
	}


	@Override
	public void visit(MessageVisitorInterface visitor, String sender) {
		visitor.process(this, sender);
	}



}
