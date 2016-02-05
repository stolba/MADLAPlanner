package cz.agents.madla.creator;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.PerformerCommunicator;
import cz.agents.alite.communication.QueuedCommunicator;
import cz.agents.alite.communication.channel.CommunicationChannelException;
import cz.agents.alite.communication.channel.DirectCommunicationChannel.DefaultReceiverTable;
import cz.agents.alite.communication.channel.DirectCommunicationChannel.ReceiverTable;
import cz.agents.alite.communication.channel.DirectCommunicationChannelAsync;
import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.alite.creator.Creator;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.DefaultDIMAPWorld;
import cz.agents.dimaptools.communication.protocol.DefaultEncoder;
import cz.agents.dimaptools.experiment.LPDataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.potential.DistributedPotentialLPGenerator;
import cz.agents.dimaptools.heuristic.potential.PotentialHeuristic;
import cz.agents.dimaptools.heuristic.potential.PotentialLPGenerator;
import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.input.addl.ADDLParser;
import cz.agents.dimaptools.input.sas.SASParser;
import cz.agents.dimaptools.input.sas.SASPreprocessor;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.lp.SolverCPLEX;
import cz.agents.dimaptools.lp.SolverLPSolve;
import cz.agents.madla.executor.PlanTestExecutor;
import cz.agents.madla.planner.Planner;

public class PotentialHeuristicEvaluatorCreator implements Creator {

    private final static Logger LOGGER = Logger.getLogger(PotentialHeuristicEvaluatorCreator.class);


    private static final String TRANSLATOR = "./misc/fd/src/translate/translate.py";
    private static final String PREPROCESSOR = "./preprocess-runner";
    private static final String RESOURCES = "./";
    private static final String OUTPUT = "./out.csv";
    
    private final ReceiverTable receiverTable = new DefaultReceiverTable();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final Set<HeuristicAgent> agentSet = new LinkedHashSet<HeuristicAgent>();
    private final Set<Thread> threadSet = new LinkedHashSet<Thread>();

    private boolean fromSAS = true;
    private boolean global = false;

    private String domainFileName;
    private String problemFileName;
    private String sasFileName;
    private String agentFileName;
    private String solverID;

    
    private LPSolver solver;



    protected SASPreprocessor preprocessor;


    @Override
    public void init(String[] args) {

        for(int i=0;i<args.length;i++){
            System.out.println(args[i]);
        }

        if (args.length != 4 && args.length != 5) {
            System.out.println("provided args: " + Arrays.toString(args));
            System.out.println("Usage (from SAS): <output>.sas <agents>.addl <add|max|FF> <recursionLevel> <time limit (min)>\nUsage (from PDDL): <domain>.pddl <problem>.pddl <agents>.addl <add|max|FF> <recursionLevel> <time limit (min)>");
            System.exit(1);
        }

        if (args.length == 4 && args[2].contains("addl")){
        	fromSAS = true;
        	global = false;
            sasFileName = args[1];
            agentFileName = args[2];
            solverID = args[3];
        }else{
        	fromSAS = false;
        	global = true;
            sasFileName = "output";
            domainFileName = args[1];
            problemFileName = args[2];
            solverID = args[3];
        }

        if (args.length == 5){
        	fromSAS = false;
        	global = false;
            sasFileName = "output";
            domainFileName = args[1];
            problemFileName = args[2];
            agentFileName = args[3];
            solverID = args[4];
        }

        Trace.setFileStream("log/trace.log");
    }



