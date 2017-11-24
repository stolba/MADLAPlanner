package cz.agents.dimaptools.input.sas;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cz.agents.dimaptools.heuristic.abstractions.MergeAndShrink.ShrinkStrategy;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.search.TestAStar;

public class TestWriter extends TestAStar {
	
	private static final String RESOURCES = "./src/test/resources/";

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
			testWriterOnProjection(a, problems.get(a));
		}
		
	}
	
	public void testWriterOnProjection(String agent, Problem problem){
		System.out.println("--- AGENT: " + agent + " ---");
		
		SASDomain sasDom = new SASDomain(problem.getDomain(), problem, agent, problem.getAllActions(),10);
		
		SASWriter writer = new SASWriter(sasDom);
		System.out.println(writer.generate());
		
		
	}

}
