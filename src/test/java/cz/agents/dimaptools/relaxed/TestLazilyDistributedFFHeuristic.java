package cz.agents.dimaptools.relaxed;

import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.heuristic.relaxed.LazilyDistributedFFHeuristic;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;
import cz.agents.dimaptools.search.DistributedBestFirstSearch;

public class TestLazilyDistributedFFHeuristic extends AbstractDistributedAStarTest {

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
    public void runSearch(DIMAPWorldInterface world){
        DistributedBestFirstSearch search = new DistributedBestFirstSearch(world);
//		AStar search = new AStar(problem);

        LazilyDistributedFFHeuristic heuristic = new LazilyDistributedFFHeuristic(world,false);
        LazilyDistributedFFHeuristic reqHeuristic = new LazilyDistributedFFHeuristic(world,true);
        heuristic.setOtherProtocol(reqHeuristic.getProtocol());
        reqHeuristic.setOtherProtocol(heuristic.getProtocol());

        search.plan(new MapConfiguration("heuristic",heuristic,"requestHeuristic",reqHeuristic), searchCallback);
    }

}
