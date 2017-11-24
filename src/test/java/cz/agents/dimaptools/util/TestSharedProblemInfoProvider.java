package cz.agents.dimaptools.util;

import org.junit.Test;

import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.search.AbstractDistributedAStarTest;

public class TestSharedProblemInfoProvider extends AbstractDistributedAStarTest {

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
	public void runSearch(DIMAPWorldInterface world) {
		SharedProblemInfoProvider provider = new SharedProblemInfoProvider(world,world.getNumberOfAgents());
		provider.sendInfoAndWait();
		
		System.out.println(world.getAgentName() + ":");
		for(String agent : provider.getKnownAgents()){
			System.out.println("  " + agent + ": " + provider.getCoupling(agent));
		}
		
	}

}
