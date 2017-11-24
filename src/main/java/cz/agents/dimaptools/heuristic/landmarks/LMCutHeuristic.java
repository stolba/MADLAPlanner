package cz.agents.dimaptools.heuristic.landmarks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.UnaryOperator;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.MaxEvaluator;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

public class LMCutHeuristic implements HeuristicInterface {
	
	private final Logger LOGGER;
	
	private final String agent;
	private final Problem problem;
	
	private Map<Integer,Integer> cf = new HashMap<>();
	private int hmaxValue = Integer.MAX_VALUE;
	
	private List<Set<UnaryOperator>> landmarks = new LinkedList<Set<UnaryOperator>>();

	public LMCutHeuristic(Problem problem) {
		agent = problem.agent;
		this.problem = problem;
		
		LOGGER = Logger.getLogger(agent + "." + LMCutHeuristic.class);
		
		cf = new HashMap<>();
		
		for(Action a : problem.getAllActions()){
			cf.put(a.hashCode(), (int)a.getCost());
		}
		
		cf.put(-1,0);//dummy actions
		
	}
	
	private Set<UnaryOperator> computeNextLandmark(State s){
		LOGGER.info(agent + " compute hmax...");
		if(LOGGER.isInfoEnabled())LOGGER.info(agent + " cost function:"+cf);
		
		RelaxationHeuristic hmax = new RelaxationHeuristic(problem, new MaxEvaluator(problem),false,cf);
		hmax.getHeuristic(s, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				hmaxValue = result.getValue();
			}
		});
		
		LOGGER.info(agent + " hmax="+hmaxValue);
		
		if(hmaxValue == 0){
			return null;
		}
		
		LOGGER.info(agent + " build justification graph...");
		
		JustificationGraph JG = new JustificationGraph(hmax.getOperators(),hmax.getPropositions(), s, hmax.getGoalPropositions());
//		LOGGER.info(agent +":"+JG);
		
		LOGGER.info(agent + " find cut...");
		
		return JG.computeCut();
	}
	
	
	
	@Override
	public void getHeuristic(State state, HeuristicComputedCallback callback) {
		if(LOGGER.isDebugEnabled())LOGGER.debug(agent + " get heuristic: " + state);

		int h = 0;
		int iteration = 0;
		
		while(hmaxValue > 0){
			LOGGER.info(agent + " - LMCut("+iteration+"):");
			Set<UnaryOperator> lm = computeNextLandmark(state);
			
			if(lm == null){
				LOGGER.info(agent + " hmax == 0 -> break");
				break;
			}
			
			if(lm.isEmpty()){
				LOGGER.warn(agent + " EMPTY LANDMARK!");
				break;
			}
			
			LOGGER.info(agent + " new cut:" + lm);
			
			landmarks.add(lm);
			
			int lmCost = Integer.MAX_VALUE;
			for(UnaryOperator op : lm){
			
				int cost = cf.get(op.actionHash);
				lmCost = cost < lmCost ? cost : lmCost;
				
			}
			for(UnaryOperator op : lm){
				cf.put(op.actionHash,cf.get(op.actionHash)-lmCost);
			}
			
			LOGGER.info(agent + " LM("+iteration+")="+lmCost+": " + lm);
			
			if(lmCost == 0){
				LOGGER.warn(agent + " LANDMARK COST == 0!");
				break;
			}
			
			h += lmCost;
			++ iteration;
		}
		
		LOGGER.info(agent + " - LMCut COMPUTED:" + h);
		
		
		callback.heuristicComputed(new HeuristicResult(h));
	}
	
	public List<Set<UnaryOperator>> getLandmarks(){
		return landmarks;
	}



	@Override
	public void processMessages() {
		// TODO Auto-generated method stub
		
	}

}
