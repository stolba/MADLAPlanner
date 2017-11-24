package cz.agents.dimaptools.search;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;

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
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.heuristic.goalsat.GoalSatHeuristic;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

/**
 * Simple implementation of distributed Best-First Search with deferred heuristic evaluation.
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class DistributedBestFirstSearch implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(DistributedBestFirstSearch.class);

    protected final Problem problem;
    protected final PriorityBlockingQueue<SearchState> open;
    protected final TIntHashSet closed = new TIntHashSet();

    protected HeuristicInterface heuristic;
    protected HeuristicInterface requestHeuristic;
    protected SearchCallback planCallback;

    protected boolean recomputeHeuristicOnReceive = false;

    protected final Communicator comm;
    protected final CommunicationPerformer commPerformer;
    protected final TIntObjectHashMap sentStates = new TIntObjectHashMap();

    protected final DistributedSearchProtocol protocol;

    protected String bestReconstructedPlanBy = null;
    protected int bestReconstructedPlanCost = Integer.MAX_VALUE;

    protected long timeLimitMs = Long.MAX_VALUE;

    protected volatile boolean run = true;
    protected volatile boolean search = true;

    protected int minH = Integer.MAX_VALUE;
    protected int maxG = 0;




    public DistributedBestFirstSearch(DIMAPWorldInterface world) {
        this(world,Long.MAX_VALUE);
    }

    public DistributedBestFirstSearch(DIMAPWorldInterface world,long timeLimitMs) {
        this.problem = world.getProblem();
        this.comm = world.getCommunicator();
        commPerformer = world.getCommPerformer();
        this.timeLimitMs = timeLimitMs;

        heuristic = new GoalSatHeuristic(problem.goalSuperState);
        requestHeuristic = heuristic;

        open = new PriorityBlockingQueue<SearchState>(10000,new Comparator<SearchState>(){

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


                run = false;

            }
        };


    }


    protected void addReceivedState(final StateMessage sm, String sender){
        if(recomputeHeuristicOnReceive){ //should be probably done for projected heuristics
            final SearchState newState = new SearchState(problem.initState.getDomain(),sm,sender);

            heuristic.getHeuristic(newState, new HeuristicComputedCallback() {

                @Override
                public void heuristicComputed(HeuristicResult result) {
                    newState.setHeuristics(Math.max(result.getValue(), sm.getH()));
                    open.add(newState);
                }
            });

        }else{

            open.add(new SearchState(problem.initState.getDomain(),sm,sender));

        }
    }


    protected void readConfiguration(ConfigurationInterface config){
        if(config.containsKey("heuristic")){
            heuristic = (HeuristicInterface) config.getObject("heuristic");
        }

        if(config.containsKey("requestHeuristic")){
            requestHeuristic = (HeuristicInterface) config.getObject("requestHeuristic");
        }else{
            requestHeuristic = heuristic;
        }

        recomputeHeuristicOnReceive = config.getBoolean("recomputeHeuristicOnReceive", false);
    }


    @Override
    public void plan(ConfigurationInterface config, SearchCallback planCallback) {

        readConfiguration(config);

        this.planCallback = planCallback;

//        DataAccumulator.getAccumulator().searchStopwatch.start(problem.agent);

        open.add(new SearchState(problem.initState));

        do {
            if(!open.isEmpty() && search) {
//                DataAccumulator.getAccumulator().globalSearchStopwatch.start(problem.agent);

                final SearchState state = open.poll();

                if(state.getHeuristic() < minH){
                      minH = state.getHeuristic();
                       if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal [" + state.getParentActionOwner() + "] /h/: " + minH);
                }
                if(state.getG() > maxG){
                       maxG = state.getG();
                       if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
                }

                if (state != null && !closed.contains(state.hashCode())) {

                    closed.add(state.hashCode());

                    if (solutionFound(state)){
                        if (state.wasExpandedByMe(problem.agent)) {
                            long time =  System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs;
                            LOGGER.warn(" by me ("+problem.agent+") - " + time);

//                            DataAccumulator.getAccumulator().searchStopwatch.stop(problem.agent);
//                            DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);

                            List<String> plan = new LinkedList<String>();
                            reconstructPlan(state, plan, problem.agent, state.getG());

//	                    	return;
                            search = false;

                        }
//	                    else {
//	                    	LOGGER.warn(" not by me ("+problem.agent+")");
////	                    	planCallback.planFoundByOther();
//	                    }



                    }

//		        	heurCount ++;
//		        	LOGGER.info(comm.getAddress() + " HEUR++ " + heurCount);

//                    DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);
//                    DataAccumulator.getAccumulator().globalHeuristicStopwatch.start(problem.agent);

                    heuristic.getHeuristic(state, new HeuristicComputedCallback(){

                        @Override
                        public void heuristicComputed(HeuristicResult result) {


//	                    	heurCount --;
//	                    	LOGGER.info(comm.getAddress() + " HEUR-- " + heurCount + ", open:" + open.size());

                            if(result.getValue() >= HeuristicInterface.LARGE_HEURISTIC){
                                LOGGER.info(comm.getAddress() + " LARGE_HEURISTIC"+ ", open:" + open.size() + ", state: "+state);
                                return;
                            }
                            state.setHeuristics(result.getValue());

                            if (state.wasReachedByPublicAction()) {
                                sendState(state);
                            }

                            DataAccumulator.getAccumulator().expandedStates++;
                            if(!DataAccumulator.getAccumulator().initHeuristic.containsKey(problem.agent))DataAccumulator.getAccumulator().initHeuristic.put(problem.agent, result.getValue());

                            List<SearchState> expandedStates = expand(state);
                            open.addAll(expandedStates);
                        }
                    });


//                    DataAccumulator.getAccumulator().globalHeuristicStopwatch.stop(problem.agent);
//                    DataAccumulator.getAccumulator().globalSearchStopwatch.start(problem.agent);

                }

                if(System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs > timeLimitMs){
                    run = false;
                    LOGGER.warn("TIMEOUT!");
                    planCallback.planNotFound();
                    break;
                }

//                DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);
            }

            //LOGGER.info(comm.getAddress() + " open:"+open.size());

            commPerformer.performReceiveNonblock();

            if(search){
//                DataAccumulator.getAccumulator().globalHeuristicStopwatch.start(problem.agent);
                heuristic.processMessages();
//                DataAccumulator.getAccumulator().globalHeuristicStopwatch.stop(problem.agent);

//                DataAccumulator.getAccumulator().otherHeuristicStopwatch.start(problem.agent);
                requestHeuristic.processMessages();
//                DataAccumulator.getAccumulator().otherHeuristicStopwatch.stop(problem.agent);
            }

            // let other threads run
            Thread.yield();

        } while(run);

//        DataAccumulator.getAccumulator().searchStopwatch.stop(problem.agent);

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

//        if(bestReconstructedPlanBy == null){
//            bestReconstructedPlanBy = initiator;
//            bestReconstructedPlanCost = solutionCost;
//        }else{
//            if(bestReconstructedPlanCost < solutionCost || bestReconstructedPlanBy.compareTo(initiator) < 0){
//                LOGGER.info(comm.getAddress() +  " plan("+bestReconstructedPlanCost+") already reconstructed by "+initiator+" stop reconstruction of plan("+solutionCost+")");
//                return;
//            }
//        }

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
    public void sendState(final SearchState state){

        if(sentStates.containsKey(state.hashCode()))return;
        sentStates.put(state.hashCode(), state);

        StateMessage msg = new StateMessage(state.getValues(), state.getG(), state.getHeuristic());

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

                LOGGER.info("OPEN-SIZE[" + problem.agent + "]" + open.size());
                LOGGER.info("CLOSED-SIZE[" + problem.agent + "]" + closed.size());
            }

            return true;
        }

        return false;
    }

}
