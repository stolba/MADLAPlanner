package cz.agents.dimaptools.search;

import static org.junit.Assert.fail;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;

public class TestDistributedAStar extends AbstractDistributedAStarTest {

	private final static Logger LOGGER = Logger.getLogger(TestDistributedAStar.class);


	@Test
	public void test() {
//		LOGGER.setLevel(Level.INFO);
//		testProblem("rovers-a4");
//		testProblem("sokoban-a2");
//		testProblem("logistics-a2");
	}

	@Override
	public void runSearch(DIMAPWorldInterface world){
		DistributedBestFirstSearch search = new DistributedBestFirstSearch(world);
		search.plan(new MapConfiguration(), new SearchInterface.SearchCallback() {

			@Override
			public void planNotFound() {
				fail("Plan not found!");
			}

			@Override
			public void planFound(List<String> plan) {
				LOGGER.info(plan);
			}

			@Override
			public void planFoundByOther() {

			}
			
			@Override
			public void partialPlanReconstructed(List<String> plan, String initiator, int solutionCost) {
				System.out.println("partial plan:\n"+plan);
			}
		});
	}


}
