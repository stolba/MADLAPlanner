package cz.agents.madla.executor;

import java.util.List;

public interface PlanExecutorInterface {
	
	public abstract boolean executePlan(List<String> plan);

	public abstract boolean executePartialPlan(List<String> plan, String initiator, int solutionCost);

}