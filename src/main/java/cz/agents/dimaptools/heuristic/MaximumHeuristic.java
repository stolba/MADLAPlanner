package cz.agents.dimaptools.heuristic;

import cz.agents.dimaptools.model.State;

public class MaximumHeuristic implements HeuristicInterface {
	
	private final HeuristicInterface h1;
	private final HeuristicInterface h2;
	
	float h1val = -1;
	float h2val = -1;
	
	

	public MaximumHeuristic(HeuristicInterface h1, HeuristicInterface h2) {
		super();
		this.h1 = h1;
		this.h2 = h2;
	}

	@Override
	public void getHeuristic(State state, final HeuristicComputedCallback callback) {
		h1.getHeuristic(state, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				if(h2val != -1){
					callback.heuristicComputed(new HeuristicResult(Math.max(h1val,h2val)));
					h1val = -1;
					h2val = -1;
				}else{
					h1val = result.getValueF();
				}
				
			}
		});
		
		h2.getHeuristic(state, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				if(h1val != -1){
					callback.heuristicComputed(new HeuristicResult(Math.max(h1val,h2val)));
					h1val = -1;
					h2val = -1;
				}else{
					h2val = result.getValueF();
				}
				
			}
		});

	}

	@Override
	public void processMessages() {
		// TODO Auto-generated method stub

	}

}
