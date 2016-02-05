package cz.agents.madla.planner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.Communicator;
import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.alite.configurator.MapConfiguration;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.InitializableHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.LazilyDistributedFFHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.LazilyDistributedSAFFPersonalizedRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.LazilyDistributedSAFFReplyHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.LazilyDistributedSAFFRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.PPLazilyDistributedSAFFReplyHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.PPLazilyDistributedSAFFRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedBFRelaxationRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationPersonalizedRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationReplyHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationRestrictedReplyHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RecursiveDistributedRelaxationRestrictedRequestHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.SubmissiveRelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.AddEvaluator;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.EvaluatorInterface;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.FFEvaluator;
import cz.agents.dimaptools.heuristic.relaxed.evaluator.MaxEvaluator;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.search.AsyncDistributedBestFirstSearch;
import cz.agents.dimaptools.search.DistributedBestFirstSearch;
import cz.agents.dimaptools.search.GlobalLocalDistributedBestFirstSearch;
import cz.agents.dimaptools.search.HeuristicOpenList;
import cz.agents.dimaptools.search.MultiheuristicDistributedBestFirstSearch;
import cz.agents.dimaptools.search.SearchInterface;
import cz.agents.dimaptools.search.SyncDistributedBestFirstSearch;
import cz.agents.dimaptools.util.DisRPSharedProblemInfoProvider;
import cz.agents.dimaptools.util.RPSharedProblemInfoProvider;
import cz.agents.dimaptools.util.SharedProblemInfoProvider;
import cz.agents.madla.executor.PlanExecutorInterface;

public class Planner {

	private final static Logger LOGGER = Logger.getLogger(Planner.class);

	public static final boolean FORCE_EXIT_AFTER_WRITE = true;
	
	private final DIMAPWorldInterface world;
	private final Problem problem;
	private final Communicator comm;

	private final PlanExecutorInterface executor;

	HeuristicInterface heuristic;
	HeuristicInterface requestHeuristic;
	
	List<InitializableHeuristic> initializables = new LinkedList<>();
	
	private SearchInterface search;
	private ConfigurationInterface plannerConfig;
	
	private int recursionLevel = -1;
	private int requestTreshold = -1;
	private boolean useLocalRP = false;
	private boolean useLocalRPwithProjections = false;
	private boolean useGlobalRP = false;
	private boolean restricted = false;
	private boolean bestfirst = false;
	private boolean localRequestsLIFO = true;
	private long timeLimitMs = Long.MAX_VALUE;
	private boolean lazy = false;
	private boolean setadditive = false;
	private boolean ppsetadditive = false;
	private boolean localSubmissive = false;
	private boolean processAllMessagesAtOnce = false;
	

