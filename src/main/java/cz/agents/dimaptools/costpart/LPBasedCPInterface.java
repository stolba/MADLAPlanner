package cz.agents.dimaptools.costpart;

import cz.agents.dimaptools.lp.LPSolution;

public interface LPBasedCPInterface extends CostPartitioningGeneratorInterface {

	public abstract void setLPSolution(LPSolution sol);

}