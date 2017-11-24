package cz.agents.dimaptools.multiheuristic;

import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;
import cz.agents.dimaptools.search.HeuristicOpenList;
import cz.agents.dimaptools.search.MultiheuristicDistributedBestFirstSearch;

public class TestFFHeuristicM extends AbstractDistributedAStarTest {

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
		MultiheuristicDistributedBestFirstSearch search = new MultiheuristicDistributedBestFirstSearch(world);

		HeuristicOpenList hFF = new HeuristicOpenList("hFF",true,new RelaxationHeuristic(world.getProblem(),new FFEvaluator(world.getProblem())),false);

		search.plan(new MapConfiguration("hFF",hFF), searchCallback);
	}

}
