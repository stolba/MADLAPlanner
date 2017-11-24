package cz.agents.dimaptools.experiment;

import org.apache.log4j.Logger;

import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

public class ComparisonHeuristic implements HeuristicInterface {

	private final Logger LOGGER;

	private final String h1id;
	private final HeuristicInterface h1;
	private final String h2id;
	private final HeuristicInterface h2;

	private long startH1;
	private long timeH1;
	private long startH2;
	private long timeH2;


	//TODO: statistics, precise timer, n-heuristic comparison?



	public ComparisonHeuristic(Problem problem, String h1id, HeuristicInterface h1, String h2id,HeuristicInterface h2) {
		super();
		this.h1id = h1id;
		this.h1 = h1;
		this.h2id = h2id;
		this.h2 = h2;

		LOGGER = Logger.getLogger(problem.agent + "." + ComparisonHeuristic.class);
	}

	@Override
	public void getHeuristic(State state, final HeuristicComputedCallback callback) {
		Aggregator agg = new Aggregator(callback);
		startH1 = System.nanoTime();
		h1.getHeuristic(state, agg.cbck1);
		startH2 = System.nanoTime();
		h2.getHeuristic(state, agg.cbck2);
	}

	@Override
	public void processMessages() {
		h1.processMessages();
		h2.processMessages();
	}

	private class Aggregator{
		int h1 = -1;
		int h2 = -1;

		final HeuristicComputedCallback cbck;

		public HeuristicComputedCallback cbck1 = new HeuristicComputedCallback(){

			@Override
			public void heuristicComputed(HeuristicResult result) {
				timeH1 = (System.nanoTime() - startH1)/1000;
				h1 = result.getValue();
				if(h2 != -1){
					if(h1!=h2)LOGGER.info(h1id+"="+h1+"("+timeH1+"), "+h2id+"="+h2+"("+timeH2+")");
//					LOGGER.info(h1id+"="+h1+"("+timeH1+"), "+h2id+"="+h2+"("+timeH2+")");
					cbck.heuristicComputed(new HeuristicResult(Math.max(h1, h2)));
				}
			}

		};

		public HeuristicComputedCallback cbck2 = new HeuristicComputedCallback(){

			@Override
			public void heuristicComputed(HeuristicResult result) {
				timeH2 = (System.nanoTime() - startH2)/1000;
				h2 = result.getValue();
				if(h1 != -1){
					if(h1!=h2)LOGGER.info(h1id+"="+h1+"("+timeH1+"), "+h2id+"="+h2+"("+timeH2+")");
//					LOGGER.info(h1id+"="+h1+"("+timeH1+"), "+h2id+"="+h2+"("+timeH2+")");
					cbck.heuristicComputed(new HeuristicResult(Math.max(h1, h2)));
				}
			}

		};

		public Aggregator(HeuristicComputedCallback cbck) {
			super();
			this.cbck = cbck;
		}


	}

}
