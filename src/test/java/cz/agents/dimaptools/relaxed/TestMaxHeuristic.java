package cz.agents.dimaptools.relaxed;

import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.MaxEvaluator;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;
import cz.agents.dimaptools.search.DistributedBestFirstSearch;

public class TestMaxHeuristic extends AbstractDistributedAStarTest {

	@Test
	public void test() {
//		testProblem("truck-crane-a2");
//		testProblem("logistics-a2");
//		testProblem("logistics-a4");
//		testProblem("deconfliction-a4");
//		testProblem("rovers-a4");
//		testProblem("sokoban-a1");
//		testProblem("sokoban-a2");
	}

	@Override
	public void runSearch(DIMAPWorldInterface world){
		DistributedBestFirstSearch search = new DistributedBestFirstSearch(world);
//		AStar search = new AStar(problem);

		HeuristicInterface heuristic = new RelaxationHeuristic(world.getProblem(),new MaxEvaluator(world.getProblem()));

		search.plan(new MapConfiguration("heuristic",heuristic), searchCallback);
	}

}
