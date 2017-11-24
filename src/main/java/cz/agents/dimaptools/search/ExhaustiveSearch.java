package cz.agents.dimaptools.search;

import gnu.trove.TIntHashSet;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.log4j.Logger;

import cz.agents.alite.configurator.ConfigurationInterface;
import cz.agents.dimaptools.model.Action;
import cz.agents.dimaptools.model.Problem;

/**
 * Reference implementation of A* Search
 * config:
 * 	"heuristic" - implementation of HeuristicInterface used to guide the search
 * @author stolba
 *
 */
public class ExhaustiveSearch implements SearchInterface {

    private static final Logger LOGGER = Logger.getLogger(ExhaustiveSearch.class);

    private final Problem problem;
    private final Set<Action> actions;
//    private final PriorityQueue<SearchState> open = new PriorityQueue<SearchState>();
    private final LinkedList<SearchState> open = new LinkedList<SearchState>();
    private final TIntHashSet closed = new TIntHashSet();

    private SearchState bestState = null;
    
    private float maxG = 0;
    private float minH = Integer.MAX_VALUE;
    
    private boolean reportAllSolutions = false;



    public ExhaustiveSearch(Problem problem) {
        this.problem = problem;
        actions = problem.getMyActions();

        
    }
    
    public ExhaustiveSearch(Problem problem,Set<Action> useActions) {
        this.problem = problem;
        actions = useActions;

        
    }
    
    public void setReportAllSolutions(){
    	reportAllSolutions = true;
    }



    @Override
    public void plan(ConfigurationInterface config, SearchCallback planFoundCallback) {

//        reportAllSolutions = config.getBoolean("reportAllSolutions", false);

        open.add(new SearchState(problem.initState));

        while(!open.isEmpty()){
            final SearchState state;

            
                state = open.poll();
                
	            if(state.getHeuristicF() < minH){
	              	minH = state.getHeuristicF();
	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new minimal [" + state.getParentActionOwner() + "] /h/: " + minH);
	            }
	            if(state.getGF() > maxG){
	               	maxG = state.getGF();
	               	if(LOGGER.isInfoEnabled())LOGGER.info(problem.agent + ": Reached new maximal [" + state.getParentActionOwner() + "] /g/: " + maxG);
	            }
                
            
            
            if (solutionFound(state)) {
            	if(bestState == null){
            		bestState = state;
            	}else if(state.getGF() < bestState.getGF()){
            		bestState = state;
            	}
            	if(reportAllSolutions){
            		System.out.println("REPORT");
            		List<String> plan = new LinkedList<String>();
                    state.reconstructPlan(plan);
                    planFoundCallback.planFound(plan);
            	}
            }
          

            if (!closed.contains(state.hashCode())) {

                closed.add(state.hashCode());

                
                List<SearchState> expandedStates = expand(state);
                for(final SearchState s : expandedStates){
                	s.setHeuristics(0);
//                	if(!open.contains(s)){
                    	open.add(s);
//                    }
                }


                

            }
            
           

        }
        
        if(!reportAllSolutions){
        	LOGGER.info("report best found solution state g=" + bestState.getGF());
	        List<String> plan = new LinkedList<String>();
	        bestState.reconstructPlan(plan);
	        planFoundCallback.planFound(plan);
        }
        
//        LOGGER.info("solution state g=" + bestState.getGF());
        LOGGER.info("OPEN-SIZE[" + problem.agent + "]" + open.size());
        LOGGER.info("CLOSED-SIZE[" + problem.agent + "]" + closed.size());
    }


    private List<SearchState> expand(SearchState state) {

        //LOGGER.info("expanding state with h=" + state.getHeuristic() + ", g+h=" + (state.getG()+state.getHeuristic()));

        List<SearchState> result = new LinkedList<SearchState>();

        for (Action action : actions) {
            if (action.isApplicableIn(state)) {
                result.add(state.transformBy(action));
            }
        }

        return result;
    }

    private boolean solutionFound(SearchState state) {
        if (state.unifiesWith(problem.goalSuperState)) {
        	LOGGER.info("solution state g=" + state.getGF());

            return true;
        }

        return false;
    }

}
