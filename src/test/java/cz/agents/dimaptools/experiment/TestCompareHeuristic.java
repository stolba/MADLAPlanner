package cz.agents.dimaptools.experiment;

import org.junit.Test;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;
import cz.agents.dimaptools.search.DistributedBestFirstSearch;

public class TestCompareHeuristic extends AbstractDistributedAStarTest {

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
        new DistributedBestFirstSearch(world);
        world.getProblem();
//		DistributedBestFirstSearch search = new DistributedBestFirstSearch(world);
//		Problem problem = world.getProblem();

//h_dradd vs h_radd
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_dradd",
//				new DistributedRecursiveAdditiveHeuristic(problem),
//				"h_radd",
//				new RecursiveAdditiveHeuristic(problem)
//				);
//
//		HeuristicInterface reqHeuristic = heuristic;

//h_add vs hradd
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_add",
//				new RelaxationHeuristic(problem, new AddEvaluator(problem, false)),
//				"h_radd",
//				new RecursiveAdditiveHeuristic(problem)
//				);
//
//		HeuristicInterface reqHeuristic = heuristic;

//h_add vs h_FF
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_add",
//				new AdditiveHeuristic(problem),
//				"h_ff",
//				new FFHeuristic(problem)
//				);
//
//		HeuristicInterface reqHeuristic = heuristic;

//h_FF vs heuristic.h_FF
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_old_ff",
//				new cz.agents.dimaptools.heuristic.FFHeuristic(problem),
//				"h_ff",
//				new FFHeuristic(problem)
//				);
//
//		HeuristicInterface reqHeuristic = heuristic;

//h_add vs h_dadd
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_add",
//				new AdditiveHeuristic(problem),
//				"h_dadd",
//				new DistributedAdditiveHeuristic(problem,comm,false)
//				);
//
//		HeuristicInterface reqHeuristic = new ComparisonHeuristic(
//				problem,
//				"h_add",
//				new AdditiveHeuristic(problem),
//				"h_dadd",
//				new DistributedAdditiveHeuristic(problem,comm,true)
//				);

//h_add vs h_rdadd
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_add",
//				new AdditiveHeuristic(problem),
//				"h_rdadd",
//				new RecursiveDistributedAdditiveHeuristic(problem,comm,false)
//				);
//
//		HeuristicInterface reqHeuristic = new ComparisonHeuristic(
//				problem,
//				"h_add",
//				new AdditiveHeuristic(problem),
//				"h_rdadd",
//				new RecursiveDistributedAdditiveHeuristic(problem,comm,true)
//				);

//h_dadd vs h_rdadd
//		Communicator commH1 = initCommunicator(comm.getAddress()+"H1");
//		Communicator commH2 = initCommunicator(comm.getAddress()+"H2");
//		HeuristicInterface heuristic = new ComparisonHeuristic(
//				problem,
//				"h_rdadd",
//				new RecursiveDistributedAdditiveHeuristic(problem,commH1,false),
//				"h_dadd",
//				new DistributedAdditiveHeuristic(problem,commH2,false)
//				);
//
//		HeuristicInterface reqHeuristic = new ComparisonHeuristic(
//				problem,
//				"h_rdadd",
//				new RecursiveDistributedAdditiveHeuristic(problem,commH1,true),
//				"h_dadd",
//				new DistributedAdditiveHeuristic(problem,commH2,true)
//				);



//		search.plan(new MapConfiguration("heuristic",heuristic,"requestHeuristic",reqHeuristic), searchCallback);
    }

}
