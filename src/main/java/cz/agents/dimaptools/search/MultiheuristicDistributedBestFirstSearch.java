package cz.agents.dimaptools.search;

import java.util.ArrayList;
import java.util.HashSet;
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
import cz.agents.dimaptools.model.Action;

/**
 * Simple implementation of distributed Best-First Search with deferred heuristic evaluation.
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class MultiheuristicDistributedBestFirstSearch extends DistributedBestFirstSearch {

    private static final Logger LOGGER = Logger.getLogger(MultiheuristicDistributedBestFirstSearch.class);

    private final ArrayList<HeuristicOpenList> openLists = new ArrayList<HeuristicOpenList>();
    
    private int currentOpenList = 0;
    private OpenSelectionStrategy strategy = OpenSelectionStrategy.ALTERNATE;

	private int heuristicsWaiting;
	
	public boolean gsync=false;
	public boolean qsync=false;
    
    public enum OpenSelectionStrategy {
    	ALTERNATE,
    	PREFFERENCE
    }



    public MultiheuristicDistributedBestFirstSearch(DIMAPWorldInterface world) {
        this(world,Long.MAX_VALUE);
    }

    public MultiheuristicDistributedBestFirstSearch(DIMAPWorldInterface world,long timeLimitMs) {
        super(world, timeLimitMs);

    }

    
    private boolean allListsEmpty(){
        for(HeuristicOpenList open : openLists){
            if(!open.isEmpty()){
                return false;
            }
        }
        return true;
    }
    
    //TODO: the information about which heuristic from which open list was computed should be added, 
//    otherwise if received state had local heuristic computed, it would dominate the distributed heuristic
//    thats also probably why multiheuristic search converges so much towards projected heuristic
    protected void addReceivedState(final StateMessage sm, String sender){
    	final SearchState newState = new SearchState(problem.initState.getDomain(),sm,sender);
    	
    	for(final HeuristicOpenList open : openLists){
    		if(sm.hasQueueID() && open.label.equals(sm.getQueueID())){
		    	if(open.recomputeHeuristicOnReceive()){ 
		                open.getHeuristic(newState, new HeuristicComputedCallback() {
		    				@Override
		    				public void heuristicComputed(HeuristicResult result) {
		    					newState.setHeuristics(Math.max(result.getValue(), sm.getH()));
		    					open.add(newState,sm.isPreferred());
		    				}
		    			});
		    	}else{
		    		open.add(newState,sm.isPreferred());
		    	}
    		}
    	}
    }

    @Override
    protected void readConfiguration(ConfigurationInterface config){

        if(config.containsKey("heuristic") && !config.containsKey("requestHeuristic")){
            boolean usePreferred = config.containsKey("preferred") ? (Boolean)config.getObject("preferred") : false;
            openLists.add(new HeuristicOpenList("heuristic",usePreferred, (HeuristicInterface) config.getObject("heuristic"),config.getBoolean("recomputeHeuristicOnReceive", false)));
            return;
        }

        if(config.containsKey("heuristic") && config.containsKey("requestHeuristic")){
            boolean usePreferred = config.containsKey("preferred") ? (Boolean)config.getObject("preferred") : false;
            openLists.add(new HeuristicOpenList("heuristic",usePreferred, (HeuristicInterface) config.getObject("heuristic"), (HeuristicInterface) config.getObject("requestHeuristic"),config.getBoolean("recomputeHeuristicOnReceive", false)));
            return;
        }

        for(String key : config.getKeyList()){
            if(!key.equals("preferred") && !key.equals("openSelectionStrategy") && !key.equals("recomputeHeuristicOnReceive") && !key.equals("qsync") && !key.equals("gsync")){
                openLists.add((HeuristicOpenList)config.getObject(key));
            }
        }
        
        strategy = OpenSelectionStrategy.valueOf(config.getString("openSelectionStrategy", OpenSelectionStrategy.ALTERNATE.name()));
        
        gsync = config.getBoolean("gsync", false);
        qsync = config.getBoolean("qsync", false);

    }

    @Override
    public void plan(ConfigurationInterface config, SearchCallback planCallback) {

        readConfiguration(config);
        
        

        this.planCallback = planCallback;

        for(HeuristicOpenList open : openLists){
            open.add(new SearchState(problem.initState),false);
        }

        do{
            if(!allListsEmpty() && search){
                final SearchState state;

                state = openLists.get(currentOpenList).pollOpen();
                
                if(state==null && strategy != OpenSelectionStrategy.ALTERNATE){
                	currentOpenList = (currentOpenList + 1) % openLists.size();
                }
                
                if (state != null && !closed.contains(state.hashCode())) {
                	
                	if(state.getHeuristic() < minH){
    	              	minH = state.getHeuristic();
    	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal [" + state.getParentActionOwner() + "] /h/: " + minH);
    	            }
    	            if(state.getG() > maxG){
    	               	maxG = state.getG();
    	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
    	            }

                    closed.add(state.hashCode());
                    DataAccumulator.getAccumulator().expandedStates ++;

                    if (solutionFound(state)){
                        if (state.wasExpandedByMe(problem.agent)) {
                            long time =  System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs;
                            LOGGER.warn(" by me ("+problem.agent+") - " + time);

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

                    final List<Action> applicableActions = new LinkedList<Action>();
                    for (Action action : problem.getMyActions()) {
                        if (action.isApplicableIn(state)) {
                            applicableActions.add(action);
                        }
                    }



                    final boolean stateIsPreferred = state.wasCreatedByPreferredAction();
                    
                    int bestOpenPriority = 0;
                    
                    if(gsync)heuristicsWaiting = openLists.size();
                    
                    for(int ol=0; ol < openLists.size(); ol++){
                    	final HeuristicOpenList open = openLists.get(ol);

//		        		LOGGER.info(comm.getAddress() + "("+open.label+") get heuristic of state " + state.hashCode());
                    	
                    	if(qsync)heuristicsWaiting = 1;

                        open.getHeuristic(state, new HeuristicComputedCallback(){

                            @Override
                            public void heuristicComputed(HeuristicResult result) {
    //	                    	heurCount --;
    //	                    	LOGGER.info(comm.getAddress() + " HEUR-- " + heurCount + ", open:" + open.size());

//		                    	LOGGER.info(comm.getAddress() + "("+open.label+") computed heuristic of state " + state.hashCode());

                                if(result.getValue() >= HeuristicInterface.LARGE_HEURISTIC){
                                    LOGGER.info(comm.getAddress() + " LARGE_HEURISTIC");
                                    return;
                                }

                                if(result.getValue() < minH){
                                    //System.out.println("found best state: " + result.getValue() + "("+stateIsPreferred+")");
                                    open.boost(stateIsPreferred);
                                }

                                state.setHeuristics(result.getValue());

                                if (state.wasReachedByPublicAction()) {
                                    sendState(state,stateIsPreferred,open.label);
                                }

                                

//                                int applicable = 0;
//                                int pref = 0;

//		                        LOGGER.info("Actions applicable in " + state + ":\n"+applicableActions);
//		                        if(result.hasHelpfulActions()){
//		                        	LOGGER.info("Helpful actions:\n");
//		                        	for(int a : result.getHelpfulActions().toArray()){
//		                        		LOGGER.info(problem.getAction(a));
//		                        	}
//		                        }

                                for (Action action : applicableActions) {
//                                    ++applicable;
                                    if(open.usePreferred() && result.hasHelpfulActions() && result.getHelpfulActions().contains(action.hashCode())){
                                            SearchState newState = state.transformBy(action,true);

                                            open.add(newState, false);
                                            open.add(newState, true);
//
//                                            ++pref;
                                    }else{
                                            SearchState newState = state.transformBy(action);
                                            open.add(newState, false);
                                    }
                                }



//		                        LOGGER.info(comm.getAddress() + " applicable(pref):"+applicable + "(" + pref + ")");
//
//		                        if(pref != result.getHelpfulActions().size()){
//		                        	LOGGER.warn("pref != result.getHelpfulActions().size(), result.getHelpfulActions().size()="+result.getHelpfulActions().size());
//		                        }
                                
                                heuristicsWaiting -= 1;

                            }
                        });
                        
                        
                        if(qsync){
	                        LOGGER.info("waiting for heuristic...");
	            	        while(heuristicsWaiting > 0){
	            				commPerformer.performReceiveNonblock();
	            				
	            				if(search){
	            					for(HeuristicOpenList openList : openLists){
	            	            		openList.processMessages();
	            	            	}
	            				}
	            				
	            				if(!run || !search)break;
	            			}
                        }
                        
                        if(strategy == OpenSelectionStrategy.PREFFERENCE && open.getPriority() > bestOpenPriority){
                        	bestOpenPriority = open.getPriority();
                        	currentOpenList = ol;
                        }
                    }
                    
                    if(gsync){
                        LOGGER.info("waiting for heuristic...");
            	        while(heuristicsWaiting > 0){
            				commPerformer.performReceiveNonblock();
            				
            				if(search){
            					for(HeuristicOpenList openList : openLists){
            	            		openList.processMessages();
            	            	}
            				}
            				
            				if(!run || !search)break;
            			}
                    }

                }

                if(System.currentTimeMillis() - DataAccumulator.getAccumulator().startTimeMs > timeLimitMs){
                    run = false;
                    LOGGER.warn("TIMEOUT!");
                    planCallback.planNotFound();
                    break;
                }
            }

//			LOGGER.info(comm.getAddress() + " open:"+open.size());

            commPerformer.performReceiveNonblock();

            if(search){
            	for(HeuristicOpenList openList : openLists){
            		openList.processMessages();
            	}
            }

            if(strategy == OpenSelectionStrategy.ALTERNATE){
            	currentOpenList = (currentOpenList + 1) % openLists.size();
            }

        }while(run);

        commPerformer.performClose();
    }
    
    /**
     * Send reached state if reached by public action
     * @param state
     */
    public void sendState(final SearchState state,boolean stateIsPreferred, String queueID){

        if(sentStates.containsKey(state.hashCode()))return;
        sentStates.put(state.hashCode(), state);

        StateMessage msg = new StateMessage(state.getValues(), state.getG(), state.getHeuristic(), stateIsPreferred,queueID);

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



}
