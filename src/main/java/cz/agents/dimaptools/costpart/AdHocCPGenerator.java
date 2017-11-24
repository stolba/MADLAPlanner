package cz.agents.dimaptools.costpart;

import java.util.HashSet;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.search.ExternalSingleAgentSearch;

public class AdHocCPGenerator implements CostPartitioningGeneratorInterface {
	
	private static final Logger LOGGER = Logger.getLogger(AdHocCPGenerator.class);

	
	HashSet<String> agents = new HashSet<>();
	private float k = 0;
	
	

	public AdHocCPGenerator(float k) {
		super();
		this.k = k;
	}

	@Override
	public void setProblem(String agent, Problem prob) {
		agents.add(agent);
	}
	
	public void setK(float k){
		this.k = k;
	}

	@Override
	public void updateCosts(Problem problem) {
		for(Action a : problem.getAllActions()){
			if(a.isPublic() && !a.isProjection()){
				a.setCost(k*a.getCost());
//				LOGGER.info("UPDATE("+a.getLabel()+"): "+a.getCost());
			}
			if(a.isPublic() && a.isProjection()){
				a.setCost((1-k)/(float)(agents.size()-1)*a.getCost());
//				LOGGER.info("UPDATE("+a.getLabel()+"): "+a.getCost());
			}
		}
	}

}
