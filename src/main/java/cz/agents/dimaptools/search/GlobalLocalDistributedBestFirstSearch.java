package cz.agents.dimaptools.search;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import cz.agents.alite.communication.CommunicationPerformer;
import cz.agents.alite.communication.Communicator;
import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.PlanningFinishedMessage;
import cz.agents.dimaptools.communication.message.ReconstructPlanMessage;
import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.dimaptools.communication.protocol.DistributedSearchProtocol;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.heuristic.DistributedReplyHeuristicInterface;
import cz.agents.dimaptools.heuristic.DistributedRequestHeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.relaxed.RelaxationHeuristic;
import cz.agents.dimaptools.heuristic.relaxed.SubmissiveRelaxationHeuristic;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

/**
 * Simple implementation of distributed Best-First Search with deferred heuristic evaluation.
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class GlobalLocalDistributedBestFirstSearch implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(GlobalLocalDistributedBestFirstSearch.class);

    public static final boolean USE_STOPWATCH = false;

    protected final Problem problem;
    protected final PriorityBlockingQueue<SearchState> openGlobal;
    protected final PriorityBlockingQueue<SearchState> openLocal;

    protected final TIntHashSet closed = new TIntHashSet();
    protected final TIntHashSet closedLocal = new TIntHashSet();
    private boolean useLocalClosed = false;

    protected RelaxationHeuristic localHeuristic;
    protected DistributedRequestHeuristicInterface requestGlobalHeuristic;
    protected DistributedReplyHeuristicInterface replyGlobalHeuristic;

    protected SearchCallback planCallback;

    protected boolean processAllMessagesAtOnce = false;
    protected boolean recomputeLocalHeuristicOnReceive = false;

    protected final Communicator comm;
    protected final CommunicationPerformer commPerformer;
    protected final TIntObjectHashMap sentStates = new TIntObjectHashMap();

    protected final DistributedSearchProtocol protocol;

    protected String bestReconstructedPlanBy = null;
    protected int bestReconstructedPlanCost = Integer.MAX_VALUE;

    protected long timeLimitMs = Long.MAX_VALUE;

    protected volatile boolean run = true;
    protected volatile boolean search = true;

    protected int minGH = Integer.MAX_VALUE;
    protected int minLH = Integer.MAX_VALUE;
    protected int maxG = 0;




    public GlobalLocalDistributedBestFirstSearch(DIMAPWorldInterface world) {
        this(world,Long.MAX_VALUE);
    }

    public GlobalLocalDistributedBestFirstSearch(DIMAPWorldInterface world,long timeLimitMs) {
        this.problem = world.getProblem();
        this.comm = world.getCommunicator();
        commPerformer = world.getCommPerformer();
        this.timeLimitMs = timeLimitMs;


        openLocal = new PriorityBlockingQueue<SearchState>(10000,new Comparator<SearchState>(){

            @Override
            public int compare(SearchState arg0, SearchState arg1) {
                return arg0.getHeuristic() - arg1.getHeuristic();
            }

        });

        openGlobal = new PriorityBlockingQueue<SearchState>(10000,new Comparator<SearchState>(){

            @Override
            public int compare(SearchState arg0, SearchState arg1) {
                return arg0.getHeuristic() - arg1.getHeuristic();
            }

        });

        protocol = new DistributedSearchProtocol(comm, world.getAgentName(), world.getEncoder()) {

            @Override
            public void receiveStateMessage(StateMessage sm, String sender) {
                if (!closed.contains(sm.getHash())) {

                    if(LOGGER.isDebugEnabled())LOGGER.debug(comm.getAddress() + " receive state " + Arrays.toString(sm.getValues()));

                    addReceivedState(sm, sender);

                }
            }

            @Override
            public void receiveReconstructPlanMessage(ReconstructPlanMessage rpm) {
                SearchState state = (SearchState)sentStates.get(rpm.getLastStateHash());

//            	LOGGER.info(comm.getAddress() + " receive reconstruct msg " + state.hashCode() );

                reconstructPlan(state, rpm.getPlan(),rpm.getInitiatorID(),rpm.getSolutionCost());

//            	run = false;
                search = false;
            }

            @Override
            public void receivePlanningFinishedMessage(PlanningFinishedMessage msg) {

                LOGGER.warn(comm.getAddress() + " receive PLANNING_FINISHED!" );

                long tCPU = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
                long startCPU = DataAccumulator.getAccumulator().startCPUTimeMs.get(problem.agent);
                DataAccumulator.getAccumulator().CPUTimeMs.put(problem.agent, (tCPU-startCPU)/1000000);
                LOGGER.info("planning finished message - CPU-time ("+problem.agent+"): " + (tCPU-startCPU)/1000000);

                run = false;

            }
        };


    }


    protected void addReceivedState(final StateMessage sm, String sender){

        final SearchState newState = new SearchState(problem.initState.getDomain(),sm,sender);

        if(sm.isGlobalH()){
            openGlobal.add(newState);
            openLocal.add(newState);
        }else if (recomputeLocalHeuristicOnReceive){
        	localHeuristic.getHeuristic(newState, new HeuristicComputedCallback() {
				
				@Override
				public void heuristicComputed(HeuristicResult result) {
					newState.setHeuristics(result.getValue());
					openLocal.add(newState);
				}
			});
            
        }else{
        	openLocal.add(newState);
        }






    }


    protected void readConfiguration(ConfigurationInterface config){
        if(config.containsKey("localHeuristic")){
            localHeuristic =  (RelaxationHeuristic) config.getObject("localHeuristic");

            if(localHeuristic instanceof SubmissiveRelaxationHeuristic){
                ((SubmissiveRelaxationHeuristic)localHeuristic).setInterrupt(new SubmissiveRelaxationHeuristic.InterruptInterface() {

                    @Override
                    public void interruptComputation() {
//						System.out.println("INTERRUPT!");
                        processCommunication();
                    }
                });
            }
        }

        if(config.containsKey("requestGlobalHeuristic")){
            requestGlobalHeuristic =  (DistributedRequestHeuristicInterface) config.getObject("requestGlobalHeuristic");
        }

        if(config.containsKey("replyGlobalHeuristic")){
            replyGlobalHeuristic = (DistributedReplyHeuristicInterface) config.getObject("replyGlobalHeuristic");
        }

        recomputeLocalHeuristicOnReceive = config.getBoolean("recomputeLocalHeuristicOnReceive", false);
        useLocalClosed = config.getBoolean("useLocalClosed", false);
        processAllMessagesAtOnce = config.getBoolean("processAllMessagesAtOnce", false);
    }


    private boolean processCommunication(){
//    	System.out.println("PROCESS! " + processAllMessagesAtOnce);
        if(System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs > timeLimitMs){
            run = false;
            LOGGER.warn("TIMEOUT!");
            planCallback.planNotFound();
            return false;
        }

        if(processAllMessagesAtOnce){
            boolean received = true;
            while(received){
//	    		System.out.println("rec...");
                received = commPerformer.performReceiveNonblock();
            }
        }else{
            commPerformer.performReceiveNonblock();
        }

        if(search){
            if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalHeuristicStopwatch.start(problem.agent);
            requestGlobalHeuristic.processMessages();
            if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalHeuristicStopwatch.stop(problem.agent);

            if(USE_STOPWATCH)DataAccumulator.getAccumulator().otherHeuristicStopwatch.start(problem.agent);
            replyGlobalHeuristic.processMessages();
            if(USE_STOPWATCH)DataAccumulator.getAccumulator().otherHeuristicStopwatch.stop(problem.agent);
        }

        return true;
    }

    private void checkSolution(SearchState state){
        if (solutionFound(state)){
            if (state.wasExpandedByMe(problem.agent)) {
                long time =  System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs;
                LOGGER.warn(" by me ("+problem.agent+") - " + time);

                if(USE_STOPWATCH){
                    DataAccumulator.getAccumulator().searchStopwatch.stop(problem.agent);
                    DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);
                    DataAccumulator.getAccumulator().localSearchStopwatch.stop(problem.agent);
                }

                List<String> plan = new LinkedList<String>();
                reconstructPlan(state, plan, problem.agent, state.getG());

                search = false;
            }
        }
    }


    @Override
    public void plan(ConfigurationInterface config, SearchCallback planCallback) {

        readConfiguration(config);

        this.planCallback = planCallback;

        if(USE_STOPWATCH)DataAccumulator.getAccumulator().searchStopwatch.start(problem.agent);

        openGlobal.add(new SearchState(problem.initState));
        openLocal.add(new SearchState(problem.initState));

        do {
            if(search){



                if(openGlobal.isEmpty() && openLocal.isEmpty()){
                    if(!requestGlobalHeuristic.hasWaitingLocalRequests()){
//        				LOGGER.warn("both open empty!!!");
                    }
                    boolean proceed = processCommunication();
                    if(!proceed)break;
                    continue;
                }

                final SearchState state;

                if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalSearchStopwatch.start(problem.agent);

                if(!openGlobal.isEmpty() || (!openLocal.isEmpty() && !requestGlobalHeuristic.hasWaitingLocalRequests())){ //Maybe if so and global heuristic is not being computed, you should take state from local open
                    state = openGlobal.isEmpty() ? openLocal.poll() : openGlobal.poll();

                    if(state.getHeuristic() < minGH){
                          minGH = state.getHeuristic();
                           if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal global [" + state.getParentActionOwner() + "] /h/: " + minGH);
                    }
                    if(state.getG() > maxG){
                           maxG = state.getG();
                           if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
                    }

                    if (state != null && !closed.contains(state.hashCode())) {

                        closed.add(state.hashCode());

                        checkSolution(state);

                        boolean computeH = true;
                        
//                        LOGGER.setLevel(Level.WARN);
//                        if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": global search - open: " + openGlobal.size());

                            if(search && !requestGlobalHeuristic.hasWaitingLocalRequests()){
                                if(computeH){
//	                        		System.out.println("GLOBAL COMPUTE..." );
                                    if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);
                                    if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalHeuristicStopwatch.start(problem.agent);

                                    requestGlobalHeuristic.getHeuristic(state, new HeuristicComputedCallback(){

                                        @Override
                                        public void heuristicComputed(HeuristicResult result) {
        //                                	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": global heuristic computed: " + state.hashCode() + "("+result.getValue()+")");


                                            if(result.getValue() >= HeuristicInterface.LARGE_HEURISTIC){
                                                return;
                                            }

                                            state.setHeuristics(result.getValue());
        //                                    state.setHeuristics(10);

                                            if (state.wasReachedByPublicAction()) {
                                                sendState(state,true);
                                            }

                                            DataAccumulator.getAccumulator().expandedStates++;
                                            DataAccumulator.getAccumulator().expandedStatesGlobal++;
                                            if(!DataAccumulator.getAccumulator().initHeuristicDist.containsKey(problem.agent))DataAccumulator.getAccumulator().initHeuristicDist.put(problem.agent, result.getValue());


                                            List<SearchState> expandedStates = expand(state);
        //                                    LOGGER.warn(problem.agent + ": global heuristic computed, expanded: " + expandedStates.size());
                                            openGlobal.addAll(expandedStates);
                                            openLocal.addAll(expandedStates);
                                        }
                                    });

                                    if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalHeuristicStopwatch.stop(problem.agent);
                                    if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalSearchStopwatch.start(problem.agent);

                                }else{
//	                        		System.out.println("GLOBAL SKIP: " + state.getHeuristic());
                                    DataAccumulator.getAccumulator().expandedStates++;
                                    DataAccumulator.getAccumulator().expandedStatesGlobal++;

                                    List<SearchState> expandedStates = expand(state);
                                    openGlobal.addAll(expandedStates);
                                    openLocal.addAll(expandedStates);
                                }
                            }

                    }


                }else{
//        			LOGGER.info(problem.agent + ": global open empty!");
                    state = null;
                }

                if(USE_STOPWATCH)DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);

                boolean local = false;

                if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.start(problem.agent);

                while(search && (openGlobal.isEmpty() || requestGlobalHeuristic.hasWaitingLocalRequests())){
                    final SearchState localState;


                    if(!local && state != null){
//        				if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": local state is state, closed: "+closed.contains(state.hashCode()));
                        localState = state;
                    }else{
                        if(openLocal.isEmpty()){
//        					LOGGER.info(problem.agent + ": local open empty!");
                            break;
                        }
                        localState = openLocal.poll();
                    }

                    if (state != null && !(closed.contains(localState.hashCode()) && local) && !(useLocalClosed && closedLocal.contains(localState.hashCode()))) {

                        if(useLocalClosed){
                            closedLocal.add(localState.hashCode());
                        }else{
                            closed.add(localState.hashCode());
                        }

                        if(localState.getHeuristic() < minLH){
                              minLH = state.getHeuristic();
                               if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal local [" + state.getParentActionOwner() + "] /h/: " + minLH);
                        }
                        if(localState.getG() > maxG){
                               maxG = state.getG();
                               if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
                        }

                        checkSolution(localState);

//                        final int receivedHeuristic = Integer.MIN_VALUE;//localState.getHeuristic() < Integer.MAX_VALUE && !problem.agent.equals(localState.getParentActionOwner()) ? localState.getHeuristic() : Integer.MIN_VALUE;

//                        if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": local search - open: " + openLocal.size());

                        if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.stop(problem.agent);
                        if(USE_STOPWATCH)DataAccumulator.getAccumulator().localHeuristicStopwatch.start(problem.agent);

                        localHeuristic.getHeuristic(localState, new HeuristicComputedCallback(){

                            @Override
                            public void heuristicComputed(HeuristicResult result) {
//	                        	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": local heuristic computed: " + state.hashCode() + "("+result.getValue()+")");



                                if(result.getValue() >= HeuristicInterface.LARGE_HEURISTIC){
                                    return;
                                }

//	                            System.out.println("LOCAL: " + result.getValue());

                                localState.setHeuristics(result.getValue());
//	                            localState.setHeuristics(Math.max(result.getValue(),receivedHeuristic));
//	                            localState.setHeuristics(5);

                                if (localState.wasReachedByPublicAction()) {
//	                            	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": send locally expanded state! ");
                                    sendState(localState,false);
                                }

                                DataAccumulator.getAccumulator().expandedStates++;
                                DataAccumulator.getAccumulator().expandedStatesLocal++;
                                if(!DataAccumulator.getAccumulator().initHeuristic.containsKey(problem.agent))DataAccumulator.getAccumulator().initHeuristic.put(problem.agent, result.getValue());


                                List<SearchState> expandedStates = expand(localState);
//	                            LOGGER.warn(problem.agent + ": local heuristic computed, expanded: " + expandedStates.size());
                                openLocal.addAll(expandedStates);
                            }
                        });

                        if(USE_STOPWATCH)DataAccumulator.getAccumulator().localHeuristicStopwatch.stop(problem.agent);
                        if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.start(problem.agent);

                        if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.stop(problem.agent);
                        boolean proceed = processCommunication();
                        if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.start(problem.agent);
                        if(!proceed)break;
                    }

                    if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.stop(problem.agent);
                    boolean proceed = processCommunication();
                    if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.start(problem.agent);
                    if(!proceed)break;

                    local = true;
                }

                if(USE_STOPWATCH)DataAccumulator.getAccumulator().localSearchStopwatch.stop(problem.agent);
            }


            boolean proceed = processCommunication();
            if(!proceed)break;

            // let other threads run
            Thread.yield();

        } while(run);

        if(USE_STOPWATCH)DataAccumulator.getAccumulator().searchStopwatch.stop(problem.agent);

        commPerformer.performClose();
    }

    /**
     * Distributed reconstruction of the plan
     * @param state
     * @param cost
     * @param initiator
     * @param plan
     */
    protected void reconstructPlan(SearchState state, List<String> globalPlan, String initiator, int solutionCost){
//		System.out.println(comm.getAddress() + " reconstruct " + state.hashCode() + " - " + plan);

        if(bestReconstructedPlanBy == null){
            bestReconstructedPlanBy = initiator;
            bestReconstructedPlanCost = solutionCost;
        }else{
//            if(bestReconstructedPlanCost < solutionCost || bestReconstructedPlanBy.compareTo(initiator) < 0){
//                LOGGER.info(comm.getAddress() +  " plan("+bestReconstructedPlanCost+") already reconstructed by "+initiator+" stop reconstruction of plan("+solutionCost+")");
//                return;
//            }
        }

        List<String> plan = new LinkedList<String>();
        ParentState lastState = state.reconstructPlan(plan);

        planCallback.partialPlanReconstructed(plan,initiator,solutionCost);

        plan.addAll(globalPlan);
        if(lastState.getParentActionOwner() == null){

            if(bestReconstructedPlanCost >= solutionCost){
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info(comm.getAddress() + " plan found " + state.hashCode() + " - " + plan);
                    LOGGER.info(comm.getAddress() + " send PLANNING_FINISHED!" );
                }
                protocol.sendPlanningFinishedMessage();
                run = false;

                planCallback.planFound(plan);
            }


        }else{

//        	LOGGER.info(comm.getAddress() + " send reconstruct msg " + state.hashCode() + " to " + lastState.getParentActionOwner());
            protocol.sendReconstructPlanMessage(new ReconstructPlanMessage(plan,lastState.hashCode(),initiator,solutionCost), lastState.getParentActionOwner());
        }
    }

    /**
     * Send reached state if reached by public action
     * @param state
     */
    public void sendState(final SearchState state, boolean global){

//        if(sentStates.containsKey(state.hashCode()))return;
        sentStates.put(state.hashCode(), state);

        StateMessage msg = new StateMessage(state.getValues(), state.getG(), state.getHeuristic(),global,false);

        DataAccumulator.getAccumulator().searchMessages ++;
        DataAccumulator.getAccumulator().totalBytes += msg.getBytes();


//		protocol.sendStateMessage(msg,CommunicationChannelBroadcast.BROADCAST_ADDRESS);

        //send only to relevant agents
        Set<String> sentTo = new HashSet<String>();
        for(Action a : problem.getProjectedActions()){
            if(!sentTo.contains(a.getOwner()) && a.isApplicableIn(state)){
                protocol.sendStateMessage(msg,a.getOwner());
                sentTo.add(a.getOwner());
            }
        }
    }

    protected List<SearchState> expand(SearchState state) {

//		LOGGER.info("expanding state with h=" + state.getHeuristic() + ", g+h=" + (state.getG()+state.getHeuristic()));

        List<SearchState> result = new ArrayList<SearchState>();

        for (Action action : problem.getMyActions()) {
            if (action.isApplicableIn(state)) {
                result.add(state.transformBy(action));
            }
        }

        return result;
    }

    protected boolean solutionFound(SearchState state) {
        if (state.unifiesWith(problem.goalSuperState)) {
            if(LOGGER.isInfoEnabled()){
                LOGGER.info("SOLUTION of cost "+state.getG()+" FOUND[" + problem.agent + "]: " + Arrays.toString(state.getValues()));

                LOGGER.info("LOCAL-OPEN-SIZE[" + problem.agent + "]" + openLocal.size());
                LOGGER.info("GLOBAL-OPEN-SIZE[" + problem.agent + "]" + openGlobal.size());
                LOGGER.info("CLOSED-SIZE[" + problem.agent + "]" + closed.size());
                LOGGER.info("LOCAL-CLOSED-SIZE[" + problem.agent + "]" + closedLocal.size());
            }

            return true;
        }

        return false;
    }

}
