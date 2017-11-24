package cz.agents.dimaptools.landmarks;

import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.landmarks.LMCutHeuristic;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;
import cz.agents.dimaptools.search.DistributedBestFirstSearch;

public class TestLMCutHeuristic extends AbstractDistributedAStarTest {

	@Test
	public void test() {
//		testProblem("truck-a1");
//		testProblem("truck-crane-a2");
//		testProblem("logistics-a2");
		testProblem("logistics-a4");
//		testProblem("deconfliction-a4");
//		testProblem("rovers-a4");
//		testProblem("sokoban-a1");
//		testProblem("sokoban-a2");
	}

	@Override
	public void runSearch(DIMAPWorldInterface world){
		DistributedBestFirstSearch search = new DistributedBestFirstSearch(world);
//		AStar search = new AStar(world.getProblem());

		HeuristicInterface heuristic = new LMCutHeuristic(world.getProblem());

		search.plan(new MapConfiguration("heuristic",heuristic), searchCallback);
	}

}
