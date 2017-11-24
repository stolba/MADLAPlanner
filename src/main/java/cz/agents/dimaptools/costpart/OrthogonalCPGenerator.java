package cz.agents.dimaptools.costpart;

import java.util.HashSet;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class OrthogonalCPGenerator implements CostPartitioningGeneratorInterface {
	
	private static final Logger LOGGER = Logger.getLogger(OrthogonalCPGenerator.class);

	
	HashSet<String> agents = new HashSet<>();
	private boolean includePublic = true;

	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
	}
	
	public void setPublic(boolean pub){
		includePublic = pub;
	}

	@Override
	public void updateCosts(Problem problem) {
		for(Action a : problem.getAllActions()){
			if(a.isPublic()){
				if(includePublic){
					a.setCost(a.getCost());
				}else{
					a.setCost(0);
				}
				LOGGER.info("UPDATE("+a.getLabel()+"): "+a.getCost());
			}
			
		}
	}

}
