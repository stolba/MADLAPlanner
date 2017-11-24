package cz.agents.dimaptools.search;

import static org.junit.Assert.fail;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.PerformerCommunicator;
import cz.agents.alite.communication.QueuedCommunicator;
import cz.agents.alite.communication.channel.CommunicationChannelException;
import cz.agents.alite.communication.channel.DirectCommunicationChannel.DefaultReceiverTable;
import cz.agents.alite.communication.channel.DirectCommunicationChannel.ReceiverTable;
import cz.agents.alite.communication.channel.DirectCommunicationChannelAsync;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.DefaultDIMAPWorld;
import cz.agents.dimaptools.communication.protocol.DefaultEncoder;
import cz.agents.dimaptools.communication.protocol.DistributedHeuristicProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedProblemInfoSharingProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedRPExtractionProtocol;
import cz.agents.dimaptools.communication.protocol.DistributedSearchProtocol;
import cz.agents.dimaptools.communication.protocol.SynchronizedDistributedHeuristicProtocol;
import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.input.addl.ADDLParser;
import cz.agents.dimaptools.input.sas.SASParser;
import cz.agents.dimaptools.input.sas.SASPreprocessor;
import cz.agents.dimaptools.model.Problem;

public abstract class AbstractDistributedAStarTest {

    private final static Logger LOGGER = Logger.getLogger(AbstractDistributedAStarTest.class);

    private static final String RESOURCES = "./src/test/resources/";

    private final ReceiverTable receiverTable = new DefaultReceiverTable();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final Set<Thread> threadSet = new LinkedHashSet<Thread>();



    abstract public void runSearch(DIMAPWorldInterface world);


    public SearchInterface.SearchCallback searchCallback = new SearchInterface.SearchCallback() {

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
            System.out.println("plan found by other agent");
        }

        @Override
        public void partialPlanReconstructed(List<String> plan, String initiator, int solutionCost) {
            System.out.println("partial plan:\n"+plan);
        }
    };


    @SuppressWarnings("deprecation")
    public void testProblem(String problem){
        final Map<String,Problem> problems = getProblems(problem);


        final Thread mainThread = Thread.currentThread();
        final UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.fatal("Inner error: ", e);
                mainThread.interrupt();
            }

        };

        for (final String a : problems.keySet()) {

            final DIMAPWorldInterface world = new DefaultDIMAPWorld(
                    a,
                    initCommunicator(a),
                    new DefaultEncoder(),
                    problems.get(a),
                    null,
                    problems.size());

            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    runSearch(world);
                }
            });

            thread.setUncaughtExceptionHandler(exceptionHandler);
            threadSet.add(thread);
            thread.start();
        }

        try {
            for (Thread thread : threadSet) {
                thread.join();
            }
        } catch (InterruptedException e) {
            LOGGER.debug("Main thread interrupted.");
        }

        for (Thread thread : threadSet) {
            if (thread.isAlive()) {
                // TODO: refactor using a stop flag
                thread.stop();
            }
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



    public PerformerCommunicator initCommunicator(String address){
        QueuedCommunicator communicator = new QueuedCommunicator(address);
        try {
//        	communicator.handleMessageClass(Object.class); //TODO: works but not very well

            DistributedSearchProtocol.registerClasses(communicator);
            DistributedHeuristicProtocol.registerClasses(communicator);
            DistributedProblemInfoSharingProtocol.registerClasses(communicator);
            SynchronizedDistributedHeuristicProtocol.registerClasses(communicator);
            DistributedRPExtractionProtocol.registerClasses(communicator);

            communicator.addChannel(new DirectCommunicationChannelAsync(communicator, receiverTable, executorService));
        } catch (CommunicationChannelException e) {
            LOGGER.fatal("Communication channel creation error!", e);
            System.exit(1);
        }

        return communicator;
    }

}
