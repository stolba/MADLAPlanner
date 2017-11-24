package cz.agents.dimaptools.search;

import gnu.trove.TIntHashSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface;
import cz.agents.dimaptools.heuristic.HeuristicInterface.HeuristicComputedCallback;
import cz.agents.dimaptools.heuristic.HeuristicResult;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;
import cz.agents.dimaptools.model.State;

/**
 * Reference implementation of Best-First Search with lazy heuristic evaluation.
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class BestFirstSearch implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(BestFirstSearch.class);

    private final Problem problem;
    private final PriorityBlockingQueue<SearchState> open = new PriorityBlockingQueue<SearchState>();
    private final TIntHashSet closed = new TIntHashSet();

    private HeuristicInterface heuristic;
    
    private int maxG = 0;
    private int minH = Integer.MAX_VALUE;



    public BestFirstSearch(Problem problem) {
        this.problem = problem;

        heuristic = new HeuristicInterface() {

            @Override
            public void getHeuristic(State state, HeuristicComputedCallback callback) {
                callback.heuristicComputed(new HeuristicResult(0));
            }

            @Override
            public void processMessages() {
            }
        };

//		heuristic = new FFHeuristic(problem);
    }



    @Override
    public void plan(ConfigurationInterface config, SearchCallback planFoundCallback) {

        if(config.containsKey("heuristic")){
            heuristic = (HeuristicInterface) config.getObject("heuristic");
        }

        open.add(new SearchState(problem.initState));

        do{
            final SearchState state;

            try {
                state = open.poll(1, TimeUnit.DAYS);
                
                
	            if(state.getHeuristic() < minH){
	              	minH = state.getHeuristic();
	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal [" + state.getParentActionOwner() + "] /h/: " + minH);
	            }
	            if(state.getG() > maxG){
	               	maxG = state.getG();
	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
	            }
                
            } catch (InterruptedException e) {
                planFoundCallback.planNotFound();
                LOGGER.fatal("Search timeout!", e);
                System.exit(1);
                return;
            }

            if (!closed.contains(state.hashCode())) {

                closed.add(state.hashCode());

                if (solutionFound(state)) {
                    if (state.wasExpandedByMe(problem.agent)) {
                        List<String> plan = new LinkedList<String>();
                        state.reconstructPlan(plan);
                        planFoundCallback.planFound(plan);
                        return;
                    } else {
                        planFoundCallback.planNotFound();
                        return;
                    }
                }


                heuristic.getHeuristic(state, new HeuristicComputedCallback(){

                    @Override
                    public void heuristicComputed(HeuristicResult result) {
                        state.setHeuristics(result.getValue());
                        
                        Set<SearchState> expandedStates = expand(state);
                        open.addAll(expandedStates);
                    }
                });

            }

        }while(true);
    }


    private Set<SearchState> expand(SearchState state) {

        LOGGER.info("expanding state with h=" + state.getHeuristic() + ", g+h=" + (state.getG()+state.getHeuristic()));

        Set<SearchState> result = new HashSet<SearchState>();

        for (Action action : problem.getMyActions()) {
            if (action.isApplicableIn(state)) {
                result.add(state.transformBy(action));
            }
        }

        return result;
    }

    private boolean solutionFound(SearchState state) {
        if (state.unifiesWith(problem.goalSuperState)) {
            LOGGER.info("OPEN-SIZE[" + problem.agent + "]" + open.size());
            LOGGER.info("CLOSED-SIZE[" + problem.agent + "]" + closed.size());

            return true;
        }

        return false;
    }

}
