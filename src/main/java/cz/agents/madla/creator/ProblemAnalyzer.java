package cz.agents.madla.creator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import cz.agents.alite.creator.Creator;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.DefaultDIMAPWorld;
import cz.agents.dimaptools.communication.protocol.DefaultEncoder;
import cz.agents.dimaptools.experiment.AnalyzerDataAccumulator;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.experiment.Trace;
import cz.agents.dimaptools.input.addl.ADDLObject;
import cz.agents.dimaptools.input.addl.ADDLParser;
import cz.agents.dimaptools.input.sas.SASParser;
import cz.agents.dimaptools.input.sas.SASPreprocessor;
import cz.agents.dimaptools.model.Domain;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.util.DisRPSharedProblemInfoProvider;
import cz.agents.dimaptools.util.InteractionGraph;
import cz.agents.dimaptools.util.RPSharedProblemInfoProvider;
import cz.agents.dimaptools.util.dig.DetailedInteractionGraph;

public class ProblemAnalyzer implements Creator {

    private final static Logger LOGGER = Logger.getLogger(ProblemAnalyzer.class);


    private static final String TRANSLATOR = "./misc/fd/src/translate/translate.py";
    private static final String PREPROCESSOR = "./preprocess-runner";
    private static final String RESOURCES = "./";
    private static final String OUTPUT = "./aout.csv";

    private boolean fromSAS = true;
    
    private final ReceiverTable receiverTable = new DefaultReceiverTable();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private String domainFileName;
    private String problemFileName;
    private String sasFileName;
    private String agentFileName;

    private String heuristic;
    private int recursionLevel;
    
    private ADDLObject addl;
    private Set<DIMAPWorldInterface> worldSet = new HashSet<>();
    private Map<String,Integer> globalRPRatios = new HashMap<>();
    
    private InteractionGraph IG = new InteractionGraph();
    private DetailedInteractionGraph DIG = new DetailedInteractionGraph();


    protected SASPreprocessor preprocessor;


    @Override
    public void init(String[] args) {

        for(int i=0;i<args.length;i++){
            System.out.println(args[i]);
        }

        if (args.length != 6 && args.length != 7) {
            System.out.println("provided args: " + Arrays.toString(args));
            System.out.println("Usage (from SAS): <output>.sas <agents>.addl <add|max|FF> <recursionLevel> <time limit (min)>\nUsage (from PDDL): <domain>.pddl <problem>.pddl <agents>.addl <add|max|FF> <recursionLevel> <time limit (min)>");
            System.exit(1);
        }

        if (args.length == 6){
            sasFileName = args[1];
            agentFileName = args[2];
            heuristic = args[3];
            recursionLevel = Integer.parseInt(args[4]);
        }

        if (args.length == 7){
            fromSAS = false;
            sasFileName = "output";
            domainFileName = args[1];
            problemFileName = args[2];
            agentFileName = args[3];
            heuristic = args[4];
            recursionLevel = Integer.parseInt(args[5]);
        }

        Trace.setFileStream("log/trace.log");
    }



