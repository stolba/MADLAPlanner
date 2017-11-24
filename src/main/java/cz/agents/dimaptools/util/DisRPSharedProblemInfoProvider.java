package cz.agents.dimaptools.util;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.RelaxedPlan;
import cz.agents.dimaptools.heuristic.relaxed.SynchronizedDistributedRelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.DistributedFFEvaluator;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

public class DisRPSharedProblemInfoProvider extends SharedProblemInfoProvider {
	
	private static final Logger LOGGER = Logger.getLogger(DisRPSharedProblemInfoProvider.class);
	
	private final DistributedFFEvaluator ff;
	private final SynchronizedDistributedRelaxationHeuristic heuristic;
	
	private int coupling = -1;
	

	public DisRPSharedProblemInfoProvider(DIMAPWorldInterface world,int totalAgents) {
		super(world, totalAgents);
		
		ff = new DistributedFFEvaluator(world);
		heuristic = new SynchronizedDistributedRelaxationHeuristic(world,ff);
	}
	
	@Override
	public int computeMyCoupling(final Problem problem){
		
		heuristic.getHeuristic(problem.initState, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				int pub=0;
				int priv=0;
				int all=0;
				int proj=0;
				
				if(LOGGER.isInfoEnabled())LOGGER.info(world.getAgentName() + " - rp:");
				RelaxedPlan rp = ff.getRelaxedPlan();
				for(int ai : rp.toArray()){
					Action a = problem.getAction(ai);
					
					if(a != null){
						if(LOGGER.isInfoEnabled())LOGGER.info(world.getAgentName() + " - " + a.getLabel());
						if(a != null && !a.isProjection()){
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
				}
				
				if(LOGGER.isInfoEnabled())LOGGER.info(world.getAgentName() + " - all:"+rp.size()+",proj:"+proj+",own:"+all+",pub:"+pub+",priv:"+priv+" - heuristic="+result.getValue());
				
				//if there are no actions of the agent, consider it coupled, i.e we don't need any private information of the agent
				coupling = all==0 ? 100 : (int)(((double)pub) / ((double)all) * 100);
				
			}
		});
		
		
		while(coupling < 0){	//TODO: need to wait for other agents to finish?
			world.getCommPerformer().performReceiveNonblock();
			heuristic.processMessages();
		}
		
		
		
//		LOGGER.info("coupling:"+coupling+"computed - return");
		
		return coupling;
		
		
	}

}
