package cz.agents.dimaptools.relaxed;

import org.junit.Test;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.SynchronizedDistributedRelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.DistributedFFEvaluator;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;

public class TestSynchronizedDistributedHeuristic extends AbstractDistributedAStarTest {
	
	private static int run = -1;

	@Test
	public void test() {
//		testProblem("truck-crane-a2");
//		testProblem("truck-crane-factory-a3");
//		testProblem("logistics-a2");
//		testProblem("logistics-a4");
//		testProblem("deconfliction-a4");
//		testProblem("rovers-a4");
//		testProblem("sokoban-a1");
//		testProblem("sokoban-a2");
	}

	@Override
	public void runSearch(final DIMAPWorldInterface world){
		
		final DistributedFFEvaluator ff = new DistributedFFEvaluator(world);
		SynchronizedDistributedRelaxationHeuristic heuristic = new SynchronizedDistributedRelaxationHeuristic(world,ff);

		if(run == -1)run = world.getNumberOfAgents();
		
		heuristic.getHeuristic(world.getProblem().initState, new HeuristicComputedCallback() {
			
			@Override
			public void heuristicComputed(HeuristicResult result) {
				System.out.println(world.getAgentName() + ": " + result.getValue());
				for(int a : ff.getRelaxedPlan().toArray()){
					System.out.println(world.getAgentName() + "   rp: " + world.getProblem().getAction(a) + " - " + a);
				}
				run -= 1;
			}
		});
		
		while(run > 0){
			world.getCommPerformer().performReceiveNonblock();
			heuristic.processMessages();
			Thread.yield();
		}
	}

}
