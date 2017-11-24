package cz.agents.dimaptools.search;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.DIMAPWorldInterface;
import cz.agents.dimaptools.communication.message.StateMessage;
import cz.agents.dimaptools.experiment.DataAccumulator;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;

/**
 * Simple implementation of distributed Best-First Search with deferred heuristic evaluation.
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class SyncDistributedBestFirstSearch extends DistributedBestFirstSearch implements SearchInterface, HeuristicComputedCallback {

    private static final Logger LOGGER = Logger.getLogger(SyncDistributedBestFirstSearch.class);

    private SearchState state;

    private boolean heuristicComputed;

    public SyncDistributedBestFirstSearch(DIMAPWorldInterface world) {
        this(world,Long.MAX_VALUE);
    }

    public SyncDistributedBestFirstSearch(DIMAPWorldInterface world,long timeLimitMs) {
        super(world,timeLimitMs);

    }




    @Override
    public void plan(ConfigurationInterface config, SearchCallback planCallback) {

        readConfiguration(config);

//        DataAccumulator.getAccumulator().searchStopwatch.start(problem.agent);

        this.planCallback = planCallback;

        open.add(new SearchState(problem.initState));

        do {
            if(!open.isEmpty() && search) {

//                DataAccumulator.getAccumulator().globalSearchStopwatch.start(problem.agent);

                state = open.poll();

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

                    heuristicComputed = false;

//                    DataAccumulator.getAccumulator().globalSearchStopwatch.stop(problem.agent);
//                    DataAccumulator.getAccumulator().globalHeuristicStopwatch.start(problem.agent);

                    heuristic.getHeuristic(state, this);

                }

                if(System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs > timeLimitMs){
                    run = false;
                    LOGGER.warn("TIMEOUT!");
                    planCallback.planNotFound();
                    break;
                }

//	        LOGGER.info("waiting for heuristic...");
            while(!heuristicComputed){
                commPerformer.performReceiveNonblock();

                if(search){
//                    DataAccumulator.getAccumulator().globalHeuristicStopwatch.start(problem.agent);
                    heuristic.processMessages();
//                    DataAccumulator.getAccumulator().globalHeuristicStopwatch.stop(problem.agent);

//                    DataAccumulator.getAccumulator().otherHeuristicStopwatch.start(problem.agent);
                    requestHeuristic.processMessages();
//                    DataAccumulator.getAccumulator().otherHeuristicStopwatch.stop(problem.agent);
                }

                if(!run || !search)break;
            }

//            DataAccumulator.getAccumulator().globalHeuristicStopwatch.stop(problem.agent);
//            DataAccumulator.getAccumulator().globalSearchStopwatch.start(problem.agent);

//	        LOGGER.info("... computed");
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

    @Override
    public void heuristicComputed(HeuristicResult result) {
        if(result.getValue() >= HeuristicInterface.LARGE_HEURISTIC){
            LOGGER.info(comm.getAddress() + " LARGE_HEURISTIC"+ ", open:" + open.size() + ", state: "+state);
            return;
        }
        state.setHeuristics(result.getValue());

        if (state.wasReachedByPublicAction()) {
            sendState(state);
        }

        DataAccumulator.getAccumulator().expandedStates++;
        if(!DataAccumulator.getAccumulator().initHeuristicDist.containsKey(problem.agent))DataAccumulator.getAccumulator().initHeuristicDist.put(problem.agent, result.getValue());


        List<SearchState> expandedStates = expand(state);
        open.addAll(expandedStates);

        heuristicComputed = true;

    }





}
