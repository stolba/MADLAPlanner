package cz.agents.dimaptools.preprocess;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;

import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.input.addl.ADDLParser;
import cz.agents.dimaptools.input.sas.SASParser;
import cz.agents.dimaptools.input.sas.SASPreprocessor;
import cz.agents.dimaptools.model.Problem;

public class TestPreprocessing {

	private static final Logger LOGGER = Logger.getLogger(TestPreprocessing.class);

	private static final String RESOURCES = "./src/test/resources/";

	@Test
	public void test() {

//		test("truck-a1");

	}

	public void test(String domain){
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

        for(String agent : addl.getAgentList()){
        	LOGGER.info("DOMAIN("+agent+"):");
            LOGGER.info(preprocessor.getDomainForAgent(agent));

            LOGGER.info("PROBLEM("+agent+"):");
            Problem problem = preprocessor.getProblemForAgent(agent);
            LOGGER.info("init: " + problem.initState);
            LOGGER.info("goal: " + problem.goalSuperState);
            LOGGER.info("actions: \n" + problem);
        }
	}

}
