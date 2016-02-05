package cz.agents.madla.creator;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
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
import cz.agents.dimaptools.costpart.ApproximateOCPGenerator;
import cz.agents.dimaptools.costpart.CPTest;
import cz.agents.dimaptools.costpart.CostPartitioningGeneratorInterface;
import cz.agents.dimaptools.costpart.FixedOptimalCPGenerator;
import cz.agents.dimaptools.costpart.LPBasedCPInterface;
import cz.agents.dimaptools.costpart.LPOCPGenerator;
import cz.agents.dimaptools.costpart.ProjectionCompensatingCPGenerator;
import cz.agents.dimaptools.costpart.SEQCostPartitioningGenerator;
import cz.agents.dimaptools.costpart.SEQNegCostPartitioningGenerator;
import cz.agents.dimaptools.costpart.UniformCPGenerator;
import cz.agents.dimaptools.experiment.CPDataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.heuristic.MaximumHeuristic;
import cz.agents.dimaptools.heuristic.potential.PotentialHeuristic;
import cz.agents.dimaptools.heuristic.potential.PotentialLPGenerator;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.MaxEvaluator;
import cz.agents.dimaptools.heuristic.seq.DistributedSEQCostPartLPGenerator;
import cz.agents.dimaptools.heuristic.seq.SEQCostPartLPGenerator;
import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.input.addl.ADDLParser;
import cz.agents.dimaptools.input.sas.SASParser;
import cz.agents.dimaptools.input.sas.SASPreprocessor;
import cz.agents.dimaptools.lp.LPSolver;
import cz.agents.dimaptools.lp.SolverCPLEX;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.search.AStar;
import cz.agents.dimaptools.search.BreadthFS;
import cz.agents.dimaptools.search.ExhaustiveSearch;
import cz.agents.dimaptools.search.SearchInterface;
import cz.agents.dimaptools.search.SearchInterface.SearchCallback;
import cz.agents.madla.executor.PlanTestExecutor;
import cz.agents.madla.planner.ExternalOptimalPlanner;
import cz.agents.madla.planner.Planner;

public class CostPartitioningEvaluatorCreator implements Creator {

    private final static Logger LOGGER = Logger.getLogger(CostPartitioningEvaluatorCreator.class);


    private static final String TRANSLATOR = "./misc/fd/src/translate/translate.py";
    private static final String PREPROCESSOR = "./preprocess-runner";
    private static final String RESOURCES = "./";
    private static final String OUTPUT = "./out.csv";
    
    private final ReceiverTable receiverTable = new DefaultReceiverTable();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final Set<CostPartitioningAgent> agentSet = new LinkedHashSet<CostPartitioningAgent>();
    private final Set<Thread> threadSet = new LinkedHashSet<Thread>();
    
    private PlanTestExecutor executor = new PlanTestExecutor();

    private boolean fromSAS = true;
    private boolean global = false;

    private String domainFileName;
    private String problemFileName;
    private String sasFileName;
    private String agentFileName;
    private String solverID;

    
    private LPSolver solver;
    private ExternalOptimalPlanner optiplan = new ExternalOptimalPlanner();



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

        CPDataAccumulator.startNewAccumulator(domainFileName, problemFileName);
        CPDataAccumulator.getAccumulator().agents = addl.getAgentCount();
        CPDataAccumulator.getAccumulator().setOutputFile(OUTPUT);
        CPDataAccumulator.getAccumulator().global = global;
        CPDataAccumulator.getAccumulator().solver = solverID;
        
        solver = new SolverCPLEX();
        

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
        
