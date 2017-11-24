package cz.agents.dimaptools.util;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RelaxedPlan;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class RPSharedProblemInfoProvider extends SharedProblemInfoProvider {
	
	private static final Logger LOGGER = Logger.getLogger(RPSharedProblemInfoProvider.class);
	
	private boolean includeProjections = false;

	public RPSharedProblemInfoProvider(DIMAPWorldInterface world,int totalAgents,boolean includeProjections) {
		super(world, totalAgents);
		this.includeProjections = includeProjections;
	}
	
	@Override
	public int computeMyCoupling(Problem problem){
		FFEvaluator ff = new FFEvaluator(problem);
		RelaxationHeuristic heur = new RelaxationHeuristic(problem, ff);
		heur.getHeuristic(problem.initState, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				// TODO Auto-generated method stub
				
			}
		});
		
		int pub=0;
		int priv=0;
		int all=0;
		int proj=0;
		
		RelaxedPlan rp = ff.getRelaxedPlan();
		for(int ai : rp.toArray()){
			Action a = problem.getAction(ai);
			
			if(!a.isProjection()){
				if(a.isPublic()){
					++pub;
				}else{
					++priv;
				}
				++all;
			}else{
				++proj;
			}
		}
		
		if(LOGGER.isInfoEnabled())LOGGER.info("all:"+rp.size()+",own:"+all+",pub:"+pub+",priv:"+priv);
		
		if(includeProjections){
			//if there are no actions of the agent, consider it coupled, i.e we don't need any private information of the agent
			return all==0 ? 100 : (int)(((double)(pub+proj)) / ((double)(all+proj)) * 100);
		}else{
			return all==0 ? 100 : (int)(((double)pub) / ((double)all) * 100);
		}
		
	}

}