	public Planner(String useHeuristic, int recursionLevel, DIMAPWorldInterface world, PlanExecutorInterface executor,long timeLimitMs) {
		this.world = world;
		this.problem = world.getProblem();
		this.comm = world.getCommunicator();
		this.executor = executor;
		this.recursionLevel = recursionLevel;
		this.timeLimitMs = timeLimitMs;
		
//		LOGGER.info(world.getAgentName() + " MY ACTIONS:");
//		LOGGER.info(problem.getMyActions());
//		LOGGER.info(world.getAgentName() + " PROJECTED ACTIONS:");
//		LOGGER.info(problem.getProjectedActions());
		
		LinkedList<String> hParams = new LinkedList<String>(Arrays.asList(useHeuristic.split("-")));
		
		//ignore new
		if(hParams.peekFirst().equals("new"))hParams.removeFirst();
		
		LOGGER.info(problem.agent + " Configuring planner:" + hParams + ", rec_limit:"+recursionLevel+", time_limit_ms:"+timeLimitMs);
		
		String eval = hParams.pollFirst();
		EvaluatorInterface evaluator1 = null;
		EvaluatorInterface evaluator2 = null;
		EvaluatorInterface evaluator3 = null;
		
		boolean helpful=false;
		boolean noha=false;
		boolean multi=false;
		boolean helpfulInf=false;
		boolean sync=false;
		boolean async=false;
		boolean gsync=false;
		boolean qsync=false;
		boolean gl=false;
		boolean glUseLocalClosed=false;
		boolean recompute=false;
		restricted=false;
		requestTreshold=-1;
		
		if(!hParams.isEmpty()){
			String param = hParams.pollFirst();
			
			if(param.equals("ha")){	//helpful actions (=preferred operators)
				helpful=true;
				helpfulInf=true;
			}else if(param.equals("noha")){	//probably not used?
				noha=true;
			}else if(param.equals("multi")){	//multi-heuristic search (alternation) with helpful actions 
				multi=true;
				if(!hParams.isEmpty()){
					String param2 = hParams.peekFirst();
					
					if(param2.equals("noha")){	//no helpful actions
						helpful=false;
						helpfulInf=false;
						hParams.pollFirst();
					}
					if(param2.equals("0ha")){	//helpful actions just for the projected heuristic
						helpful=true;
						helpfulInf=false;
						hParams.pollFirst();
					}
					if(param.equals("gsync")){	//global synchronization of multiheuristic search - waiting for all heuristics to compute
						gsync=true;
						hParams.pollFirst();
					}
					if(param.equals("qsync")){	//per-open synchronization of mh search - waiting for each heuristic to compute
						qsync=true;
						hParams.pollFirst();
					}
				}
			}else if(param.equals("sync")){		//synchronized search (makes sense for d_max>0)
				sync=true;
			}else if(param.equals("async")){	//asynchronized search (relevant for d_max==0)
				async=true;
			}else if(param.equals("gl")){		//global-local search (d_max>0)
				gl=true;
			}else if(param.equals("glcl")){		//global-local search with separate local closed list (d_max>0)
				glUseLocalClosed=true;
				gl=true;
			}else if(param.equals("re")){		//recompute heuristic on receive (d_max==0)
				recompute=true;
			}else if(param.startsWith("rt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio
				requestTreshold=Integer.parseInt(param.replace("rt", ""));
			}else if(param.startsWith("lRPrt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio in local RP
				requestTreshold=Integer.parseInt(param.replace("lRPrt", ""));
				useLocalRP=true;
			}else if(param.startsWith("lRPprt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio in local RP including projections
				requestTreshold=Integer.parseInt(param.replace("lRPprt", ""));
				useLocalRPwithProjections=true;
			}else if(param.startsWith("gRPrt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio in global RP
				requestTreshold=Integer.parseInt(param.replace("gRPrt", ""));
				useGlobalRP=true;
			}else if(param.equals("rest")){		//distributed heuristic restricted to first layer
				restricted=true;
			}else if(param.equals("fifo")){		//FIFO order of computing local heuristic requests (d_max>0)
				localRequestsLIFO=false;
			}else if(param.equals("bf")){		//best-first order of computing local heuristic requests (d_max>0)
				bestfirst=true;
			}
		}
		
		if(!hParams.isEmpty()){
			String param = hParams.pollFirst();
			
			if(param.equals("ha")){	//helpful actions (=preferred operators)
				helpful=true;
				helpfulInf=true;
			}else if(param.equals("re")){
				recompute=true;
			}else if(param.startsWith("rt")){
				requestTreshold=Integer.parseInt(param.replace("rt", ""));
			}else if(param.startsWith("lRPrt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio in local RP
				requestTreshold=Integer.parseInt(param.replace("lRPrt", ""));
				useLocalRP=true;
			}else if(param.startsWith("lRPprt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio in local RP including projections
				requestTreshold=Integer.parseInt(param.replace("lRPprt", ""));
				useLocalRPwithProjections=true;
			}else if(param.startsWith("gRPrt")){	//switch between projected and distributed heuristic based on a threshold computed from public/all ratio in global RP
				requestTreshold=Integer.parseInt(param.replace("gRPrt", ""));
				useGlobalRP=true;
			}else if(param.equals("rest")){
				restricted=true;
			}else if(param.equals("fifo")){
				localRequestsLIFO=false;
			}else if(param.equals("bf")){
				bestfirst=true;
			}else if(param.equals("sub")){		//in gl or glcl, the local heuristic will check one message every iteration of the exploration queue
				localSubmissive=true;
			}else if(param.equals("suball")){		//in gl or glcl, the local heuristic will check all messages every iteration of the exploration queue
				localSubmissive=true;
				processAllMessagesAtOnce=true; 
			}else if(param.equals("all")){		//in gl or glcl, the search will check all messages after every local expansion
				processAllMessagesAtOnce=true; 
			}
		}
		
		if(eval.equals("add")){
			evaluator1 = new AddEvaluator(problem,helpful);
			evaluator2 = new AddEvaluator(problem,helpfulInf);
			evaluator3 = new AddEvaluator(problem,helpfulInf);
		}else if(eval.equals("max")){
			evaluator1 = new MaxEvaluator(problem,helpful);
			evaluator2 = new MaxEvaluator(problem,helpfulInf);
			evaluator3 = new MaxEvaluator(problem,helpfulInf);
		}else if(eval.equals("rdFF")){
			evaluator1 = new FFEvaluator(problem);
			evaluator2 = new FFEvaluator(problem);
			evaluator3 = new FFEvaluator(problem);
		}else if(eval.equals("FF") || eval.equals("lazyFF")){
			lazy=true;					//lazy distribution of FF, run with -sync modifier
			evaluator1 = new FFEvaluator(problem);
		}else if(eval.equals("saFF")){
			setadditive=true;			//lazy distribution of FF with set-additive combination of other agent's plans, run with -sync modifier
			evaluator1 = new FFEvaluator(problem);
		}else if(eval.equals("PPsaFF")){
			ppsetadditive=true;			//lazy distribution of FF with privacy-preserving set-additive combination of other agent's plans, run with -sync modifier
			evaluator1 = new FFEvaluator(problem);
		}else{
			LOGGER.error("Unknown evaluator " + eval, new RuntimeException("Unknown evaluator " + eval));
		}
		
		if(noha){
			configureHelpful(evaluator1, evaluator2, false, recompute);
		}else if(multi){
			configureMulti(evaluator1, evaluator2, evaluator3, helpful, helpfulInf, recompute, qsync, gsync);
		}else if(helpful){
			configureHelpful(evaluator1, evaluator2, true, recompute);
		}else if(sync){
			configureSync(evaluator1, evaluator2, recompute);
		}else if(async){
			configureAsync(evaluator1, evaluator2, recompute);
		}else if(gl){
			configureGlobalLocal(evaluator1, evaluator2, evaluator3, recompute, glUseLocalClosed);
		}else{
			configureSimple(evaluator1, evaluator2, recompute);
		}

	}
	
	private void configureDistributedHeuristics(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2){
		
		if(lazy){
			LazilyDistributedFFHeuristic req = new LazilyDistributedFFHeuristic(world,false,recursionLevel);
			LazilyDistributedFFHeuristic rep = new LazilyDistributedFFHeuristic(world,true,recursionLevel);
			
			req.setOtherProtocol(rep.getProtocol());
			rep.setOtherProtocol(req.getProtocol());
			
			heuristic = req;
			requestHeuristic = rep;
		}else if(setadditive){
			LazilyDistributedSAFFRequestHeuristic req;
			
			if(requestTreshold>=0){
				SharedProblemInfoProvider provider = getSharedProblemInfoProvider();
				req = new LazilyDistributedSAFFPersonalizedRequestHeuristic(world, provider, recursionLevel, requestTreshold);
				initializables.add((InitializableHeuristic) req);
			}else{
				req = new LazilyDistributedSAFFRequestHeuristic(world,recursionLevel);
			}
			LazilyDistributedSAFFReplyHeuristic rep = new LazilyDistributedSAFFReplyHeuristic(world);
				
			req.setReplyProtocol(rep.getReplyProtocol());
			rep.setRequestProtocol(req.getRequestProtocol());
				
			heuristic = req;
			requestHeuristic = rep;
		}else if(ppsetadditive){
			PPLazilyDistributedSAFFRequestHeuristic req = new PPLazilyDistributedSAFFRequestHeuristic(world,recursionLevel);
			PPLazilyDistributedSAFFReplyHeuristic rep = new PPLazilyDistributedSAFFReplyHeuristic(world);
				
			req.setReplyProtocol(rep.getReplyProtocol());
			rep.setRequestProtocol(req.getRequestProtocol());
				
			heuristic = req;
			requestHeuristic = rep;
		}else{
			RecursiveDistributedRelaxationRequestHeuristic req;
			RecursiveDistributedRelaxationReplyHeuristic rep;
			
			if(restricted){
				req = new RecursiveDistributedRelaxationRestrictedRequestHeuristic(world, evaluator1, recursionLevel);
				rep = new RecursiveDistributedRelaxationRestrictedReplyHeuristic(world, evaluator2,req.getRequestProtocol());
			}else{
				if(requestTreshold>=0){
					SharedProblemInfoProvider provider = getSharedProblemInfoProvider();
					req = new RecursiveDistributedRelaxationPersonalizedRequestHeuristic(world, evaluator1, provider, recursionLevel, requestTreshold);
					initializables.add((InitializableHeuristic) req);
				}else if(bestfirst){
					req = new RecursiveDistributedBFRelaxationRequestHeuristic(world, evaluator1, recursionLevel);
				}else{
					req = new RecursiveDistributedRelaxationRequestHeuristic(world, evaluator1, recursionLevel,localRequestsLIFO);
				}
				rep = new RecursiveDistributedRelaxationReplyHeuristic(world, evaluator2,req.getRequestProtocol());
			}
			
			req.setReplyProtocol(rep.getReplyProtocol());
			heuristic = req;
			requestHeuristic = rep;
		}
		
		
	}
	
	
	private SharedProblemInfoProvider getSharedProblemInfoProvider(){
		SharedProblemInfoProvider provider;
		if(useLocalRP){
			provider = new RPSharedProblemInfoProvider(world,world.getNumberOfAgents(),false);
		}else if(useLocalRPwithProjections){
			provider = new RPSharedProblemInfoProvider(world,world.getNumberOfAgents(),true);
		}else if(useGlobalRP){
			provider= new DisRPSharedProblemInfoProvider(world,world.getNumberOfAgents());
		}else{
			provider = new SharedProblemInfoProvider(world,world.getNumberOfAgents());
		}
		return provider;
	}
	
	
	public void configureSimple(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2, boolean recompute){
		if(recursionLevel == 0){
			heuristic = new RelaxationHeuristic(problem,evaluator1);
			requestHeuristic = heuristic;
			
		}else{
			configureDistributedHeuristics(evaluator1, evaluator2);
		}
		search = new DistributedBestFirstSearch(world, timeLimitMs);
		plannerConfig = new MapConfiguration("heuristic",heuristic,"requestHeuristic",requestHeuristic,"recomputeHeuristicOnReceive",recompute);
	}
	
	public void configureGlobalLocal(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2, EvaluatorInterface evaluator3, boolean recompute, boolean glUseLocalClosed){
		RelaxationHeuristic localHeuristic = localSubmissive ? new SubmissiveRelaxationHeuristic(problem,evaluator1) : new RelaxationHeuristic(problem,evaluator1);
		configureDistributedHeuristics(evaluator2, evaluator3);
		
		search = new GlobalLocalDistributedBestFirstSearch(world, timeLimitMs);
		plannerConfig = new MapConfiguration(
				"localHeuristic",localHeuristic,
				"requestGlobalHeuristic",heuristic,
				"replyGlobalHeuristic",requestHeuristic,
				"recomputeHeuristicOnReceive",recompute,
				"useLocalClosed",glUseLocalClosed,
				"processAllMessagesAtOnce",processAllMessagesAtOnce);
	}
	
	public void configureSync(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2, boolean recompute){
		if(recursionLevel == 0){
			heuristic = new RelaxationHeuristic(problem,evaluator1);
			requestHeuristic = heuristic;
		}else{
			configureDistributedHeuristics(evaluator1, evaluator2);
		}
		search = new SyncDistributedBestFirstSearch(world, timeLimitMs);
		plannerConfig = new MapConfiguration("heuristic",heuristic,"requestHeuristic",requestHeuristic,"recomputeHeuristicOnReceive",recompute);
	}
	
	public void configureAsync(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2, boolean recompute){
		if(recursionLevel == 0){
			heuristic = new RelaxationHeuristic(problem,evaluator1);
			requestHeuristic = heuristic;
		}else{
			configureDistributedHeuristics(evaluator1, evaluator2);
		}
		search = new AsyncDistributedBestFirstSearch(world, timeLimitMs);
		plannerConfig = new MapConfiguration("heuristic",heuristic,"requestHeuristic",requestHeuristic,"recomputeHeuristicOnReceive",recompute);
	}
	
	public void configureHelpful(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2, boolean useHelpful, boolean recompute){
		HeuristicOpenList open = null;
		String openID = "open";
		
		if(recursionLevel == 0){
			open = new HeuristicOpenList(openID,useHelpful,new RelaxationHeuristic(problem,evaluator1),recompute);
		}else{
			configureDistributedHeuristics(evaluator1, evaluator2);
			
			open = new HeuristicOpenList(openID,useHelpful,heuristic,requestHeuristic,recompute);
		}
		
		search = new MultiheuristicDistributedBestFirstSearch(world);
		plannerConfig = new MapConfiguration(openID,open);
	}
	
	public void configureMulti(EvaluatorInterface evaluator1, EvaluatorInterface evaluator2, EvaluatorInterface evaluator3, boolean useHelpful0, boolean useHelpfulInf, boolean recompute, boolean qsync, boolean gsync){
		String openID = "open";
		
		HeuristicOpenList open = new HeuristicOpenList(openID,useHelpful0,new RelaxationHeuristic(problem,evaluator1),recompute);
		
		configureDistributedHeuristics(evaluator2, evaluator3);
		
		HeuristicOpenList openRD = new HeuristicOpenList(openID+"RD",useHelpfulInf,heuristic,requestHeuristic,false);
		
		search = new MultiheuristicDistributedBestFirstSearch(world);
		plannerConfig = new MapConfiguration(openID+"RD",openRD,openID,open,"qsync",qsync,"gsync",gsync);
		
	}


	public void planAndExecuteFinal(){
		
		initialize();
		
        search.plan(plannerConfig, new SearchInterface.SearchCallback() {

        	@Override
            public void planNotFound() {
                System.out.println("Plan not found!");
                DataAccumulator.getAccumulator().finishTimeMs = System.currentTimeMillis();
                DataAccumulator.getAccumulator().finished = false;
                DataAccumulator.getAccumulator().planLength = -1;
                DataAccumulator.getAccumulator().planValid = false;
            }

			@Override
			public void planFoundByOther() {
				System.out.println("Plan found by other agent...");
			}

            @Override
            public void planFound(List<String> plan) {
            	if(!DataAccumulator.getAccumulator().finished){
	            	DataAccumulator.getAccumulator().finishTimeMs = System.currentTimeMillis();
	                DataAccumulator.getAccumulator().finished = true;
	                DataAccumulator.getAccumulator().planLength = plan.size();
	                long time = DataAccumulator.getAccumulator().finishTimeMs - DataAccumulator.getAccumulator().startTimeMs;
	                System.out.println(time +" - "+ plan);
	                DataAccumulator.getAccumulator().planValid = executor.executePlan(plan);
	                DataAccumulator.getAccumulator().writeOutput(FORCE_EXIT_AFTER_WRITE);
            	}
            }



			@Override
			public void partialPlanReconstructed(List<String> plan, String initiator, int solutionCost) {
				// TODO Auto-generated method stub
				
			}

        });

	}
	
public void planAndReturnPartial(){
	
	initialize();
		
        search.plan(plannerConfig, new SearchInterface.SearchCallback() {

        	@Override
            public void planNotFound() {
                System.out.println("Plan not found!");
                DataAccumulator.getAccumulator().finishTimeMs = System.currentTimeMillis();
                DataAccumulator.getAccumulator().finished = false;
                DataAccumulator.getAccumulator().planLength = -1;
                DataAccumulator.getAccumulator().planValid = false;
            }

			@Override
			public void planFoundByOther() {
				System.out.println("Plan found by other agent...");
			}

            @Override
            public void planFound(List<String> plan) {
            	if(!DataAccumulator.getAccumulator().finished){
	            	DataAccumulator.getAccumulator().finishTimeMs = System.currentTimeMillis();
	                DataAccumulator.getAccumulator().finished = true;
	                DataAccumulator.getAccumulator().planLength = plan.size();
	                long time = DataAccumulator.getAccumulator().finishTimeMs - DataAccumulator.getAccumulator().startTimeMs;
	                System.out.println(time +" - "+ plan);
//	                DataAccumulator.getAccumulator().planValid = executor.executePlan(plan);
            	}
            }

			@Override
			public void partialPlanReconstructed(List<String> plan, String initiator, int solutionCost) {
				executor.executePartialPlan(plan,initiator,solutionCost);
			}

        });

	}

	private void initialize() {
		for(InitializableHeuristic ih : initializables){
			ih.initialize();
		}
	
	}


	public DIMAPWorldInterface getWorld(){
		return world;
	}

	public String getName() {
		return comm.getAddress();
	}



}