        if(solverID.equals("cplex-dist")){	//uses solution of distributed LP to generate CP, uniform when val=0
        	prepareAndSolveLPsDist(false,false);
        }else if(solverID.equals("cplex-dist-pc")){  //uses solution of distributed LP to generate CP, projection-compensation when val=0
        	prepareAndSolveLPsDist(false,true);
        }else if(solverID.equals("cplex")){ //computes SEQ LP and solution per projection (no cost-partitioning)
        	prepareAndSolveLPs();
        }else if(solverID.equals("cplex-dist-neg")){ //uses solution of distributed LP to generate CP, generates general CP
        	prepareAndSolveLPsDist(true,false);
        }else if(solverID.equals("exact-ocp")){  //solve exact ocp using occurences of actions in plans. WARNING: not a CP!
        	solveExactOCP();
        }else if(solverID.equals("exact-ocp-lp")){
        	solveExactOCPUsingLP(false,false);
        }else if(solverID.equals("exact-ocp-lp-ao")){
        	solveExactOCPUsingLP(true,false);
        }else if(solverID.equals("exact-ocp-lp-a")){
        	solveExactOCPUsingLP(false,true);
        }else if(solverID.equals("approx-ocp")){  //approximate exact ocp using occurences of actions in SEQ. WARNING: not a CP?
        	solveApproxOCP();
        }else{
        	prepareCPDist(solverID); //no LP-solver used, "uniform" - uniform CP, "projcom" - projection compensating CP
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
            
            agentSet.add(new CostPartitioningAgent(agentName,hpot,world));

            executor.addProblem(world.getProblem());
        }