    @Override
    public void create() {

        LOGGER.info(">>> CREATION");
        LOGGER.info(">>>   sas: " + sasFileName);
        LOGGER.info(">>>   agents: " + agentFileName);

        File agentFile = new File(RESOURCES + agentFileName);
        if (!agentFile.exists()) {
            LOGGER.fatal("Agent file " + RESOURCES + agentFileName + " does not exist!");
            System.exit(1);
        }
        addl = new ADDLParser().parse(agentFile);
        
        if(domainFileName == null) domainFileName = sasFileName;
        if(problemFileName == null) problemFileName = sasFileName;

        AnalyzerDataAccumulator.startNewAccumulator(domainFileName, problemFileName);
        AnalyzerDataAccumulator.getAccumulator().agents = addl.getAgentCount();
        AnalyzerDataAccumulator.getAccumulator().setOutputFile(OUTPUT);

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
        preprocessor = new SASPreprocessor(parser.getDomain(), addl);

        createEntities();

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

    private void createEntities() {
        LOGGER.info(">>> ENTITIES CREATION");


        for (String agentName: addl.getAgentList()) {

            Domain dom = preprocessor.getDomainForAgent(agentName);
            Problem prob = preprocessor.getProblemForAgent(agentName);

            if(LOGGER.isInfoEnabled()){
	            LOGGER.info(agentName + " domain:\n" + dom.getNames());
	            LOGGER.info(agentName + " private actions:\n" + prob.getMyPrivateActions());
	            LOGGER.info(agentName + " public actions:\n" + prob.getMyPublicActions());
	            LOGGER.info(agentName + " projections:\n" + prob.getProjectedActions());
            }
            
        }
        
        if(LOGGER.isInfoEnabled())preprocessor.debugPrint();
        
        int pubActions = 0;
        int allActions = 0;
        
        for (String agentName: addl.getAgentList()) {
        	
        	DIMAPWorldInterface w = initWorld(agentName,addl.getAgentCount());
        	worldSet.add(w);
        	
        	pubActions += w.getProblem().getMyPublicActions().size();
        	allActions += w.getProblem().getMyActions().size();
        	
        }
        
        AnalyzerDataAccumulator.getAccumulator().publicRatio = (int)((float)pubActions/(float)allActions *100);
        
        runDisRPSharedProblemInfoProvider();
        	
        for(DIMAPWorldInterface world : worldSet){
        	
            debugPrint(world.getAgentName(),world);
        }
        
        generateInteractionGraph();
        
        AnalyzerDataAccumulator.getAccumulator().writeOutput(true);

        System.exit(0);

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

    	DefaultDIMAPWorld world = new DefaultDIMAPWorld(
                agentName,
                initQueuedCommunicator(agentName),
                new DefaultEncoder(),
                preprocessor.getProblemForAgent(agentName),
                totalAgents
                );
        
        IG.addAgent(agentName, world.getProblem());
        DIG.addAgent(agentName, world.getProblem());
        
        return world;
    }
    
    public void runDisRPSharedProblemInfoProvider(){
    	Set<Thread> threadSet = new HashSet<>();
    	
    	for (final DIMAPWorldInterface world : worldSet) {

//          final Communicator comm = initCommunicator(agent);

          Thread thread = new Thread(new Runnable() {

              @Override
              public void run() {
            	  DisRPSharedProblemInfoProvider drpInfo = new DisRPSharedProblemInfoProvider(world,world.getNumberOfAgents());
            	  try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	  int globalRPRatio = drpInfo.computeMyCoupling(world.getProblem());
            	  globalRPRatios.put(world.getAgentName(), globalRPRatio);
              }
          }, world.getAgentName());
         
          thread.start();
          threadSet.add(thread);
      }

      try {
          for (Thread thread : threadSet) {
              thread.join();
          }
      } catch (InterruptedException e) {
          LOGGER.debug("Main thread interrupted.");
      }
      
      int sum = 0;
      int max = 0;
      for(int ratio : globalRPRatios.values()){
    	  sum += ratio;
    	  max = Math.max(max, ratio);
      }
      
      AnalyzerDataAccumulator.getAccumulator().globalRPPublicRatioAvg = sum/globalRPRatios.size();
      AnalyzerDataAccumulator.getAccumulator().globalRPPublicRatioMax = max;
      
    }
    
    public void generateInteractionGraph(){
    	IG.createGraph();
    	
    	if(LOGGER.isInfoEnabled()){
	    	LOGGER.info("---- Interaction Graph ----");
	    	LOGGER.info("("+IG.numberOfAgents()+")\n"+IG);
	    	LOGGER.info("---- x ----");
    	}
    	
    	DIG.buildGraph();
    	int cost = DIG.getCost();
    	
    	if(LOGGER.isInfoEnabled()){
	    	LOGGER.info("---- Detailed Interaction Graph ----");
	    	LOGGER.info("("+DIG.numberOfAgents()+")\n");//+DIG.toString());
	    	LOGGER.info("cost:" + cost);
	    	LOGGER.info("---- x ----");
	    	LOGGER.info("---- Achiever Graph ----");
	    	LOGGER.info("("+DIG.numberOfAgents()+")\n" );//+ DIG.printActionSubset(DIG.getAchievers()));
	    	LOGGER.info("achievers:" + DIG.getAchievers());
	    	LOGGER.info("agent max value:" + DIG.getAgentMaxValue());
	    	LOGGER.info("agent add value:" + DIG.getAgentAddValue());
	    	LOGGER.info("agent add per goal value:" + DIG.getAgentAddPerGoalValue());
	    	LOGGER.info("agent simple value:" + DIG.getAgentSimpleValue());
	    	LOGGER.info("---- x ----");
    	}else{
    		DIG.getAchievers();
    	}
    	
    	AnalyzerDataAccumulator.getAccumulator().agentAddPerGoalValue = DIG.getAgentAddPerGoalValue();
    	AnalyzerDataAccumulator.getAccumulator().agentAddValue = DIG.getAgentAddValue();
    	AnalyzerDataAccumulator.getAccumulator().agentMaxValue = DIG.getAgentMaxValue();
    	AnalyzerDataAccumulator.getAccumulator().agentSimpleValue = DIG.getAgentSimpleValue();
    	
    }
    
    public void debugPrint(String agent, DIMAPWorldInterface world) {
		
    	LOGGER.info("---- ----");
    	LOGGER.info("AGENT: " + agent);
    	
		Domain dom = preprocessor.getDomainForAgent(agent);
		
		if(LOGGER.isInfoEnabled())LOGGER.info(dom.toString());
		
		Problem prob = preprocessor.getProblemForAgent(agent);
		int pub = prob.getPublicActions().size()-prob.getProjectedActions().size();
		
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("my private actions: "+(prob.getMyActions().size()-pub));
			LOGGER.info("my public actions: "+pub);
			LOGGER.info("projected actions: "+prob.getProjectedActions().size());
			LOGGER.info("pure projected actions: "+prob.getPureProjectedActions().size());
		}
		
		int ratio = (int)(((double)pub/(double)prob.getMyActions().size())*100);
		
		LOGGER.info("public action ratio: "+ratio+"%");
		
		RPSharedProblemInfoProvider rpInfo = new RPSharedProblemInfoProvider(world,world.getNumberOfAgents(),false);
		int localRPRatio = rpInfo.computeMyCoupling(prob);
		
		LOGGER.info("local RP public action ratio: "+localRPRatio+"%");
		
		rpInfo = new RPSharedProblemInfoProvider(world,world.getNumberOfAgents(),true);
		localRPRatio = rpInfo.computeMyCoupling(prob);
		
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("local RP public action ratio (including projections): "+localRPRatio+"%");
			
			LOGGER.info("global RP public action ratio: "+globalRPRatios.get(world.getAgentName())+"%");
			
			LOGGER.info("- domain -");
			LOGGER.info("variables: "+dom.sizeGlobal());
			LOGGER.info("agent variables: "+dom.sizeAgent());
			LOGGER.info("public variables: "+dom.publicVarMax);
			LOGGER.info("public values: "+dom.publicValMax);
			LOGGER.info("private values: "+(dom.agentValMax-dom.agentValMin));
		}
		
	}


    


}
