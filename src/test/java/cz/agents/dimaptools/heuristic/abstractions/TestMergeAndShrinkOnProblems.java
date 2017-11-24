package cz.agents.dimaptools.heuristic.abstractions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cz.agents.dimaptools.heuristic.abstractions.MergeAndShrink.ShrinkStrategy;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.search.TestAStar;

public class TestMergeAndShrinkOnProblems extends TestAStar {

	@Test
	public void test() {
//		testProblem("truck-a1");
//		testProblem("truck-crane-a2");
		testProblem("logistics-a2");
//		testProblem("logistics-a4");
//		testProblem("deconfliction-a4");
//		testProblem("rovers-a4");
//		testProblem("sokoban-a1");
//		testProblem("sokoban-a2");
	}

	
	public void testProblem(String problem){
		
		Map<String,Problem> problems = getProblems(problem);

		for(String a : problems.keySet()){
			testAtomicProjections(a, problems.get(a));
		}
		
	}
	
	public void testAtomicProjections(String agent, Problem problem){
		System.out.println("--- AGENT: " + agent + " ---");
		System.out.println("INIT: " + problem.initState);
		
		MergeAndShrink ms = new MergeAndShrink(problem,6);
		ms.mergeAndShrink(ShrinkStrategy.EQUAL_G);
		
		ms.getAbstraction().resetSearchStats();
		Dijkstra d = new Dijkstra();
		float dist = d.search(ms.getAbstraction().getInit());
		
		System.out.println("COST: " + dist);
		System.out.println("PLAN: " + d.getShortestPath());
		
	}

}
