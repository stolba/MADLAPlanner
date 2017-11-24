package cz.agents.dimaptools.search;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.input.addl.ADDLParser;
import cz.agents.dimaptools.input.sas.SASParser;
import cz.agents.dimaptools.input.sas.SASPreprocessor;
import cz.agents.dimaptools.model.Problem;

public class TestImplicitBellmanFord {

    private static final String RESOURCES = "./src/test/resources/";

    @Test
    public void test() {
//        getProblems("sokoban-a1");
        Map<String,Problem> problems = getProblems("logistics-a2");

		for(String a : problems.keySet()){
			runSearch(problems.get(a));
		}
    }

    public Map<String,Problem> getProblems(String domain){
        String sasFileName = RESOURCES + domain + ".pre";
        String agentFileName = RESOURCES + domain + ".addl";

        File sasFile = new File(sasFileName);
        if (!sasFile.exists()) {
            fail("SAS file " + sasFileName + " does not exist!");
            System.exit(1);
        }

        File agentFile = new File(agentFileName);
        if (!agentFile.exists()) {
            fail("Agent file " + agentFileName + " does not exist!");
            System.exit(1);
        }
        ADDLObject addl = new ADDLParser().parse(agentFile);

        SASParser parser = new SASParser(sasFile);
        SASPreprocessor preprocessor = new SASPreprocessor(parser.getDomain(), addl);

        Map<String,Problem> problems = new HashMap<String,Problem>();
        for(String a : addl.getAgentList()){
            problems.put(a, preprocessor.getProblemForAgent(a));
        }

        return problems;
    }

    public void runSearch(Problem problem){
        ImplicitBellmanFord search = new ImplicitBellmanFord(problem,problem.getAllActions());
        search.plan(new MapConfiguration(), new SearchInterface.SearchCallback() {

            @Override
            public void planNotFound() {
                fail("Plan not found!");
            }

            @Override
            public void planFound(List<String> plan) {
                System.out.println(plan);
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
