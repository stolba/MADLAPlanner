package cz.agents.dimaptools.costpart;

import cz.agents.dimaptools.model.Problem;

public interface CostPartitioningGeneratorInterface {

	public abstract void setProblem(String agent, Problem prob);

	public abstract void updateCosts(Problem problem);

}