    @Override
    public void create() {

        LOGGER.info(">>> CREATION");
        LOGGER.info(">>>   sas: " + sasFileName);
        
        
        ADDLObject addl;
        
        if(agentFileName == null){
        	LOGGER.info(">>>   no agents here");

        	List<String> dummyAgents = new LinkedList<>();
        	dummyAgents.add("");
        	addl = new ADDLObject(dummyAgents); // new ADDLParser().parse(agentFile);
        
        }else{
        	LOGGER.info(">>>   addl: " + agentFileName);
        	File agentFile = new File(RESOURCES + agentFileName);
            if (!agentFile.exists()) {
                LOGGER.fatal("Agent file " + RESOURCES + agentFileName + " does not exist!");
                System.exit(1);
            }
        	addl = new ADDLParser().parse(agentFile);
        }
        
        if(domainFileName == null) domainFileName = sasFileName;
        if(problemFileName == null) problemFileName = sasFileName;

        LPDataAccumulator.startNewAccumulator(domainFileName, problemFileName);
        LPDataAccumulator.getAccumulator().agents = addl.getAgentCount();
        LPDataAccumulator.getAccumulator().setOutputFile(OUTPUT);
        LPDataAccumulator.getAccumulator().global = global;
        LPDataAccumulator.getAccumulator().solver = solverID;
        
        if(solverID.equals("lpsolve")){
        	solver = new SolverLPSolve();
        }else if(solverID.equals("cplex")){
        	solver = new SolverCPLEX();
        }else if(solverID.equals("cplex-dist")){
        	solver = new SolverCPLEX();
        }

        if(!fromSAS){
//                Timer.setTranslationTime(System.nanoTime());
            runTranslate();
//                Timer.setTranslationTime(System.nanoTime()-Timer.getTranslationTime());
//                Timer.setPreprocessingTime(System.nanoTime());
            runPreprocess();
//                Timer.setPreprocessingTime(System.nanoTime()-Timer.getPreprocessingTime());
        }

        File sasFile = new File(sasFileName);
        if (!sasFile.exists()) {
            LOGGER.fatal("SAS file " + sasFileName + " does not exist!");
            System.exit(1);
        }

        SASParser parser = new SASParser(sasFile);
        preprocessor = new SASPreprocessor(parser.getDomain(), addl, new MapConfiguration("unitCost",!parser.isMetric()));


//            Timer.setCreatEntetiesTime(System.nanoTime());
        createEntities(addl);
//            Timer.setCreatEntetiesTime(System.nanoTime()-Timer.getCreatEntetiesTime());
//            Timer.setRunEntetiesTime(System.nanoTime());
        
        if(solverID.equals("cplex-dist")){
        	prepareAndSolveLPsDist();
        }else{
        	prepareAndSolveLPs();
        }
        
        
        runEntities();
//            Timer.setRunEntetiesTime(System.nanoTime()-Timer.getRunEntetiesTime());

//        writeOutput();

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOGGER.warn("Shutdown interrupted!");
        }
    }


    




    private void runTranslate(){
        try {
            String cmd = TRANSLATOR + " " + RESOURCES+domainFileName + " " + RESOURCES+problemFileName;
            LOGGER.info("RUN: " + cmd);
            Process pr = Runtime.getRuntime().exec(cmd);

            pr.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void runPreprocess() {
        try {
            String cmd = PREPROCESSOR;
            LOGGER.info("RUN: " + cmd);
            Process pr = Runtime.getRuntime().exec(cmd);
            pr.waitFor();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void createEntities(ADDLObject addl) {
        LOGGER.info(">>> ENTITIES CREATION");

        PlanTestExecutor executor = new PlanTestExecutor();

        for (String agentName: addl.getAgentList()) {

            DIMAPWorldInterface world = initWorld(agentName,addl.getAgentCount());

            PotentialHeuristic hpot = new PotentialHeuristic();
            
            agentSet.add(new HeuristicAgent(agentName,hpot,world));

            executor.addProblem(world.getProblem());
        }

        executor.setInitAndGoal(preprocessor.getGlobalInit(), preprocessor.getGlobalGoal());

    }
    
    private void prepareAndSolveLPs() {
        LOGGER.info(">>> LP SOLUTION");
        
        for (final HeuristicAgent agent : agentSet) {
        	PotentialLPGenerator potgen = new PotentialLPGenerator(agent.world,solver,LPDataAccumulator.getAccumulator().domain,LPDataAccumulator.getAccumulator().problem,agent.agentName);
            potgen.generateAndSolvePotentialLP(agent.hpot);
        }

        

    }
    
    private void prepareAndSolveLPsDist() {
        LOGGER.info(">>> LP SOLUTION");
        
        HashMap<String,Float> publicPots = new HashMap<String,Float>();
        
        for (final HeuristicAgent agent : agentSet) {
        	DistributedPotentialLPGenerator potgen = new DistributedPotentialLPGenerator(agent.world,solver,LPDataAccumulator.getAccumulator().domain,LPDataAccumulator.getAccumulator().problem,agent.agentName);
            potgen.setPublicPots(publicPots);
        	potgen.generateAndSolvePotentialLP(agent.hpot);
        	publicPots = potgen.getPublicPots();
        }

        

    }


    public PerformerCommunicator initQueuedCommunicator(String address){
        QueuedCommunicator communicator = new QueuedCommunicator(address);
        try {

            communicator.handleMessageClass(Object.class);

            communicator.addChannel(new DirectCommunicationChannelAsync(communicator, receiverTable, executorService));
        } catch (CommunicationChannelException e) {
            LOGGER.fatal("Communication channel creation error!", e);
            System.exit(1);
        }

        return communicator;
    }

    public DIMAPWorldInterface initWorld(String agentName, int totalAgents){

        return new DefaultDIMAPWorld(
                agentName,
                initQueuedCommunicator(agentName),
                new DefaultEncoder(),
                preprocessor.getProblemForAgent(agentName),
                preprocessor.getDomainForAgent(agentName),
                totalAgents
                );
    }


    @SuppressWarnings("deprecation")
    private void runEntities() {
        LOGGER.info(">>> ENTITIES RUNNING");


        final Thread mainThread = Thread.currentThread();
        final UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.error("Uncaught exception in agent/planner thread!", e);

                LPDataAccumulator.getAccumulator().writeOutput(Planner.FORCE_EXIT_AFTER_WRITE);

                

                mainThread.interrupt();
            }

        };

        for (final HeuristicAgent agent : agentSet) {
        	agent.run();    
        }

        
        
        LPDataAccumulator.getAccumulator().initMaxSumHeuristic = LPDataAccumulator.getAccumulator().initPubMaxHeuristic + LPDataAccumulator.getAccumulator().initPrivSumHeuristic;
        LPDataAccumulator.getAccumulator().initMinSumHeuristic = LPDataAccumulator.getAccumulator().initPubMinHeuristic + LPDataAccumulator.getAccumulator().initPrivSumHeuristic;
        LPDataAccumulator.getAccumulator().initMinMaxHeuristic = LPDataAccumulator.getAccumulator().initPubMinHeuristic + LPDataAccumulator.getAccumulator().initPrivMaxHeuristic;
        LPDataAccumulator.getAccumulator().writeOutput(Planner.FORCE_EXIT_AFTER_WRITE);
    }
    
    
    public class HeuristicAgent{
    	public final String agentName;
    	public final PotentialHeuristic hpot;
    	public final DIMAPWorldInterface world;
    	
		public HeuristicAgent(String agentName, PotentialHeuristic hpot,
				DIMAPWorldInterface world) {
			super();
			this.agentName = agentName;
			this.hpot = hpot;
			this.world = world;
		}

		public void run() {
			final float pubH = hpot.getPublicHeuristic(world.getProblem().initState);
			final float privH = hpot.getPrivateHeuristic(world.getProblem().initState);
			
			LOGGER.info("writing agent " + agentName);
			LPDataAccumulator.getAccumulator().note += "; "+agentName+":(pubH:"+pubH+";privH:"+privH+";status:"+hpot.getStatus()+")";
			
			LPDataAccumulator.getAccumulator().initPubMaxHeuristic = Math.max(LPDataAccumulator.getAccumulator().initPubMaxHeuristic, pubH);
			LPDataAccumulator.getAccumulator().initPubMinHeuristic = Math.min(LPDataAccumulator.getAccumulator().initPubMinHeuristic, pubH);
			LPDataAccumulator.getAccumulator().initPrivSumHeuristic += privH;
			LPDataAccumulator.getAccumulator().initPrivMaxHeuristic = Math.max(LPDataAccumulator.getAccumulator().initPrivMaxHeuristic, privH);
			LPDataAccumulator.getAccumulator().initMaxProjHeuristic = Math.max(pubH+privH,LPDataAccumulator.getAccumulator().initMaxProjHeuristic);
			LPDataAccumulator.getAccumulator().initMinProjHeuristic = Math.min(pubH+privH,LPDataAccumulator.getAccumulator().initMinProjHeuristic);
			LPDataAccumulator.getAccumulator().initSumHeuristic = LPDataAccumulator.getAccumulator().initSumHeuristic + privH + pubH;
			
			hpot.getHeuristic(world.getProblem().initState, new HeuristicComputedCallback() {
				
				@Override
				public void heuristicComputed(HeuristicResult result) {
					if(result.getValue() != pubH+privH){
						LOGGER.error("privH:"+privH+" + pubH:"+pubH+" != h:"+result.getValue()+"!!!");
						LPDataAccumulator.getAccumulator().note += "; "+agentName+":error("+"privH:"+privH+" + pubH:"+pubH+" != h:"+result.getValue()+"!!!"+")";
						
					}
				}
			});
		}
    }


}