        executor.setInitAndGoal(preprocessor.getGlobalInit(), preprocessor.getGlobalGoal());

    }
    
    private void prepareAndSolveLPs() {
        LOGGER.info(">>> LP SOLUTION");
        
        float sum = 0;
        float max = 0;
        
        
        for (final CostPartitioningAgent agent : agentSet) {
        	SEQCostPartLPGenerator cpgen = new SEQCostPartLPGenerator(agent.world,solver,CPDataAccumulator.getAccumulator().domain,CPDataAccumulator.getAccumulator().problem,agent.agentName);
            float heur = cpgen.generateAndSolveCPLP();
            
            agent.plan("A*","hpot");
            
        	System.out.println(agent.agentName + " SEQ heur: " + heur);
        	System.out.println(agent.agentName + " optimal: " + agent.cost);
        	sum += agent.cost;
        	max = Math.max(max, agent.cost);
        	
            if(agentSet.size()==1){
            	CPDataAccumulator.getAccumulator().globalEstimate = heur;
            	CPDataAccumulator.getAccumulator().globalOptimum = agent.cost;
            }else{
            	CPDataAccumulator.getAccumulator().note += agent.agentName+":heur="+heur+" opt="+agent.cost+";";
            }
        }

        CPDataAccumulator.getAccumulator().maxOfLocalOptimums = max;
        CPDataAccumulator.getAccumulator().sumOfLocalOptimums = sum;
        CPDataAccumulator.getAccumulator().isCP = false;

    }
    
    private void solveExactOCP() {
        LOGGER.info(">>> SOLVE EXACT OCP");
        
//        OptimalCPGenerator cpgen = new OptimalCPGenerator();
        FixedOptimalCPGenerator cpgen = new FixedOptimalCPGenerator();
        CPTest test = new CPTest();
        
        for (final CostPartitioningAgent agent : agentSet) {
        	
        	LOGGER.info("> FIND optimal solution of projection for agent " + agent.agentName);
            agent.plan("A*","hpot");
            List<Action> plan = executor.getActionPlan();
//            LOGGER.info("Optimal action plan:\n"+plan);
            cpgen.setProblem(agent.agentName, agent.world.getProblem());
            cpgen.addPlan(plan,agent.agentName);
            test.addOriginalProblem(agent.world.getProblem());
        	
        }
        
        LOGGER.info(">>> PLAN USING EXACT OCP");
        
        float sum = 0;
        
        for (final CostPartitioningAgent agent : agentSet) {
        	
        	LOGGER.info("> apply cost-partitioning for agent " + agent.agentName);
            
        	cpgen.updateCosts(agent.world.getProblem());
        	test.addCPProblem(agent.world.getProblem());
        	
        	LOGGER.info("> FIND optimal solution of cost-partitioned projection for agent " + agent.agentName);
            agent.plan("A*","hpot");
            
        	System.out.println(agent.agentName + " optimal: " + agent.cost);
        	sum += agent.cost;
        	
            CPDataAccumulator.getAccumulator().note += agent.agentName+":opt="+agent.cost+";";
            
        }

        CPDataAccumulator.getAccumulator().sumOfLocalOptimums = sum;
        
        LOGGER.info(">>> TEST CP PROPERTY");
        
        boolean cp = test.testCP();
        CPDataAccumulator.getAccumulator().isCP = cp;
        
        if(cp){
        	LOGGER.info(">>> OK");
        }else{
        	LOGGER.info(">>> NOT A COST PARTITIONING!");
        }

    }
    
    private void solveExactOCPUsingLP(boolean findAllOptimalPlans,boolean findAllPlans) {
        LOGGER.info(">>> SOLVE EXACT OCP USING LP");
        
        LPOCPGenerator cpgen = new LPOCPGenerator(solver);
        CPTest test = new CPTest();
        
        for (final CostPartitioningAgent agent : agentSet) {
        	
        	LOGGER.info("> FIND optimal solution of projection for agent " + agent.agentName);
        	if(findAllPlans){
        		agent.plan("ES","none",!findAllPlans);
        	}else{
        		agent.plan("A*","hpot",!findAllOptimalPlans);
        	}
            cpgen.setProblem(agent.agentName, agent.world.getProblem());
            
            for(List<Action> plan : agent.plans){
            	cpgen.addPlan(plan,agent.agentName);
            }
            
        	test.addOriginalProblem(agent.world.getProblem());
        }
        
        LOGGER.info(">>> SOLVE EXACT OCP LP");
        
        cpgen.solveOCPLP();
        CPDataAccumulator.getAccumulator().globalEstimate = cpgen.getSolution().getObjctiveValue();
        
        LOGGER.info(">>> PLAN USING EXACT OCP");
        
        float sum = 0;
        
        for (final CostPartitioningAgent agent : agentSet) {
        	
        	LOGGER.info("> apply cost-partitioning for agent " + agent.agentName);
            
        	cpgen.updateCosts(agent.world.getProblem());
        	test.addCPProblem(agent.world.getProblem());
        	
        	LOGGER.info("> FIND optimal solution of cost-partitioned projection for agent " + agent.agentName);
            agent.plan("ES","none");
            
            LOGGER.info("Optimal action plan:\n"+executor.getActionPlan());
            
        	System.out.println(agent.agentName + " optimal: " + agent.cost);
        	sum += agent.cost;
        	
            CPDataAccumulator.getAccumulator().note += agent.agentName+":opt="+agent.cost+",plans="+agent.plans.size()+";";
            
        }

        CPDataAccumulator.getAccumulator().sumOfLocalOptimums = sum;
        
        LOGGER.info(">>> TEST CP PROPERTY");
        
        boolean cp = test.testCP();
        CPDataAccumulator.getAccumulator().isCP = cp;
        
        if(cp){
        	LOGGER.info(">>> OK");
        }else{
        	LOGGER.info(">>> NOT A COST PARTITIONING!");
        }

    }
    
    private void solveApproxOCP() {
        LOGGER.info(">>> SOLVE APPROX OCP");
        
        ApproximateOCPGenerator cpgen = new ApproximateOCPGenerator();
        CPTest test = new CPTest();
        
        for (final CostPartitioningAgent agent : agentSet) {
        	
        	SEQCostPartLPGenerator lpgen = new SEQCostPartLPGenerator(agent.world,solver,CPDataAccumulator.getAccumulator().domain,CPDataAccumulator.getAccumulator().problem,agent.agentName);
            float heur = lpgen.generateAndSolveCPLP();
            
            cpgen.setProblem(agent.agentName, agent.world.getProblem());
            cpgen.setLPSolution(lpgen.getSolution(),agent.agentName);
            test.addOriginalProblem(agent.world.getProblem());
        	
        }
        
        LOGGER.info(">>> PLAN USING APPROX OCP");
        
        float sum = 0;
        
        for (final CostPartitioningAgent agent : agentSet) {
            
        	cpgen.updateCosts(agent.world.getProblem());
        	test.addCPProblem(agent.world.getProblem());
        	
            agent.plan("A*","hpot");
            
        	System.out.println(agent.agentName + " optimal: " + agent.cost);
        	sum += agent.cost;
        	
            CPDataAccumulator.getAccumulator().note += agent.agentName+":opt="+agent.cost+";";
            
        }

        CPDataAccumulator.getAccumulator().sumOfLocalOptimums = sum;

        LOGGER.info(">>> TEST CP PROPERTY");
        
        boolean cp = test.testCP();
        CPDataAccumulator.getAccumulator().isCP = cp;
        
        if(cp){
        	LOGGER.info(">>> OK");
        }else{
        	LOGGER.info(">>> NOT A COST PARTITIONING!");
        }

    }
    
    private void prepareAndSolveLPsDist(boolean negative, boolean compensate) {
        LOGGER.info(">>> LP SOLUTION");
        
        DistributedSEQCostPartLPGenerator cplp = new DistributedSEQCostPartLPGenerator(solver, CPDataAccumulator.getAccumulator().domain,CPDataAccumulator.getAccumulator().problem);
        
        LPBasedCPInterface cpgen;
        
        if(negative){
        	cpgen = new SEQNegCostPartitioningGenerator();
        }else{
        	cpgen = new SEQCostPartitioningGenerator(compensate);
        }
        
        CPTest test = new CPTest();
        
        for (final CostPartitioningAgent agent : agentSet) {
        	cplp.generateCPLP(agent.agentName, agent.world);
        	cpgen.setProblem(agent.agentName, agent.world.getProblem());
        	test.addOriginalProblem(agent.world.getProblem());
        }
        
        cplp.solveCPLP();
        cpgen.setLPSolution(cplp.getSolution());
        
        float sum = 0;
        
        for (final CostPartitioningAgent agent : agentSet) {
            
        	cpgen.updateCosts(agent.world.getProblem());
        	test.addCPProblem(agent.world.getProblem());
        	
        	if(negative){
        		agent.plan("ES","none");
        	}else{
        		agent.plan("A*","hpot");
        	}
            
        	System.out.println(agent.agentName + " optimal: " + agent.cost);
        	sum += agent.cost;
        	
            CPDataAccumulator.getAccumulator().note += agent.agentName+":opt="+agent.cost+";";
            
        }

        CPDataAccumulator.getAccumulator().sumOfLocalOptimums = sum;
        
        LOGGER.info(">>> TEST CP PROPERTY");
        
        boolean cp = test.testCP();
        CPDataAccumulator.getAccumulator().isCP = cp;
        
        if(cp){
        	LOGGER.info(">>> OK");
        }else{
        	LOGGER.info(">>> NOT A COST PARTITIONING!");
        }

    }
    
    
    
    
    private void prepareCPDist(String solverID) {
        LOGGER.info(">>> COST PARTITIONING GENERATION");
        
        
        CostPartitioningGeneratorInterface cpgen = null;
        
        if(solverID.equals("uniform")){
        	cpgen = new UniformCPGenerator();
        }else if(solverID.equals("projcom")){
        	cpgen = new ProjectionCompensatingCPGenerator();
        }
        
        CPTest test = new CPTest();
        
        for (final CostPartitioningAgent agent : agentSet) {
        	cpgen.setProblem(agent.agentName, agent.world.getProblem());
        	test.addOriginalProblem(agent.world.getProblem());
        }
        
        float sum = 0;
        
        for (final CostPartitioningAgent agent : agentSet) {
            
        	cpgen.updateCosts(agent.world.getProblem());
        	test.addCPProblem(agent.world.getProblem());
        	
            agent.plan("A*","hpot");
            
        	System.out.println(agent.agentName + " optimal: " + agent.cost);
        	sum += agent.cost;
        	
            CPDataAccumulator.getAccumulator().note += agent.agentName+":opt="+agent.cost+";";
            
        }

        CPDataAccumulator.getAccumulator().sumOfLocalOptimums = sum;
        
        LOGGER.info(">>> TEST CP PROPERTY");
        
        boolean cp = test.testCP();
        CPDataAccumulator.getAccumulator().isCP = cp;
        
        if(cp){
        	LOGGER.info(">>> OK");
        }else{
        	LOGGER.info(">>> NOT A COST PARTITIONING!");
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

    	DIMAPWorldInterface world =  new DefaultDIMAPWorld(
                agentName,
                initQueuedCommunicator(agentName),
                new DefaultEncoder(),
                preprocessor.getProblemForAgent(agentName),
                preprocessor.getDomainForAgent(agentName),
                totalAgents
                );
        

		executor.addProblem(world.getProblem());
		
		return world;
    }


    @SuppressWarnings("deprecation")
    private void runEntities() {
        LOGGER.info(">>> ENTITIES RUNNING");


        final Thread mainThread = Thread.currentThread();
        final UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.error("Uncaught exception in agent/planner thread!", e);

                CPDataAccumulator.getAccumulator().writeOutput(Planner.FORCE_EXIT_AFTER_WRITE);

                

                mainThread.interrupt();
            }

        };

        for (final CostPartitioningAgent agent : agentSet) {
        	agent.run();    
        }

        
        CPDataAccumulator.getAccumulator().writeOutput(Planner.FORCE_EXIT_AFTER_WRITE);
    }
    
    
    public class CostPartitioningAgent{
    	public final String agentName;
    	public final PotentialHeuristic hpot;
    	public final DIMAPWorldInterface world;
    	public List<List<Action>> plans = new LinkedList<>();
    	
    	public boolean finished = false;
    	public float cost = -1;
    	
		public CostPartitioningAgent(String agentName, PotentialHeuristic hpot,DIMAPWorldInterface world) {
			super();
			this.agentName = agentName;
			this.hpot = hpot;
			this.world = world;
			
		}
		
		public void plan(String searchType, String heurType){
			plan( searchType,  heurType, true);
		}
		
		public void plan(String searchType, String heurType, boolean endWithFirstSolution){
			SearchInterface search =  null;
			MapConfiguration config = new MapConfiguration();
			
			if(searchType.equals("A*")){
				search =  new AStar(world.getProblem(),world.getProblem().getAllActions());
			}else if(searchType.equals("BFS")){
				search =  new BreadthFS(world.getProblem(),world.getProblem().getAllActions());
			}if(searchType.equals("ES")){
				search =  new ExhaustiveSearch(world.getProblem(),world.getProblem().getAllActions());
				if(!endWithFirstSolution){
					((ExhaustiveSearch)search).setReportAllSolutions();
				}
			}
			
			if(heurType.equals("hmax")){
				config = new MapConfiguration("endWithFirstSolution",endWithFirstSolution,"heuristic",new RelaxationHeuristic(world.getProblem(),new MaxEvaluator(world.getProblem())));
			}else if(heurType.equals("hpot")){
				PotentialLPGenerator potlp = new PotentialLPGenerator(world, solver,"hpot-domain" , "hpot-problem", agentName);
				PotentialHeuristic hpot = new PotentialHeuristic();
				potlp.generateAndSolvePotentialLP(hpot);
				config = new MapConfiguration("endWithFirstSolution",endWithFirstSolution,"heuristic",hpot);
			}else if(heurType.equals("max_hmax_hpot")){
				RelaxationHeuristic hmax = new RelaxationHeuristic(world.getProblem(),new MaxEvaluator(world.getProblem()));
				
				PotentialLPGenerator potlp = new PotentialLPGenerator(world, solver,"hpot-domain" , "hpot-problem", agentName);
				PotentialHeuristic hpot = new PotentialHeuristic();
				potlp.generateAndSolvePotentialLP(hpot);
				
				config = new MapConfiguration("endWithFirstSolution",endWithFirstSolution,"heuristic",new MaximumHeuristic(hpot,hmax));
			}else if(heurType.equals("none")){
				config = new MapConfiguration("endWithFirstSolution",endWithFirstSolution);
			}
			
			
            search.plan(config, new SearchCallback() {//new MapConfiguration("heuristic",new RelaxationHeuristic(world.getProblem(),new MaxEvaluator(world.getProblem()))), new SearchCallback() {
				
				@Override
				public void planNotFound() {
					finished = true;
					
				}
				
				@Override
				public void planFoundByOther() {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void planFound(List<String> plan) {
					finished = true;
					List<String> fixedPlan = new LinkedList<>();
					for(String a : plan){
						String[] split = a.split(" ");
						split[1] = agentName;
						String na = "";
						for(String ap : split) na = na  + ap+ " ";
						fixedPlan.add(na);
					}
					LOGGER.info(agentName+":\n"+fixedPlan);
					executor.setInitAndGoal(world.getProblem().initState, world.getProblem().goalSuperState);
					executor.executePlan(fixedPlan);
					plans.add(executor.getActionPlan());
					cost = executor.getCost();
					
				}
				
				@Override
				public void partialPlanReconstructed(List<String> plan, String initiator,int solutionCost) {
					// TODO Auto-generated method stub
					
				}
			});
            
            while(!finished){
            	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
		}

		public void run() {
			
		}
    }


}
